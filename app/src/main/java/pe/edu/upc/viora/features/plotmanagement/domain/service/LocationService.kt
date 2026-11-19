package pe.edu.upc.viora.features.plotmanagement.domain.service

import kotlinx.coroutines.flow.Flow

/** The device-wide location service, independent of the app's permission. */
interface LocationService {
    fun isEnabled(): Boolean
    fun observeEnabled(): Flow<Boolean>
}
