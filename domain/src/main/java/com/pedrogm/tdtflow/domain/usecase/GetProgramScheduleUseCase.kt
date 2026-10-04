package com.pedrogm.tdtflow.domain.usecase

import com.pedrogm.tdtflow.domain.model.Program
import com.pedrogm.tdtflow.domain.repository.EpgRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProgramScheduleUseCase @Inject constructor(
    private val repository: EpgRepository
) {
    operator fun invoke(epgId: String): Flow<List<Program>> =
        repository.getSchedule(epgId)
}
