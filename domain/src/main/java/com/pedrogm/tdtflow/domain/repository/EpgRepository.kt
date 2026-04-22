package com.pedrogm.tdtflow.domain.repository

import com.pedrogm.tdtflow.domain.model.Program
import kotlinx.coroutines.flow.Flow

interface EpgRepository {
    fun getNowPlaying(channelUrl: String): Flow<Program?>
}
