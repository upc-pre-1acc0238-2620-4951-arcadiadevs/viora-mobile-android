package pe.edu.upc.viora.features.home.domain.repository

import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.features.home.domain.valueobject.HomeTour

/** Remembers which Home tours the producer already went through (or dismissed). */
interface HomeTourRepository {

    /** The tours finished, skipped or dismissed on this account. */
    val seenTours: Flow<Set<HomeTour>>

    suspend fun markSeen(tour: HomeTour)

    /** Lets both tours show again (the "Recorrido del Inicio" entry in the account screen). */
    suspend fun reset()
}
