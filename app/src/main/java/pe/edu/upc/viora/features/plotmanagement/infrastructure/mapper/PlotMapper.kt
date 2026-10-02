package pe.edu.upc.viora.features.plotmanagement.infrastructure.mapper

import android.util.Log
import java.time.LocalDate
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.plotmanagement.infrastructure.local.PlotEntity
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.PlotDto

private const val TAG = "PlotMapper"
private const val ACTIVE = "ACTIVE"

fun PlotDto.toEntity(): PlotEntity = PlotEntity(
    id = id,
    name = name,
    variety = variety,
    areaHa = areaHa,
    treeDensity = treeDensity,
    rowSpacingM = rowSpacingM,
    treeSpacingM = treeSpacingM,
    polygonGeoJson = polygonGeoJson,
    lastPruningDate = lastPruningDate,
    status = status,
    revision = revision,
)

/**
 * Converts a cached row to the domain type, or `null` when the row cannot be represented
 * (blank id or an unknown variety). One bad row must not blank the whole list, so callers
 * skip it. A broken outline or date only degrades that field: the outline is decorative.
 */
fun PlotEntity.toDomainOrNull(): Plot? = try {
    Plot(
        id = PlotId(id),
        name = name,
        variety = OliveVariety.valueOf(variety),
        areaHectares = areaHa,
        treesPerHectare = treeDensity,
        rowSpacingMeters = rowSpacingM,
        treeSpacingMeters = treeSpacingM,
        outline = runCatching { GeoJsonPolygonParser.parse(polygonGeoJson) }.getOrDefault(emptyList()),
        lastPruningDate = lastPruningDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        isActive = status == ACTIVE,
        revision = revision,
    )
} catch (e: IllegalArgumentException) {
    Log.w(TAG, "Skipping plot $id: ${e.message}")
    null
}
