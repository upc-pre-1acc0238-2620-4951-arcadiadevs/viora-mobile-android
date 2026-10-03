package pe.edu.upc.viora.features.plotmanagement.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GpsFix
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GpsSignal

class GpsSignalTest {

    @Test
    fun `up to five metres is a good signal`() {
        assertEquals(GpsSignal.GOOD, GpsSignal.of(0.0))
        assertEquals(GpsSignal.GOOD, GpsSignal.of(3.0))
        assertEquals(GpsSignal.GOOD, GpsSignal.of(5.0))
    }

    @Test
    fun `between five and fifteen metres is fair and still lets a corner be marked`() {
        assertEquals(GpsSignal.FAIR, GpsSignal.of(5.1))
        assertEquals(GpsSignal.FAIR, GpsSignal.of(15.0))
        assertTrue(GpsSignal.FAIR.allowsMarking)
    }

    @Test
    fun `above fifteen metres the signal is weak and no corner can be marked`() {
        assertEquals(GpsSignal.WEAK, GpsSignal.of(15.1))
        assertEquals(GpsSignal.WEAK, GpsSignal.of(30.0))
        assertFalse(GpsSignal.WEAK.allowsMarking)
    }

    @Test
    fun `above thirty metres it counts as no GPS at all`() {
        assertEquals(GpsSignal.LOST, GpsSignal.of(30.1))
        assertEquals(GpsSignal.LOST, GpsSignal.of(40.0))
        assertFalse(GpsSignal.LOST.allowsMarking)
    }

    @Test
    fun `a fix knows the signal of its accuracy`() {
        assertEquals(GpsSignal.GOOD, GpsFix(GeoPoint(-18.0, -70.0), accuracyMeters = 3.0).signal)
        assertEquals(GpsSignal.WEAK, GpsFix(GeoPoint(-18.0, -70.0), accuracyMeters = 20.0).signal)
    }

    @Test
    fun `an accuracy cannot be negative`() {
        assertThrows(IllegalArgumentException::class.java) { GpsFix(GeoPoint(-18.0, -70.0), accuracyMeters = -1.0) }
    }
}
