package pe.edu.upc.viora.features.plotmanagement.infrastructure

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.infrastructure.mapper.GeoJsonPolygonParser
import pe.edu.upc.viora.features.plotmanagement.infrastructure.mapper.GeoJsonPolygonWriter

class GeoJsonPolygonWriterTest {

    private val corners = listOf(
        GeoPoint(latitude = -18.05, longitude = -70.25),
        GeoPoint(latitude = -18.05, longitude = -70.24),
        GeoPoint(latitude = -18.06, longitude = -70.24),
    )

    @Test
    fun `writes a closed ring in longitude latitude order`() {
        assertEquals(
            """{"type":"Polygon","coordinates":[[[-70.2500000,-18.0500000],[-70.2400000,-18.0500000],[-70.2400000,-18.0600000],[-70.2500000,-18.0500000]]]}""",
            GeoJsonPolygonWriter.write(corners),
        )
    }

    @Test
    fun `what the writer produces the parser reads back`() {
        assertEquals(corners, GeoJsonPolygonParser.parse(GeoJsonPolygonWriter.write(corners)))
    }

    @Test
    fun `tiny numbers never use scientific notation which the backend cannot parse`() {
        val json = GeoJsonPolygonWriter.write(
            listOf(GeoPoint(0.00001, 0.00002), GeoPoint(0.00001, 0.00004), GeoPoint(0.00003, 0.00004)),
        )

        assertFalse(json.contains("E", ignoreCase = true).and(json.contains("E-")))
        assertEquals(true, json.contains("[0.0000200,0.0000100]"))
    }

    @Test
    fun `uses a dot as decimal separator whatever the device language is`() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("es-PE"))
            assertFalse(GeoJsonPolygonWriter.write(corners).contains("-70,25"))
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun `fewer than three corners cannot be written`() {
        assertThrows(IllegalArgumentException::class.java) { GeoJsonPolygonWriter.write(corners.take(2)) }
    }
}
