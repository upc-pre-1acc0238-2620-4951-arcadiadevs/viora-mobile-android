package pe.edu.upc.viora.features.plotmanagement.domain.entity

import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlantationFrame
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotName
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotOutline

/**
 * What the producer can change in a registered plot. A null [outline] keeps the plot's current outline.
 */
data class PlotChanges(
    val name: PlotName,
    val variety: OliveVariety,
    val frame: PlantationFrame,
    val outline: PlotOutline? = null,
)
