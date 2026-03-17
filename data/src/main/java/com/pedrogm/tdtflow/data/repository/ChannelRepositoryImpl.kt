package com.pedrogm.tdtflow.data.repository

import android.util.Log
import com.pedrogm.tdtflow.data.remote.AmbitConstants
import com.pedrogm.tdtflow.data.remote.NetworkModule
import com.pedrogm.tdtflow.data.remote.toChannel
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.repository.ChannelRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
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
            Log.d("ChannelRepository", "Loading channels from TDTChannels API...")
            
            // Cargamos TV y Radio en paralelo
            val tvDeferred = async { 
                runCatching { NetworkModule.service.getChannels() }.getOrNull() 
            }
            val radioDeferred = async { 
                runCatching { NetworkModule.service.getRadioChannels() }.getOrNull() 
            }
            
            val tvResponse = tvDeferred.await()
            val radioResponse = radioDeferred.await()
            
            // Obtenemos España de cada respuesta
            val spainTv = tvResponse?.countries?.firstOrNull { it.name == "Spain" }
            val spainRadio = radioResponse?.countries?.firstOrNull { it.name == "Spain" }
            
            if (spainTv == null && spainRadio == null) {
                Log.w("ChannelRepository", "Spain not found in responses, using fallback")
                return@withContext fallback
            }
            
            // Aplanamos los canales de TV
            val tvChannels = withContext(Dispatchers.Default) {
                spainTv?.ambits?.flatMap { ambit ->
                    ambit.channels.mapNotNull { channel ->
                        channel.toChannel(ambitName = ambit.name)
                    }
                } ?: emptyList()
            }
            
            // Aplanamos las emisoras de Radio (solo Musicales para empezar)
            val radioChannels = withContext(Dispatchers.Default) {
                spainRadio?.ambits
                    ?.filter { ambit ->
                        // Solo cargamos Musicales (hay muchas emisoras locales que saturarían)
                        ambit.name.equals(AmbitConstants.MUSICALES, ignoreCase = true)
                    }
                    ?.flatMap { ambit ->
                        ambit.channels.mapNotNull { channel ->
                            channel.toChannel(ambitName = ambit.name)
                        }
                    } ?: emptyList()
            }
            
            Log.d("ChannelRepository", "Mapped ${tvChannels.size} TV + ${radioChannels.size} radio channels")
            
            val apiChannels = tvChannels + radioChannels
            
            // Combinamos: Fallback + API, eliminando duplicados por URL
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
            Log.e("ChannelRepository", "Error loading TDTChannels: ${e.message}", e)
            Log.d("ChannelRepository", "Using only ${fallback.size} fallback channels")
            fallback
        }
    }
}
