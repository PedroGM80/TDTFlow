package com.pedrogm.tdtflow.data.repository

import com.pedrogm.tdtflow.domain.model.Program
import com.pedrogm.tdtflow.domain.repository.EpgRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

class MockEpgRepositoryImpl : EpgRepository {

    private val titles = listOf(
        "Informativos", "Cine de Acción", "El Hormiguero", "Deportes Noche",
        "Documental Naturaleza", "Series TV", "Magazine Mañana", "Dibujos Animados"
    )

    override fun getNowPlaying(channelUrl: String): Flow<Program?> = flow {
        while (true) {
            val now = System.currentTimeMillis()
            val seed = channelUrl.hashCode().toLong()
            val random = Random(seed)
            
            // Determinar un programa "fijo" basado en la hora para que sea consistente
            val hourMillis = 3600_000L
            val currentHourStart = (now / hourMillis) * hourMillis
            
            val titleIndex = (seed.coerceAtLeast(0) + (currentHourStart / hourMillis)) % titles.size
            
            emit(Program(
                title = titles[titleIndex.toInt()],
                startTime = currentHourStart,
                endTime = currentHourStart + hourMillis,
                channelUrl = channelUrl
            ))
            
            delay(60_000) // Update every minute
        }
    }
}
