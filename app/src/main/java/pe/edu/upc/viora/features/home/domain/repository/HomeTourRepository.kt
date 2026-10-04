package pe.edu.upc.viora.features.home.domain.repository

import kotlinx.coroutines.flow.Flow

/** Remembers whether the producer already went through (or dismissed) the Home tour. */
interface HomeTourRepository {

    /** `true` once the tour was finished, skipped or dismissed. */
    val hasSeenTour: Flow<Boolean>

    suspend fun markSeen()

    /** Lets the tour show again (the "Recorrido del Inicio" entry in the account screen). */
    suspend fun reset()
}
