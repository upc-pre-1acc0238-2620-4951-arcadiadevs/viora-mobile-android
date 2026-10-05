package pe.edu.upc.viora.features.phenology.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.viora.features.phenology.domain.entity.BbiClass

class BbiClassTest {

    @Test
    fun classifiesWithFigmaThresholds() {
        assertEquals(BbiClass.LOW, BbiClass.of(0.0))
        assertEquals(BbiClass.LOW, BbiClass.of(0.19))
        assertEquals(BbiClass.MODERATE, BbiClass.of(0.20))
        assertEquals(BbiClass.MODERATE, BbiClass.of(0.40))
        assertEquals(BbiClass.SEVERE, BbiClass.of(0.41))
        assertEquals(BbiClass.SEVERE, BbiClass.of(1.0))
    }
}
