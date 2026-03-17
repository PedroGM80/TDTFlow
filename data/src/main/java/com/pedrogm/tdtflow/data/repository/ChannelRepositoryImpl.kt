package com.pedrogm.tdtflow.data.repository

import android.util.Log
import com.pedrogm.tdtflow.data.remote.NetworkModule
import com.pedrogm.tdtflow.data.remote.toChannel
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.repository.ChannelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class ChannelRepositoryImpl : ChannelRepository {

    override fun getChannels(): Flow<List<Channel>> = flow {
        emit(loadChannels())
    }

    private suspend fun loadChannels(): List<Channel> = withContext(Dispatchers.IO) {
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
            // Cuando hay duplicados por nombre, usamos el logo del fallback si la API está vacía
            val fallbackMap = fallback.associateBy { it.name }
            val combined = (fallback + apiChannels).distinctBy { it.url }.map { channel ->
                val fallbackChannel = fallbackMap[channel.name]
                if (channel.logo.isEmpty() && fallbackChannel != null && fallbackChannel.logo.isNotEmpty()) {
                    channel.copy(logo = fallbackChannel.logo)
                } else {
                    channel
                }
            }
            
            Log.d("ChannelRepository", "Total combined channels: ${combined.size}")
            combined
        } catch (e: Exception) {
            Log.e("ChannelRepository", "Error loading JSON channels: ${e.message}", e)
            Log.d("ChannelRepository", "Using only ${fallback.size} fallback channels")
            fallback
        }
    }
}
