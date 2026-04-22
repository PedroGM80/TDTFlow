package com.pedrogm.tdtflow.domain.usecase

import com.pedrogm.tdtflow.domain.model.Program
import com.pedrogm.tdtflow.domain.repository.EpgRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNowPlayingUseCase @Inject constructor(
    private val repository: EpgRepository
) {
    operator fun invoke(channelUrl: String): Flow<Program?> =
        repository.getNowPlaying(channelUrl)
}
