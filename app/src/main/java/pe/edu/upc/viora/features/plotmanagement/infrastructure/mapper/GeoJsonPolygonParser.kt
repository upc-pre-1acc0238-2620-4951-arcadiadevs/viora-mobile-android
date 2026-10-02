package pe.edu.upc.viora.features.plotmanagement.infrastructure.mapper

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint

/** Reads the outer ring of a GeoJSON `Polygon` (RFC 7946: `[longitude, latitude]` positions). */
object GeoJsonPolygonParser {

    /**
     * Returns the outer ring without the repeated closing vertex.
     * Throws if the text is not a well-formed polygon.
     */
    fun parse(geoJson: String): List<GeoPoint> {
        val ring = Json.parseToJsonElement(geoJson)
            .jsonObject.getValue("coordinates")
            .jsonArray.first()
            .jsonArray
        val points = ring.map { position ->
            val pair = position.jsonArray
            GeoPoint(
                latitude = pair[1].jsonPrimitive.double,
                longitude = pair[0].jsonPrimitive.double,
            )
        }
        return if (points.size > 1 && points.first() == points.last()) points.dropLast(1) else points
    }
}
