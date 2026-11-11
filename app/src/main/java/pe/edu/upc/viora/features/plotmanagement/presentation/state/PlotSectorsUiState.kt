package pe.edu.upc.viora.features.plotmanagement.presentation.state

import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.SectorChoice
import pe.edu.upc.viora.features.plotmanagement.domain.entity.SectorZone

/** P06b · Sector de tus lotes: the plots the member had before joining, and the sector chosen for each. */
data class PlotSectorsUiState(
    val loading: Boolean = true,
    val plots: List<Plot> = emptyList(),
    val sectors: List<SectorZone> = emptyList(),
    val managerName: String? = null,
    /** The answer for each plot, by plot id; a plot without one keeps «Elegir sector». */
    val choices: Map<String, SectorChoice> = emptyMap(),
    val saving: Boolean = false,
    /** Some sectors did not get through; those plots stay on screen to try again. */
    val saveFailed: Boolean = false,
    /** Saved or put off: the screen closes. */
    val done: Boolean = false,
) {
    val canSave: Boolean get() = choices.isNotEmpty() && !saving
}
