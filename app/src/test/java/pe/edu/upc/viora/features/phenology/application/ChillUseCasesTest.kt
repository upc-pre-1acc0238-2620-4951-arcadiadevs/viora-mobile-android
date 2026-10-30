package pe.edu.upc.viora.features.phenology.application

import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.phenology.chillTracker
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveChillTrackerUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveCurrentPhaseUseCase
import pe.edu.upc.viora.features.phenology.domain.OlivePhase
import pe.edu.upc.viora.features.phenology.domain.PlotPhaseDates
import pe.edu.upc.viora.features.phenology.application.usecase.RefreshChillTrackerUseCase
import pe.edu.upc.viora.features.phenology.domain.entity.ChillTracker
import pe.edu.upc.viora.features.phenology.domain.repository.ChillRepository

private class FakeChillRepository : ChillRepository {
    val trackerFlow = MutableStateFlow<ChillTracker?>(null)
    var refreshCalled = false

    override fun observeChillTracker(plotId: String): Flow<ChillTracker?> = trackerFlow

    override fun observeLastSync(plotId: String): Flow<Instant?> = MutableStateFlow(null)

    override suspend fun refresh(plotId: String): AppResult<Unit> {
        refreshCalled = true
        return AppResult.Success(Unit)
    }
}

class ChillUseCasesTest {

    private val fakeRepo = FakeChillRepository()
    private val observeUseCase = ObserveChillTrackerUseCase(fakeRepo)
    private val refreshUseCase = RefreshChillTrackerUseCase(fakeRepo)

    @Test
    fun observeUseCaseDelegatesToRepository() = runTest {
        val tracker = chillTracker(accumulated = 20.0)
        fakeRepo.trackerFlow.value = tracker

        val emitted = observeUseCase("p-1").first()
        assertNotNull(emitted)
        assertEquals(20.0, emitted!!.accumulatedPortions, 0.001)
    }

    @Test
    fun refreshUseCaseDelegatesToRepository() = runTest {
        val result = refreshUseCase("p-1")
        assertTrue(result is AppResult.Success)
        assertTrue(fakeRepo.refreshCalled)
    }

    private fun observePhaseOn(day: LocalDate) = ObserveCurrentPhaseUseCase(
        fakeRepo,
        Clock.fixed(day.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC),
    )

    @Test
    fun `the current phase follows the plot's chill and its own thinning window`() = runTest {
        val today = LocalDate.of(2026, 11, 2)
        val dates = PlotPhaseDates(thinningOpensOn = LocalDate.of(2026, 10, 30), thinningClosesOn = LocalDate.of(2026, 11, 25))
        fakeRepo.trackerFlow.value = chillTracker(accumulated = 20.0)

        val phase = observePhaseOn(today)("plot-1", dates).first()

        assertEquals(OlivePhase.THINNING, phase.current)
        assertEquals(LocalDate.of(2026, 11, 25), phase.window(OlivePhase.THINNING).endsOn)
        assertEquals(20.0, phase.chill!!.accumulatedPortions, 0.001)
    }

    @Test
    fun `without a plot the usual calendar is used and no chill is read`() = runTest {
        val phase = observePhaseOn(LocalDate.of(2026, 11, 2))(null).first()

        assertEquals(OlivePhase.FRUIT_SET, phase.current)
        assertEquals(null, phase.chill)
    }
}
