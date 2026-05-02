package com.pedrogm.tdtflow.data.repository

import android.util.Log
import com.pedrogm.tdtflow.data.local.ChannelDao
import com.pedrogm.tdtflow.data.local.toDomain
import com.pedrogm.tdtflow.data.local.toEntity
import com.pedrogm.tdtflow.data.remote.TdtApi
import com.pedrogm.tdtflow.data.remote.TdtChannelsResponse
import com.pedrogm.tdtflow.data.remote.toChannel
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.repository.ChannelRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class ChannelRepositoryImpl(
    private val tdtApi: TdtApi,
    private val channelDao: ChannelDao,
    private val ioDispatcher: CoroutineDispatcher,
    private val cache: ChannelCache = ChannelCache(),
    private val onError: (Throwable) -> Unit = {}
) : ChannelRepository {

    companion object {
        private const val TAG = "ChannelRepository"
        private const val DEFAULT_REGION = "Spain"
    }

    override fun getChannels(): Flow<List<Channel>> = flow {
        // 1. Memory cache
        cache.get()?.let {
            emit(it)
            return@flow
        }

        // 2. Room fallback (initial load)
        val localChannels = channelDao.getAllChannels().map { it.toDomain() }
        if (localChannels.isNotEmpty()) {
            emit(localChannels)
        }

        // 3. Network load
        val remoteChannels = loadChannels()
        if (remoteChannels.isNotEmpty()) {
            cache.put(remoteChannels)
            channelDao.deleteAll()
            channelDao.insertChannels(remoteChannels.map { it.toEntity() })
            emit(remoteChannels)
        }
    }

    private suspend fun loadChannels(): List<Channel> = withContext(ioDispatcher) {
        try {
            Log.d(TAG, "Loading channels from TDTChannels API...")

            // Build fallback list concurrently with network requests
            val fallbackDeferred = async { fallbackChannels() }
            val tvResponseDeferred = async { fetchTvChannels() }
            val radioResponseDeferred = async { fetchRadioChannels() }

            val tvChannelsDeferred = async { mapTvChannels(tvResponseDeferred.await()) }
            val radioChannelsDeferred = async { mapRadioChannels(radioResponseDeferred.await()) }

            val tvChannels = tvChannelsDeferred.await()
            val radioChannels = radioChannelsDeferred.await()

            if (tvChannels.isEmpty() && radioChannels.isEmpty()) {
                Log.w(TAG, "Spain not found or mapping failed, using fallback")
                return@withContext fallbackDeferred.await()
            }

            Log.d(TAG, "Mapped ${tvChannels.size} TV + ${radioChannels.size} radio channels")

            val apiChannels = tvChannels + radioChannels
            val combined = mergeWithFallback(fallbackDeferred.await(), apiChannels)

            Log.d(TAG, "Total combined channels: ${combined.size}")
            combined
        } catch (e: Exception) {
            Log.e(TAG, "Error loading TDTChannels: ${e.message}", e)
            val fallback = fallbackChannels()
            Log.d(TAG, "Using only ${fallback.size} fallback channels")
            onError(e)
            fallback
        }
    }

    private suspend fun fetchTvChannels(): TdtChannelsResponse? =
        runCatching { tdtApi.getTvChannels() }
            .onFailure { Log.w(TAG, "TV fetch failed: ${it.message}") }
            .getOrNull()

    private suspend fun fetchRadioChannels(): TdtChannelsResponse? =
        runCatching { tdtApi.getRadioChannels() }
            .onFailure { Log.w(TAG, "Radio fetch failed: ${it.message}") }
            .getOrNull()

    private fun mapTvChannels(response: TdtChannelsResponse?): List<Channel> =
        response?.countries?.firstOrNull { it.name == DEFAULT_REGION }?.ambits?.flatMap { ambit ->
            ambit.channels.mapNotNull { channel ->
                channel.toChannel(ambitName = ambit.name, isRadioManual = false)
            }
        } ?: emptyList()

    private fun mapRadioChannels(response: TdtChannelsResponse?): List<Channel> =
        response?.countries?.firstOrNull { it.name == DEFAULT_REGION }?.ambits
            ?.flatMap { ambit ->
                ambit.channels.mapNotNull { channel ->
                    channel.toChannel(ambitName = ambit.name, isRadioManual = true)
                }
            } ?: emptyList()

    private fun mergeWithFallback(fallback: List<Channel>, apiChannels: List<Channel>): List<Channel> =
        mergeChannelsWithFallback(fallback, apiChannels)
}
