package pe.edu.upc.viora.features.harvestsettlement.presentation

import androidx.lifecycle.SavedStateHandle
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.DiscardPendingSettlementUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObservePendingSettlementsUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObserveSettledHarvestsUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.RefreshSettledHarvestsUseCase
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.PendingStatus
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementSummary
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.CampaignClosedUiState
import pe.edu.upc.viora.features.harvestsettlement.presentation.viewmodel.CampaignClosedViewModel
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety

@OptIn(ExperimentalCoroutinesApi::class)
class CampaignClosedViewModelTest {

    private val clock = Clock.fixed(Instant.parse("2026-10-05T12:00:00Z"), ZoneOffset.UTC)
    private val repository = FakeSettleRepository()
    private val plots = FakePlotsForSettle()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        plots.cached.value = listOf(testPlot("p1", "La Yarada 02", hectares = 2.5))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): CampaignClosedViewModel {
        val vm = CampaignClosedViewModel(
            savedStateHandle = SavedStateHandle(mapOf("plotId" to "p1", "campaignYear" to 2026)),
            observePlot = ObservePlotUseCase(plots),
            observeSettled = ObserveSettledHarvestsUseCase(repository),
            observePending = ObservePendingSettlementsUseCase(repository),
            refreshSettled = RefreshSettledHarvestsUseCase(repository),
            discardPending = DiscardPendingSettlementUseCase(repository),
            clock = clock,
        )
        vm.uiState.launchIn(CoroutineScope(Dispatchers.Main))
        return vm
    }

    private fun closed(vm: CampaignClosedViewModel) = (vm.uiState.value as CampaignClosedUiState.Closed).receipt

    @Test
    fun `a settlement with receipt and weighing date builds the closed receipt`() = runTest {
        repository.cache.value = listOf(
            testSettlement("p1", 2026).copy(
                receiptNumber = "VR-26-0142",
                weighedOn = LocalDate.of(2026, 5, 14),
                commercialSizeGrade = "101/110",
            ),
        )

        val receipt = closed(viewModel())

        assertEquals("VR-26-0142", receipt.receiptNumber)
        assertEquals(LocalDate.of(2026, 5, 14), receipt.weighedOn)
        assertEquals(20_800.0, receipt.totalKg, 0.0)
        assertEquals(0.6, receipt.greenShare!!, 1e-9)
        assertEquals(8.32, receipt.tonnesPerHectare!!, 1e-9)
        assertEquals("101/110", receipt.calibre)
        assertEquals("La Yarada 02", receipt.plotName)
        assertEquals(OliveVariety.SEVILLANA, receipt.variety)
    }

    @Test
    fun `without receipt number or weighing date the settle moment stands in and there is no number`() = runTest {
        repository.cache.value = listOf(testSettlement("p1", 2026))

        val receipt = closed(viewModel())

        assertNull(receipt.receiptNumber)
        assertEquals(LocalDate.of(2026, 5, 12), receipt.weighedOn)
        assertNull(receipt.calibre)
    }

    @Test
    fun `a fruits per kilo count without a grade label maps to its grade`() = runTest {
        repository.cache.value = listOf(testSettlement("p1", 2026).copy(commercialFruitsPerKg = 105.5))

        assertEquals("101/110", closed(viewModel()).calibre)
    }

    @Test
    fun `a pending row builds the pending receipt from the draft`() = runTest {
        val draft = testDraft("p1", 2026).copy(commercialFruitsPerKg = 95.0)
        repository.pending.value = listOf(testPending(draft, PendingStatus.PENDING))

        val state = viewModel().uiState.value as CampaignClosedUiState.Pending

        assertEquals(false, state.failed)
        assertNull(state.receipt.receiptNumber)
        assertEquals(LocalDate.of(2026, 5, 14), state.receipt.weighedOn)
        assertEquals(20_800.0, state.receipt.totalKg, 0.0)
        assertEquals("91/100", state.receipt.calibre)
    }

    @Test
    fun `pending turns into closed when the row disappears and the settlement appears`() = runTest {
        repository.pending.value = listOf(testPending(testDraft("p1", 2026), PendingStatus.PENDING))
        val vm = viewModel()
        assertTrue(vm.uiState.value is CampaignClosedUiState.Pending)

        repository.cache.value = listOf(testSettlement("p1", 2026).copy(receiptNumber = "VR-26-0001"))
        repository.pending.value = emptyList()

        assertEquals("VR-26-0001", closed(vm).receiptNumber)
    }

    @Test
    fun `a conflict row shows the 409 state and acknowledging discards it`() = runTest {
        val summary = SettlementSummary(2026, 18_000.0, "VR-26-0007", LocalDate.of(2026, 5, 2))
        repository.pending.value = listOf(testPending(testDraft("p1", 2026), PendingStatus.CONFLICT).copy(existing = summary))
        val vm = viewModel()

        val state = vm.uiState.value as CampaignClosedUiState.Conflict
        assertEquals(summary, state.existing)

        var done = false
        vm.acknowledgeConflict { done = true }

        assertEquals(listOf("p1" to 2026), repository.discarded)
        assertTrue(done)
    }

    @Test
    fun `viewing the existing receipt after a conflict discards the row and shows the settlement`() = runTest {
        repository.cache.value = listOf(testSettlement("p1", 2026))
        repository.pending.value = listOf(testPending(testDraft("p1", 2026), PendingStatus.CONFLICT))
        val vm = viewModel()
        assertTrue(vm.uiState.value is CampaignClosedUiState.Conflict)

        vm.showExistingReceipt()

        assertTrue(vm.uiState.value is CampaignClosedUiState.Closed)
    }

    @Test
    fun `a failed row is pending with the failed flag`() = runTest {
        repository.pending.value = listOf(testPending(testDraft("p1", 2026), PendingStatus.FAILED))

        val state = viewModel().uiState.value as CampaignClosedUiState.Pending

        assertTrue(state.failed)
    }

    @Test
    fun `nothing cached refreshes the plot once and then reports it missing`() = runTest {
        val vm = viewModel()

        assertEquals(listOf(listOf("p1")), repository.refreshedIds)
        assertEquals(CampaignClosedUiState.Missing, vm.uiState.value)
    }

    @Test
    fun `a refresh that finds the settlement shows it`() = runTest {
        val vm = viewModel()

        repository.cache.value = listOf(testSettlement("p1", 2026))

        assertTrue(vm.uiState.value is CampaignClosedUiState.Closed)
    }
}
