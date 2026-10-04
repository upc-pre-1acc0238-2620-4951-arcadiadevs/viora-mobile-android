package pe.edu.upc.viora.features.home.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.home.domain.repository.HomeTourRepository

/** Whether the producer already saw the Home tour. */
class ObserveHomeTourSeenUseCase @Inject constructor(private val repository: HomeTourRepository) {
    operator fun invoke(): Flow<Boolean> = repository.hasSeenTour
}

/** The tour was finished, skipped or dismissed: it will not show again on its own. */
class CompleteHomeTourUseCase @Inject constructor(private val repository: HomeTourRepository) {
    suspend operator fun invoke() = repository.markSeen()
}

/** Lets the tour show again the next time the Home opens. */
class RestartHomeTourUseCase @Inject constructor(private val repository: HomeTourRepository) {
    suspend operator fun invoke() = repository.reset()
}
