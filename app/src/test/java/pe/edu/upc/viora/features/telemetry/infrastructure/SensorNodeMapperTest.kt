package pe.edu.upc.viora.features.telemetry.infrastructure

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.viora.features.telemetry.domain.entity.NewSensorNode
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorType
import pe.edu.upc.viora.features.telemetry.infrastructure.mapper.toDomainOrNull
import pe.edu.upc.viora.features.telemetry.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.telemetry.infrastructure.mapper.toRequestDto
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.SensorNodeDto

class SensorNodeMapperTest {

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
