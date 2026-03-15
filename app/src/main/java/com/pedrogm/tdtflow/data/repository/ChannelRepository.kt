package com.pedrogm.tdtflow.data.repository

import android.util.Log
import com.pedrogm.tdtflow.data.fallbackChannels
import com.pedrogm.tdtflow.data.model.Channel
import com.pedrogm.tdtflow.data.remote.NetworkModule
import com.pedrogm.tdtflow.data.remote.toChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class ChannelRepository {

    /**
     * Emite canales como Flow.
     * Mantiene compatibilidad con ViewModel.
     */
    fun getChannelsFlow(): Flow<List<Channel>> = flow {
        emit(loadChannels())
    }

    /**
     * Carga canales desde la API de TdtChannels.
     * Si falla, usa fallbackChannels().
     */
    suspend fun loadChannels(): List<Channel> = withContext(Dispatchers.IO) {
        try {
            Log.d("ChannelRepository", "Loading channels from JSON list...")
            val response = NetworkModule.service.getChannels()
            
            // Aplanamos la lista de ambitos -> canales
            val channels = withContext(Dispatchers.Default) {
                response.ambits.flatMap { ambit ->
                    ambit.channels.mapNotNull { it.toChannel() }
                }
            }
            
            Log.d("ChannelRepository", "Mapped ${channels.size} valid channels from JSON")
            when {
                channels.isNotEmpty() -> {
                    channels
                }
                else -> {
                    Log.w("ChannelRepository", "JSON returned 0 valid channels, using fallback")
                    fallbackChannels()
                }
            }
        } catch (e: Exception) {
            Log.e("ChannelRepository", "Error loading JSON channels: ${e.message}", e)
            val fallback = fallbackChannels()
            Log.d("ChannelRepository", "Using ${fallback.size} fallback channels")
            fallback
        }
    }
}
