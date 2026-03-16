package com.pedrogm.tdtflow.data.repository

import android.util.Log
import com.pedrogm.tdtflow.data.remote.TdtChannelsService
import com.pedrogm.tdtflow.data.remote.toChannel
import com.pedrogm.tdtflow.domain.model.Channel
import com.pedrogm.tdtflow.domain.repository.ChannelRepository
import dagger.Reusable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Reusable
class ChannelRepositoryImpl @Inject constructor(
    private val service: TdtChannelsService
) : ChannelRepository {

    override fun getChannels(): Flow<List<Channel>> = flow {
        emit(loadChannels())
    }

    private suspend fun loadChannels(): List<Channel> = withContext(Dispatchers.IO) {
        val fallback = fallbackChannels()
        try {
            Log.d("ChannelRepository", "Loading channels from JSON list...")
            val response = service.getChannels()
            
            // Aplanamos la lista de ambitos -> canales
            val apiChannels = withContext(Dispatchers.Default) {
                response.ambits.flatMap { ambit ->
                    ambit.channels.mapNotNull { it.toChannel() }
                }
            }
            
            Log.d("ChannelRepository", "Mapped ${apiChannels.size} valid channels from JSON")
            
            // Combinamos: Fallback (favoritos asegurados) + API (resto)
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
