package pe.edu.upc.viora.core.presentation

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Test

class ThousandsVisualTransformationTest {

    private fun show(raw: String) = ThousandsVisualTransformation.filter(AnnotatedString(raw))

    @Test
    fun `groups the whole part with a no-break space`() {
        assertEquals("12\u00A0500", show("12500").text.text)
        assertEquals("125\u00A0000", show("125000").text.text)
        assertEquals("1\u00A0250\u00A0000,5", show("1250000,5").text.text)
        assertEquals("850", show("850").text.text)
    }

    @Test
    fun `leaves what is not a plain number as typed`() {
        assertEquals("-300", show("-300").text.text)
        assertEquals("", show("").text.text)
    }

    @Test
    fun `the cursor keeps its place across the separators`() {
        val mapping = show("12500").offsetMapping
        assertEquals(0, mapping.originalToTransformed(0))
        assertEquals(2, mapping.originalToTransformed(2))
        assertEquals(4, mapping.originalToTransformed(3))
        assertEquals(6, mapping.originalToTransformed(5))
        (0..5).forEach { assertEquals(it, mapping.transformedToOriginal(mapping.originalToTransformed(it))) }
    }
}
