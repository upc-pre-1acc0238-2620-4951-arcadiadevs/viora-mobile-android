package pe.edu.upc.viora.features.plotmanagement.domain.entity

import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlantationFrame
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotName
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotOutline

/**
 * A plot the producer wants to register. It only exists in a valid state: every field is a
 * value object that already checked its own rules, so the repository can send it as is.
 */
data class NewPlot(
    val name: PlotName,
    val variety: OliveVariety,
    val outline: PlotOutline,
    val frame: PlantationFrame,
)
