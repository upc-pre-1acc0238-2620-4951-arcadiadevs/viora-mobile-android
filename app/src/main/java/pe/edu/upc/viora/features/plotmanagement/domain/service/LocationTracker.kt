package pe.edu.upc.viora.features.plotmanagement.domain.service

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GpsFix

/** The phone's GPS, as a stream of fixes. */
interface LocationTracker {

    /**
     * Fixes as they arrive, about once a second, while the flow is collected; the GPS is switched
     * off again when collection stops. Needs the precise-location permission: without it the flow
     * fails with a [SecurityException].
     */
    fun observeFixes(): Flow<GpsFix>
}
