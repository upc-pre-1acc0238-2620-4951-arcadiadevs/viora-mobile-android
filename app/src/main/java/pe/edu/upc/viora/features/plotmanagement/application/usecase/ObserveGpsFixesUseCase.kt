package pe.edu.upc.viora.features.plotmanagement.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.plotmanagement.domain.service.LocationTracker
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GpsFix

/** The phone's GPS fixes while the producer walks the outline of a plot. */
class ObserveGpsFixesUseCase @Inject constructor(private val tracker: LocationTracker) {
    operator fun invoke(): Flow<GpsFix> = tracker.observeFixes()
}
