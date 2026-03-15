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
     * Carga canales desde la API de TdtChannels y los combina con los de reserva.
     */
    suspend fun loadChannels(): List<Channel> = withContext(Dispatchers.IO) {
        val fallback = fallbackChannels()
        try {
            Log.d("ChannelRepository", "Loading channels from JSON list...")
            val response = NetworkModule.service.getChannels()
            
            // Aplanamos la lista de ambitos -> canales
            val apiChannels = withContext(Dispatchers.Default) {
                response.ambits.flatMap { ambit ->
                    ambit.channels.mapNotNull { it.toChannel() }
                }
            }
            
            Log.d("ChannelRepository", "Mapped ${apiChannels.size} valid channels from JSON")
            
            // Combinamos: Fallback (favoritos asegurados) + API (resto)
            // Usamos distinctBy por URL para no duplicar
            val combined = (fallback + apiChannels).distinctBy { it.url }
            
            Log.d("ChannelRepository", "Total combined channels: ${combined.size}")
            combined
        } catch (e: Exception) {
            Log.e("ChannelRepository", "Error loading JSON channels: ${e.message}", e)
            Log.d("ChannelRepository", "Using only ${fallback.size} fallback channels")
            fallback
        }
    }
}
