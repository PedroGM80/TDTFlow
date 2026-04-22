package com.pedrogm.tdtflow.data.repository

import android.util.Log
import com.pedrogm.tdtflow.data.remote.AmbitConstants
import com.pedrogm.tdtflow.data.remote.NetworkModule
import com.pedrogm.tdtflow.data.remote.TdtChannelsResponse
import com.pedrogm.tdtflow.data.remote.toChannel
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.repository.ChannelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class ChannelRepositoryImpl(
    private val cache: ChannelCache = ChannelCache(),
    private val onError: (Throwable) -> Unit = {}
) : ChannelRepository {

    companion object {
        private const val TAG = "ChannelRepository"
        private const val SPAIN = "Spain"
    }

    override fun getChannels(): Flow<List<Channel>> = flow {
        cache.get()?.let {
            emit(it)
            return@flow
        }
        val channels = loadChannels()
        cache.put(channels)
        emit(channels)
    }

    private suspend fun loadChannels(): List<Channel> = withContext(Dispatchers.IO) {
        val fallback = fallbackChannels()
        try {
            Log.d(TAG, "Loading channels from TDTChannels API...")

            // Fetch TV and Radio in parallel — halves load time vs sequential
            val tvDeferred = async { fetchTvChannels() }
            val radioDeferred = async { fetchRadioChannels() }
            val tvResponse = tvDeferred.await()
            val radioResponse = radioDeferred.await()

            if (tvResponse == null && radioResponse == null) {
                Log.w(TAG, "Spain not found in responses, using fallback")
                return@withContext fallback
            }

            val tvChannels = mapTvChannels(tvResponse)
            val radioChannels = mapRadioChannels(radioResponse)

            Log.d(TAG, "Mapped ${tvChannels.size} TV + ${radioChannels.size} radio channels")

            val apiChannels = tvChannels + radioChannels
            val combined = mergeWithFallback(fallback, apiChannels)

            Log.d(TAG, "Total combined channels: ${combined.size}")
            combined
        } catch (e: Exception) {
            Log.e(TAG, "Error loading TDTChannels: ${e.message}", e)
            Log.d(TAG, "Using only ${fallback.size} fallback channels")
            onError(e)
            fallback
        }
    }

    private suspend fun fetchTvChannels(): TdtChannelsResponse? =
        runCatching { NetworkModule.getTvChannels() }
            .onFailure { Log.w(TAG, "TV fetch failed: ${it.message}") }
            .getOrNull()

    private suspend fun fetchRadioChannels(): TdtChannelsResponse? =
        runCatching { NetworkModule.getRadioChannels() }
            .onFailure { Log.w(TAG, "Radio fetch failed: ${it.message}") }
            .getOrNull()

    private suspend fun mapTvChannels(response: TdtChannelsResponse?): List<Channel> =
        withContext(Dispatchers.Default) {
            response?.countries?.firstOrNull { it.name == SPAIN }?.ambits?.flatMap { ambit ->
                ambit.channels.mapNotNull { channel ->
                    channel.toChannel(ambitName = ambit.name, isRadioManual = false)
                }
            } ?: emptyList()
        }

    private suspend fun mapRadioChannels(response: TdtChannelsResponse?): List<Channel> =
        withContext(Dispatchers.Default) {
            response?.countries?.firstOrNull { it.name == SPAIN }?.ambits
                ?.flatMap { ambit ->
                    ambit.channels.mapNotNull { channel ->
                        channel.toChannel(ambitName = ambit.name, isRadioManual = true)
                    }
                } ?: emptyList()
        }

    private fun mergeWithFallback(fallback: List<Channel>, apiChannels: List<Channel>): List<Channel> =
        mergeChannelsWithFallback(fallback, apiChannels)
}
