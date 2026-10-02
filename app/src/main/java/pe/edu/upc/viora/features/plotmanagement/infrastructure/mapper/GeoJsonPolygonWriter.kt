package pe.edu.upc.viora.features.plotmanagement.infrastructure.mapper

import java.math.BigDecimal
import java.math.RoundingMode
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint

/** Writes the corners of a plot as a GeoJSON `Polygon` string (RFC 7946, `[longitude, latitude]`). */
object GeoJsonPolygonWriter {

    private const val DECIMALS = 7 // about 1 cm: more than any phone GPS or map tap can give

    /**
     * The ring is closed by repeating the first corner, as the backend requires. Numbers are
     * written in plain decimal notation because the backend parses them with a regex that does
     * not accept exponents such as `1.0E-5`, and never with the device's decimal comma.
     */
    fun write(corners: List<GeoPoint>): String {
        require(corners.size >= 3) { "A polygon needs at least 3 corners" }
        val ring = (corners + corners.first()).joinToString(separator = ",") { point ->
            "[${plain(point.longitude)},${plain(point.latitude)}]"
        }
        return """{"type":"Polygon","coordinates":[[$ring]]}"""
    }

    private fun plain(value: Double): String =
        BigDecimal.valueOf(value).setScale(DECIMALS, RoundingMode.HALF_UP).toPlainString()
}
