package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.DefaultMapCenter
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.walkingCamera

class WalkingCameraTest {

    private val producer = GeoPoint(-18.0958, -70.3167)

    // About 40 m east and 40 m south of the producer.
    private val east = GeoPoint(-18.0958, -70.3163)
    private val southEast = GeoPoint(-18.0962, -70.3163)

    @Test
    fun `with no position and no corners it opens on the given centre`() {
        val camera = walkingCamera(emptyList(), null, GeoPoint(-18.2, -70.4), 400f, 600f)

        assertEquals(GeoPoint(-18.2, -70.4), camera.center)
    }

    @Test
    fun `with nothing at all it falls back to the olive valley`() {
        assertEquals(DefaultMapCenter, walkingCamera(emptyList(), null, null, 400f, 600f).center)
    }

    @Test
    fun `with only the producer it looks at them, close up`() {
        val camera = walkingCamera(emptyList(), producer, null, 400f, 600f)

        assertEquals(producer, camera.center)
        assertEquals(18.5, camera.zoom, 0.0)
    }

    @Test
    fun `with corners it frames them together with the producer`() {
        val camera = walkingCamera(listOf(east, southEast), producer, null, 400f, 600f)

        // The middle of the three points, not the producer.
        assertEquals((-18.0958 + -18.0962) / 2, camera.center.latitude, 1e-9)
        assertEquals((-70.3167 + -70.3163) / 2, camera.center.longitude, 1e-9)
        assertTrue("zoom was ${camera.zoom}", camera.zoom in 16.0..19.0)
    }

    @Test
    fun `it never zooms in past the closest level of the framing even when everything is on one spot`() {
        val camera = walkingCamera(listOf(producer), producer, null, 400f, 600f)

        assertEquals(19.0, camera.zoom, 0.0)
    }

    @Test
    fun `corners too far away to keep in view do not make the producer disappear`() {
        val farAway = GeoPoint(-18.15, -70.2)

        val camera = walkingCamera(listOf(farAway), producer, null, 400f, 600f)

        assertEquals(producer, camera.center)
        assertEquals(16.0, camera.zoom, 0.0)
    }
}
