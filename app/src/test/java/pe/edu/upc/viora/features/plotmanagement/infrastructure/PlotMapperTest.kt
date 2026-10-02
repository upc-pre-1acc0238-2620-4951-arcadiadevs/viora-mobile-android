package pe.edu.upc.viora.features.plotmanagement.infrastructure

import java.time.LocalDate
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.infrastructure.mapper.GeoJsonPolygonParser
import pe.edu.upc.viora.features.plotmanagement.infrastructure.mapper.toDomainOrNull
import pe.edu.upc.viora.features.plotmanagement.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.PlotDto

class PlotMapperTest {

    // Same ring the backend returns for a real plot (closed: first vertex repeated at the end).
    private val polygon =
        """{"type":"Polygon","coordinates":[[[-70.25,-18.05],[-70.24,-18.05],[-70.24,-18.06],[-70.25,-18.06],[-70.25,-18.05]]]}"""

    // Verbatim shape of GET /api/v1/plots on the deployed backend, with an extra unknown field.
    private val responseJson = """
        [{"id":"e07fc555-060b-4034-843d-ebcc0e76b1e2","producerId":"550e8400-e29b-41d4-a716-446655440000",
          "name":"Cuartel San Jerónimo","variety":"CRIOLLA","areaHa":117.82,"treeDensity":286,
          "rowSpacingM":7.0,"treeSpacingM":5.0,"polygonGeoJson":"${polygon.replace("\"", "\\\"")}",
          "lastPruningDate":null,"status":"ACTIVE","revision":3,"somethingNew":true}]
    """.trimIndent()

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    private fun dto(
        variety: String = "SEVILLANA",
        status: String = "ACTIVE",
        polygonGeoJson: String = polygon,
        lastPruningDate: String? = null,
    ) = PlotDto(
        id = "p-1",
        producerId = "u-1",
        name = "La Yarada 02",
        variety = variety,
        areaHa = 2.5,
        treeDensity = 72,
        rowSpacingM = 7.0,
        treeSpacingM = 5.0,
        polygonGeoJson = polygonGeoJson,
        lastPruningDate = lastPruningDate,
        status = status,
        revision = 1,
    )

    @Test
    fun `the real backend response deserializes ignoring unknown fields`() {
        val plots = json.decodeFromString<List<PlotDto>>(responseJson)

        assertEquals(1, plots.size)
        assertEquals("CRIOLLA", plots[0].variety)
        assertEquals(3L, plots[0].revision)
        assertNull(plots[0].lastPruningDate)
    }

    @Test
    fun `dto to entity to domain keeps every field`() {
        val plot = dto(lastPruningDate = "2026-06-15").toEntity().toDomainOrNull()

        assertNotNull(plot)
        plot!!
        assertEquals("p-1", plot.id.value)
        assertEquals("La Yarada 02", plot.name)
        assertEquals(OliveVariety.SEVILLANA, plot.variety)
        assertEquals(2.5, plot.areaHectares, 0.0)
        assertEquals(72, plot.treesPerHectare)
        assertEquals(LocalDate.of(2026, 6, 15), plot.lastPruningDate)
        assertEquals(1L, plot.revision)
        assertTrue(plot.isActive)
    }

    @Test
    fun `parser returns the ring without the closing vertex in latitude longitude order`() {
        val points = GeoJsonPolygonParser.parse(polygon)

        assertEquals(4, points.size)
        assertEquals(GeoPoint(latitude = -18.05, longitude = -70.25), points.first())
        assertEquals(GeoPoint(latitude = -18.06, longitude = -70.25), points.last())
    }

    @Test
    fun `parser rejects text that is not a polygon`() {
        assertThrows(Exception::class.java) { GeoJsonPolygonParser.parse("not json") }
        assertThrows(Exception::class.java) { GeoJsonPolygonParser.parse("""{"type":"Polygon"}""") }
    }

    @Test
    fun `a broken outline degrades to an empty outline instead of dropping the plot`() {
        val plot = dto(polygonGeoJson = "garbage").toEntity().toDomainOrNull()

        assertNotNull(plot)
        assertTrue(plot!!.outline.isEmpty())
    }

    @Test
    fun `an unreadable pruning date becomes null`() {
        val plot = dto(lastPruningDate = "yesterday").toEntity().toDomainOrNull()

        assertNull(plot!!.lastPruningDate)
    }

    @Test
    fun `a plot with an unknown variety is skipped`() {
        assertNull(dto(variety = "PICUAL").toEntity().toDomainOrNull())
    }

    @Test
    fun `inactive status maps to a non active plot`() {
        assertFalse(dto(status = "INACTIVE").toEntity().toDomainOrNull()!!.isActive)
    }
}
