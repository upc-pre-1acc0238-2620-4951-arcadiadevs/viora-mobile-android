package pe.edu.upc.viora.features.home.domain.repository

import kotlinx.coroutines.flow.Flow

/** Remembers the plot the producer chose to see in the Home. */
interface FocusedPlotRepository {

    /** The id of the chosen plot, or null while the producer has not chosen one. */
    val chosenPlotId: Flow<String?>

    suspend fun choose(plotId: String)
}
