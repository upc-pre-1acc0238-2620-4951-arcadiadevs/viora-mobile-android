package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.accuracyRadiusDp

class GpsAccuracyRingTest {

    @Test
    fun `at the equator and zoom zero one dp is about 78 kilometres, so a 78 km accuracy is one dp`() {
        assertEquals(1.0, accuracyRadiusDp(accuracyMeters = 78_271.517, latitude = 0.0, zoom = 0.0), 1e-6)
    }

    @Test
    fun `each zoom level doubles the radius`() {
        val near = accuracyRadiusDp(5.0, latitude = -18.0, zoom = 18.0)
        val nearer = accuracyRadiusDp(5.0, latitude = -18.0, zoom = 19.0)

        assertEquals(2.0, nearer / near, 1e-9)
    }

    @Test
    fun `the radius is proportional to the accuracy`() {
        val good = accuracyRadiusDp(3.0, latitude = -18.0, zoom = 18.5)
        val bad = accuracyRadiusDp(30.0, latitude = -18.0, zoom = 18.5)

        assertEquals(10.0, bad / good, 1e-9)
    }

    @Test
    fun `at walking zoom a few metres are a few dozen dp`() {
        // About 0.2 m per dp at zoom 18.5 around Tacna, so 5 m are 25 dp.
        val radius = accuracyRadiusDp(5.0, latitude = -18.1, zoom = 18.5)

        assertTrue("was $radius", abs(radius - 25.0) < 1.0)
    }
}
