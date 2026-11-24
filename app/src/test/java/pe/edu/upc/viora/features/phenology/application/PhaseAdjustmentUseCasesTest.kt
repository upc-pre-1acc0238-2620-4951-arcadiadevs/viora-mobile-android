package pe.edu.upc.viora.features.phenology.application

import java.time.LocalDate
import java.time.MonthDay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.edu.upc.viora.features.phenology.FakePhaseAdjustmentsRepository
import pe.edu.upc.viora.features.phenology.application.usecase.AdjustPhaseStartUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.ResetPhaseStartUseCase
import pe.edu.upc.viora.features.phenology.domain.OlivePhase

class PhaseAdjustmentUseCasesTest {

    private val repository = FakePhaseAdjustmentsRepository()
    private val adjustUseCase = AdjustPhaseStartUseCase(repository)
    private val resetUseCase = ResetPhaseStartUseCase(repository)

    @Test
    fun `adjust phase start saves date in repository for plot`() = runTest {
        adjustUseCase("plot-1", OlivePhase.BLOOM, LocalDate.of(2026, 7, 15))

        val adjustments = repository.observe("plot-1").first()
        assertEquals(MonthDay.of(7, 15), adjustments[OlivePhase.BLOOM])
    }

    @Test
    fun `reset phase start clears adjusted date in repository for plot`() = runTest {
        adjustUseCase("plot-1", OlivePhase.BLOOM, LocalDate.of(2026, 7, 15))
        resetUseCase("plot-1", OlivePhase.BLOOM)

        val adjustments = repository.observe("plot-1").first()
        assertNull(adjustments[OlivePhase.BLOOM])
    }
}
