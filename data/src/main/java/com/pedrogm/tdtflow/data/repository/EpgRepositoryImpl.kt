package com.pedrogm.tdtflow.data.repository

import android.util.Log
import com.pedrogm.tdtflow.data.remote.TdtApi
import com.pedrogm.tdtflow.data.remote.TdtEpgEvent
import com.pedrogm.tdtflow.domain.model.Program
import com.pedrogm.tdtflow.domain.repository.EpgRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * EPG real contra https://www.tdtchannels.com/epg/{TV,RADIO}.json.
 * El índice completo (TV + radio) se descarga una vez y se cachea en memoria con TTL,
 * indexado por `name` (= `epg_id` de cada [com.pedrogm.tdtflow.domain.model.Channel]).
 */
class EpgRepositoryImpl(
    private val tdtApi: TdtApi,
    private val ioDispatcher: CoroutineDispatcher,
    private val ttlMs: Long = DEFAULT_TTL_MS
) : EpgRepository {

    private companion object {
        const val TAG = "EpgRepository"
        const val DEFAULT_TTL_MS = 3 * 60 * 60 * 1000L // 3h
        const val REFRESH_INTERVAL_MS = 60_000L
    }

    private val mutex = Mutex()
    private var cachedIndex: Map<String, List<TdtEpgEvent>>? = null
    private var cachedAtMs: Long = 0L

    private suspend fun epgIndex(): Map<String, List<TdtEpgEvent>> = mutex.withLock {
        val now = System.currentTimeMillis()
        cachedIndex?.let { if (now - cachedAtMs < ttlMs) return it }

        val index = withContext(ioDispatcher) {
            val tv = runCatching { tdtApi.getTvEpg() }
                .onFailure { Log.w(TAG, "TV EPG fetch failed: ${it.message}") }
                .getOrDefault(emptyList())
            val radio = runCatching { tdtApi.getRadioEpg() }
                .onFailure { Log.w(TAG, "Radio EPG fetch failed: ${it.message}") }
                .getOrDefault(emptyList())
            (tv + radio).associateBy({ it.name }, { it.events })
        }

        cachedIndex = index
        cachedAtMs = now
        index
    }

    private fun TdtEpgEvent.toProgram(epgId: String) = Program(
        title = t,
        description = d,
        startTime = hi * 1000,
        endTime = hf * 1000,
        channelId = epgId
    )

    override fun getNowPlaying(epgId: String): Flow<Program?> = flow {
        if (epgId.isBlank()) {
            emit(null)
            return@flow
        }
        while (true) {
            val events = epgIndex()[epgId].orEmpty()
            val nowMs = System.currentTimeMillis()
            val current = events.firstOrNull { nowMs in (it.hi * 1000) until (it.hf * 1000) }
            emit(current?.toProgram(epgId))
            delay(REFRESH_INTERVAL_MS)
        }
    }

    override fun getSchedule(epgId: String): Flow<List<Program>> = flow {
        if (epgId.isBlank()) {
            emit(emptyList())
            return@flow
        }
        val events = epgIndex()[epgId].orEmpty()
        emit(events.map { it.toProgram(epgId) }.sortedBy { it.startTime })
    }
}
