package pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint

class OutlineFramingTest {
    private val square = listOf(
        GeoPoint(latitude = -18.050, longitude = -70.250),
        GeoPoint(latitude = -18.050, longitude = -70.249),
        GeoPoint(latitude = -18.051, longitude = -70.249),
        GeoPoint(latitude = -18.051, longitude = -70.250),
    )

    @Test
    fun `the camera is centred on the middle of the outline`() {
        val framing = frameOutline(square, widthDp = 300f, heightDp = 200f, paddingDp = 20f)

        assertNotNull(framing)
        assertEquals(-18.0505, framing!!.center.latitude, 1e-9)
        assertEquals(-70.2495, framing.center.longitude, 1e-9)
    }

    @Test
    fun `a plot of about a hundred metres is framed at a street level zoom, not the whole planet`() {
        val framing = frameOutline(square, widthDp = 300f, heightDp = 200f, paddingDp = 20f)!!

        assertTrue("zoom was ${framing.zoom}", framing.zoom in 15.0..19.0)
    }

    @Test
    fun `a bigger plot is framed further out`() {
        val big = square.map { GeoPoint(it.latitude * 1.0, it.longitude) } + GeoPoint(-18.06, -70.24)

        assertTrue(frameOutline(big, 300f, 200f, 20f)!!.zoom < frameOutline(square, 300f, 200f, 20f)!!.zoom)
    }

    @Test
    fun `there is nothing to frame without corners`() {
        assertNull(frameOutline(emptyList(), 300f, 200f, 20f))
    }
}
