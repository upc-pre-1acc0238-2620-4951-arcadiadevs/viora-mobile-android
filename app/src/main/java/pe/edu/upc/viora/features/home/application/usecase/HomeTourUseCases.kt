package pe.edu.upc.viora.features.home.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.home.domain.repository.HomeTourRepository
import pe.edu.upc.viora.features.home.domain.valueobject.HomeTour

/** Which Home tours the producer already saw. */
class ObserveHomeTourSeenUseCase @Inject constructor(private val repository: HomeTourRepository) {
    operator fun invoke(): Flow<Set<HomeTour>> = repository.seenTours
}

/** The tour was finished, skipped or dismissed: it will not show again on its own. */
class CompleteHomeTourUseCase @Inject constructor(private val repository: HomeTourRepository) {
    suspend operator fun invoke(tour: HomeTour) = repository.markSeen(tour)
}

/** Lets the tours show again the next time the Home opens. */
class RestartHomeTourUseCase @Inject constructor(private val repository: HomeTourRepository) {
    suspend operator fun invoke() = repository.reset()
}
