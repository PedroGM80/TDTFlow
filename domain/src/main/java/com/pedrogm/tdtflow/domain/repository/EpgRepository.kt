package com.pedrogm.tdtflow.domain.repository

import com.pedrogm.tdtflow.domain.model.Program
import kotlinx.coroutines.flow.Flow

interface EpgRepository {
    fun getNowPlaying(epgId: String): Flow<Program?>
    fun getSchedule(epgId: String): Flow<List<Program>>
}
