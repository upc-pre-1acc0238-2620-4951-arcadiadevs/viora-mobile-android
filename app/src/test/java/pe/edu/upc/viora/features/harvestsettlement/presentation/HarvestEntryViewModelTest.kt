package pe.edu.upc.viora.features.harvestsettlement.presentation

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObservePendingSettlementsUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObserveSettledHarvestsUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.RefreshSettledHarvestsUseCase
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.PendingStatus
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.HarvestEntryUiState
import pe.edu.upc.viora.features.harvestsettlement.presentation.viewmodel.HarvestEntryViewModel
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class HarvestEntryViewModelTest {

    private val clock = Clock.fixed(Instant.parse("2026-10-05T12:00:00Z"), ZoneOffset.UTC)
    private val settlements = FakeSettleRepository()
    private val plots = FakePlotsForSettle()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun state(): HarvestEntryUiState {
        val vm = HarvestEntryViewModel(
            observePlots = ObservePlotsUseCase(plots),
            observeSettlements = ObserveSettledHarvestsUseCase(settlements),
            observePending = ObservePendingSettlementsUseCase(settlements),
            refreshSettlements = RefreshSettledHarvestsUseCase(settlements),
            clock = clock,
        )
        vm.uiState.launchIn(CoroutineScope(Dispatchers.Main))
        return vm.uiState.value
    }

    @Test
    fun `every plot is por registrar when nothing is settled or queued`() = runTest {
        plots.cached.value = listOf(testPlot("p1", "La Yarada 02"), testPlot("p2", "Lote Norte", 1.5))

        val state = state()

        assertEquals(2026, state.campaignYear)
        assertEquals(listOf("p1", "p2"), state.plots.map { it.plotId })
        assertEquals(2, state.pendingCount)
        assertEquals(0, state.registeredCount)
        assertTrue(state.showCard)
    }

    @Test
    fun `a plot settled this year is excluded, one settled in another year is not`() = runTest {
        plots.cached.value = listOf(testPlot("p1", "La Yarada 02"), testPlot("p2", "Lote Norte"))
        settlements.cache.value = listOf(testSettlement("p1", 2026), testSettlement("p2", 2025))

        val state = state()

        assertEquals(listOf("p2"), state.plots.map { it.plotId })
        assertEquals(1, state.registeredCount)
    }

    @Test
    fun `plots with a PENDING or FAILED local settlement are excluded, a CONFLICT one is not`() = runTest {
        plots.cached.value = listOf(testPlot("p1", "A"), testPlot("p2", "B"), testPlot("p3", "C"), testPlot("p4", "D"))
        settlements.pending.value = listOf(
            testPending(testDraft("p1", 2026), PendingStatus.PENDING),
            testPending(testDraft("p2", 2026), PendingStatus.FAILED),
            testPending(testDraft("p3", 2026), PendingStatus.CONFLICT),
            testPending(testDraft("p4", 2025), PendingStatus.PENDING),
        )

        val state = state()

        assertEquals(listOf("p3", "p4"), state.plots.map { it.plotId })
    }

    @Test
    fun `the card hides when every plot is registered`() = runTest {
        plots.cached.value = listOf(testPlot("p1", "La Yarada 02"))
        settlements.cache.value = listOf(testSettlement("p1", 2026))

        val state = state()

        assertFalse(state.showCard)
        assertEquals(0, state.pendingCount)
        assertEquals(1, state.registeredCount)
    }

    @Test
    fun `the card hides when the producer has no plots`() = runTest {
        assertFalse(state().showCard)
    }

    @Test
    fun `the settlements are refreshed with the plot ids and a failed refresh still shows the card`() = runTest {
        plots.cached.value = listOf(testPlot("p1", "La Yarada 02"), testPlot("p2", "Lote Norte"))
        settlements.refreshResult = AppResult.Failure(AppError.Offline)

        val state = state()

        assertEquals(listOf(listOf("p1", "p2")), settlements.refreshedIds)
        assertEquals(2, state.pendingCount)
    }
}
