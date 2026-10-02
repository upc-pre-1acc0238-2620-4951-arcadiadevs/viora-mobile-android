package pe.edu.upc.viora.features.plotmanagement.infrastructure.mapper

import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.CreatePlotRequestDto

fun NewPlot.toRequestDto(): CreatePlotRequestDto = CreatePlotRequestDto(
    name = name.value,
    variety = variety.name,
    polygonGeoJson = GeoJsonPolygonWriter.write(outline.corners),
    rowSpacingM = frame.rowSpacingMeters,
    treeSpacingM = frame.treeSpacingMeters,
)
