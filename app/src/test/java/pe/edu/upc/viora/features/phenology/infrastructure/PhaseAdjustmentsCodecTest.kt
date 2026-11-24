package pe.edu.upc.viora.features.phenology.infrastructure

import java.time.MonthDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.phenology.domain.OlivePhase
import pe.edu.upc.viora.features.phenology.domain.PhaseAdjustments
import pe.edu.upc.viora.features.phenology.infrastructure.local.PhaseAdjustmentsCodec

class PhaseAdjustmentsCodecTest {

    @Test
    fun `empty raw string decodes to empty map`() {
        assertTrue(PhaseAdjustmentsCodec.decode(null).isEmpty())
        assertTrue(PhaseAdjustmentsCodec.decode("").isEmpty())
        assertTrue(PhaseAdjustmentsCodec.decode("   ").isEmpty())
    }

    @Test
    fun `encodes and decodes adjustments preserving plot and phase values`() {
        val original = mapOf(
            "plot-1" to PhaseAdjustments(
                mapOf(
                    OlivePhase.BLOOM to MonthDay.of(7, 15),
                    OlivePhase.HARVEST to MonthDay.of(3, 20),
                ),
            ),
            "plot-2" to PhaseAdjustments(
                mapOf(
                    OlivePhase.THINNING to MonthDay.of(10, 28),
                ),
            ),
        )

        val encoded = PhaseAdjustmentsCodec.encode(original)
        val decoded = PhaseAdjustmentsCodec.decode(encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun `malformed parts are ignored without losing valid corrections`() {
        val raw = "plot-1|BLOOM=07-15,INVALID=xx,HARVEST=03-20;invalidpiece;plot-2|THINNING=10-28"
        val decoded = PhaseAdjustmentsCodec.decode(raw)

        assertEquals(2, decoded.size)
        assertEquals(MonthDay.of(7, 15), decoded["plot-1"]?.get(OlivePhase.BLOOM))
        assertEquals(MonthDay.of(3, 20), decoded["plot-1"]?.get(OlivePhase.HARVEST))
        assertEquals(MonthDay.of(10, 28), decoded["plot-2"]?.get(OlivePhase.THINNING))
    }
}
