package pe.edu.upc.viora.features.phenology.presentation

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveAlternationSummaryUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveBearingIndexUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveHarvestHistoryUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveHarvestLastRefreshUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RefreshHarvestHistoryUseCase
import pe.edu.upc.viora.features.phenology.presentation.state.PlotAlternation
import pe.edu.upc.viora.features.phenology.presentation.viewmodel.PlotAlternationViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class PlotAlternationViewModelTest {

    private val harvests = FakeHarvestRepository()
    private val clock = Clock.fixed(Instant.parse("2026-10-04T12:00:00Z"), ZoneOffset.UTC)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = PlotAlternationViewModel(
        observeAlternation = ObserveAlternationSummaryUseCase(
            ObserveHarvestHistoryUseCase(harvests), ObserveBearingIndexUseCase(harvests), ObserveHarvestLastRefreshUseCase(harvests),
        ),
        refreshHarvests = RefreshHarvestHistoryUseCase(harvests),
        clock = clock,
    )

    @Test
    fun `the harvests of the plot shown are refreshed once per plot`() = runTest {
        val vm = viewModel()

        vm.show("y", 2.5)
        vm.show("y", 2.5)

        assertEquals(1, harvests.refreshCalls)
    }

    @Test
    fun `the card has nothing to say until the harvests are known`() = runTest {
        val vm = viewModel()

        vm.show("y", 2.5)

        assertNull(vm.alternation.value)
    }

    @Test
    fun `with fewer than three campaigns the card asks for the missing ones`() = runTest {
        harvests.records.value = figmaRecords().takeLast(1)
        harvests.lastRefresh.value = Instant.parse("2026-10-01T00:00:00Z")
        val vm = viewModel()

        vm.show("y", 2.5)

        assertEquals(PlotAlternation.Insufficient(missing = 2), vm.alternation.value)
    }

    @Test
    fun `with enough campaigns the card shows the last ones oldest first`() = runTest {
        harvests.records.value = figmaRecords()
        harvests.lastRefresh.value = Instant.parse("2026-10-01T00:00:00Z")
        val vm = viewModel()

        vm.show("y", 2.5)
        val alternation = vm.alternation.value as PlotAlternation.Ready

        assertEquals(listOf(2022, 2023, 2024, 2025), alternation.records.map { it.campaignYear })
        assertEquals(2.5, alternation.areaHectares, 0.0)
    }
}
