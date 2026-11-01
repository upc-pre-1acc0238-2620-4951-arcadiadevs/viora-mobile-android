package pe.edu.upc.viora.features.telemetry.infrastructure

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.edu.upc.viora.features.telemetry.domain.entity.NewSensorNode
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorNode
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorStatus
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorType
import pe.edu.upc.viora.features.telemetry.infrastructure.mapper.toCalibrationRequestDto
import pe.edu.upc.viora.features.telemetry.infrastructure.mapper.toDomainOrNull
import pe.edu.upc.viora.features.telemetry.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.telemetry.infrastructure.mapper.toRequestDto
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.SensorNodeDto

class SensorNodeMapperTest {

    private val node = SensorNode(
        id = "n1",
        plotId = "p1",
        name = "Sonda Sur",
        type = SensorType.SONDA_SUELO,
        depthCm = 60,
        status = SensorStatus.ACTIVE,
        lastReadingAt = null,
        lastTemperatureCelsius = null,
        lastHumidityPercent = null,
    )

    @Test
    fun `saving the node sends the name and Transmitir lecturas, so the server pauses or resumes it`() {
        val paused = node.copy(status = SensorStatus.PAUSED).toCalibrationRequestDto()
        assertEquals("Sonda Sur", paused.name)
        assertEquals(false, paused.transmitting)

        assertEquals(true, node.toCalibrationRequestDto().transmitting)
    }

    @Test
    fun `the day of the pause the server returns reaches the screen, and an active node has none`() {
        val dto = SensorNodeDto(
            id = "n1",
            plotId = "p1",
            name = "Sonda Sur",
            type = "SOIL_PROBE",
            depthCm = 60,
            status = "PAUSED",
            pausedAt = "2026-10-20T14:00:00Z",
        )

        assertEquals("2026-10-20T14:00:00Z", dto.toEntity().toDomainOrNull()?.pausedAt)
        assertNull(dto.copy(status = "ACTIVE", pausedAt = null).toEntity().toDomainOrNull()?.pausedAt)
    }

    @Test
    fun `the nodes of the backend are read with their english types, and linking sends them back in english`() {
        val dto = SensorNodeDto(id = "n1", plotId = "p1", name = "Estación", type = "MICROCLIMATE", status = "ACTIVE")
        assertEquals(SensorType.MICROCLIMA, dto.toEntity().toDomainOrNull()?.type)
        assertEquals(SensorType.SONDA_SUELO, dto.copy(type = "SOIL_PROBE").toEntity().toDomainOrNull()?.type)
        // A row cached by an older version keeps the app's name
        assertEquals(SensorType.SONDA_SUELO, dto.copy(type = "SONDA_SUELO").toEntity().toDomainOrNull()?.type)

        assertEquals("SOIL_PROBE", NewSensorNode("p1", "Sonda", SensorType.SONDA_SUELO, 30).toRequestDto().type)
        assertEquals("MICROCLIMATE", NewSensorNode("p1", "Nodo", SensorType.MICROCLIMA, null).toRequestDto().type)
    }
}
