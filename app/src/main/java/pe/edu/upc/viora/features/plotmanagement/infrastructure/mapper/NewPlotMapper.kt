package pe.edu.upc.viora.features.plotmanagement.infrastructure.mapper

import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotChanges
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.CreatePlotRequestDto
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.UpdatePlotRequestDto

fun NewPlot.toRequestDto(): CreatePlotRequestDto = CreatePlotRequestDto(
    name = name.value,
    variety = variety.name,
    polygonGeoJson = GeoJsonPolygonWriter.write(outline.corners),
    rowSpacingM = frame.rowSpacingMeters,
    treeSpacingM = frame.treeSpacingMeters,
)

/** The `PUT` body for [changes]: what is not being changed is sent back as the plot already has it. */
fun Plot.toUpdateRequestDto(changes: PlotChanges): UpdatePlotRequestDto = UpdatePlotRequestDto(
    name = changes.name.value,
    variety = changes.variety.name,
    rowSpacingM = changes.frame.rowSpacingMeters,
    treeSpacingM = changes.frame.treeSpacingMeters,
    lastPruningDate = lastPruningDate?.toString(),
    polygonGeoJson = GeoJsonPolygonWriter.write(changes.outline?.corners ?: outline),
)
