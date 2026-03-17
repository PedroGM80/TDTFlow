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

class ChannelRepositoryImpl : ChannelRepository {

    companion object {
        private const val TAG = "ChannelRepository"
        private const val SPAIN = "Spain"
    }

    override fun getChannels(): Flow<List<Channel>> = flow {
        emit(loadChannels())
    }

    private suspend fun loadChannels(): List<Channel> = withContext(Dispatchers.IO) {
        val fallback = fallbackChannels()
        try {
            Log.d(TAG, "Loading channels from TDTChannels API...")

            // Fetch TV and Radio in parallel
            val tvResponse = fetchTvChannels()
            val radioResponse = fetchRadioChannels()

            if (tvResponse == null && radioResponse == null) {
                Log.w(TAG, "Spain not found in responses, using fallback")
                return@withContext fallback
            }

            // Map API responses to Channel objects
            val tvChannels = mapTvChannels(tvResponse)
            val radioChannels = mapRadioChannels(radioResponse)

            Log.d(TAG, "Mapped ${tvChannels.size} TV + ${radioChannels.size} radio channels")

            val apiChannels = tvChannels + radioChannels

            // Merge with fallback, enriching with logos where needed
            val combined = mergeWithFallback(fallback, apiChannels)

            Log.d(TAG, "Total combined channels: ${combined.size}")
            combined
        } catch (e: Exception) {
            Log.e(TAG, "Error loading TDTChannels: ${e.message}", e)
            Log.d(TAG, "Using only ${fallback.size} fallback channels")
            fallback
        }
    }

    /**
     * Fetches TV channels from the API
     */
    private suspend fun fetchTvChannels(): TdtChannelsResponse? = withContext(Dispatchers.IO) {
        runCatching { NetworkModule.service.getChannels() }.getOrNull()
    }

    /**
     * Fetches radio channels from the API
     */
    private suspend fun fetchRadioChannels(): TdtChannelsResponse? = withContext(Dispatchers.IO) {
        runCatching { NetworkModule.service.getRadioChannels() }.getOrNull()
    }

    /**
     * Extracts and maps TV channels from API response
     */
    private suspend fun mapTvChannels(response: TdtChannelsResponse?): List<Channel> =
        withContext(Dispatchers.Default) {
            response?.countries?.firstOrNull { it.name == SPAIN }?.ambits?.flatMap { ambit ->
                ambit.channels.mapNotNull { channel ->
                    channel.toChannel(ambitName = ambit.name)
                }
            } ?: emptyList()
        }

    /**
     * Extracts and maps radio channels from API response.
     * Only includes "Musicales" category to avoid overwhelming the app with local stations.
     */
    private suspend fun mapRadioChannels(response: TdtChannelsResponse?): List<Channel> =
        withContext(Dispatchers.Default) {
            response?.countries?.firstOrNull { it.name == SPAIN }?.ambits
                ?.filter { ambit ->
                    ambit.name.equals(AmbitConstants.MUSICALES, ignoreCase = true)
                }
                ?.flatMap { ambit ->
                    ambit.channels.mapNotNull { channel ->
                        channel.toChannel(ambitName = ambit.name)
                    }
                } ?: emptyList()
        }

    /**
     * Merges API channels with fallback channels, enriching with fallback logos when needed.
     * Deduplicates by URL and enriches empty logos from fallback.
     */
    private fun mergeWithFallback(fallback: List<Channel>, apiChannels: List<Channel>): List<Channel> {
        val fallbackMap = fallback.associateBy { it.name }
        return (fallback + apiChannels)
            .distinctBy { it.url }
            .map { channel ->
                val fallbackChannel = fallbackMap[channel.name]
                if (channel.logo.isEmpty() && fallbackChannel != null && fallbackChannel.logo.isNotEmpty()) {
                    channel.copy(logo = fallbackChannel.logo)
                } else {
                    channel
                }
            }
    }
}
