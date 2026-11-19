package pe.edu.upc.viora.features.assistant.presentation

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.viora.features.assistant.domain.entity.AnswerFact
import pe.edu.upc.viora.features.assistant.domain.entity.FactTone
import pe.edu.upc.viora.features.assistant.presentation.state.summaryLine

class SummaryLineTest {

    private fun fact(value: String, label: String) = AnswerFact(value, label, "", FactTone.PRIMARY)

    @Test
    fun `a figure is followed by what it counts`() {
        val line = summaryLine(
            listOf(fact("12", "días de ventana"), fact("34°", "máx. jueves"), fact("2", "alertas activas")),
        )

        assertEquals("12 días de ventana · 34° máx. jueves · 2 alertas activas", line)
    }

    @Test
    fun `no thinning window does not repeat the word window`() {
        val line = summaryLine(
            listOf(fact("Sin ventana", "ventana de aclareo"), fact("34°", "máx. jueves"), fact("0", "alertas activas")),
        )

        assertEquals("Sin ventana de aclareo · 34° máx. jueves · 0 alertas activas", line)
    }

    @Test
    fun `the overlap is matched regardless of case and only at the seam`() {
        assertEquals("Sin Ventana de aclareo", summaryLine(listOf(fact("Sin Ventana", "ventana de aclareo"))))
        assertEquals("No window thinning window", summaryLine(listOf(fact("No window", "thinning window"))))
    }

    @Test
    fun `a value that already says everything is not followed by a blank label`() {
        assertEquals("Sin datos", summaryLine(listOf(fact("Sin datos", ""))))
    }
}
