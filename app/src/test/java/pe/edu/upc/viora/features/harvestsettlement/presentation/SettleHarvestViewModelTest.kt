package pe.edu.upc.viora.features.harvestsettlement.presentation

import androidx.lifecycle.SavedStateHandle
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.CompletableDeferred
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObservePendingSettlementsUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.SettleHarvestUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.UpdatePendingSettlementUseCase
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.PendingStatus
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleOutcome
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementSummary
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.CommercialSizeGrade
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.SettleDialog
import pe.edu.upc.viora.features.harvestsettlement.presentation.viewmodel.SettleHarvestViewModel
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class SettleHarvestViewModelTest {

    private val clock = Clock.fixed(Instant.parse("2026-10-05T12:00:00Z"), ZoneOffset.UTC)
    private val repository = FakeSettleRepository()
    private val plots = FakePlotsForSettle()
    private val settledCalls = mutableListOf<Pair<String, Int>>()
    private val queuedCalls = mutableListOf<Pair<String, Int>>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        plots.cached.value = listOf(testPlot("p1", "La Yarada 02", hectares = 2.5))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): SettleHarvestViewModel {
        val vm = SettleHarvestViewModel(
            savedStateHandle = SavedStateHandle(mapOf("plotId" to "p1", "campaignYear" to 2026)),
            observePlot = ObservePlotUseCase(plots),
            observePending = ObservePendingSettlementsUseCase(repository),
            settleHarvest = SettleHarvestUseCase(repository),
            updatePending = UpdatePendingSettlementUseCase(repository),
            clock = clock,
        )
        vm.uiState.launchIn(CoroutineScope(Dispatchers.Main))
        return vm
    }

    private fun SettleHarvestViewModel.fill(green: String = "12500", black: String = "8300") {
        onGreenChange(green)
        onBlackChange(black)
    }

    private fun SettleHarvestViewModel.confirm() =
        confirm(onSettled = { p, y -> settledCalls += p to y }, onQueued = { p, y -> queuedCalls += p to y })

    @Test
    fun `starts empty with today as the weighing date and cannot be saved`() = runTest {
        val state = viewModel().uiState.value

        assertEquals("La Yarada 02", state.plotName)
        assertEquals(2026, state.campaignYear)
        assertEquals(LocalDate.of(2026, 10, 5), state.weighedOn)
        assertFalse(state.canSave)
        assertNull(state.totalKg)
        assertNull(state.greenShare)
    }

    @Test
    fun `valid kilos compute the total, the tonnes per hectare and the split`() = runTest {
        val vm = viewModel()

        vm.fill()

        val state = vm.uiState.value
        assertEquals(20_800.0, state.totalKg!!, 0.0)
        assertEquals(8.32, state.tonnesPerHectare!!, 1e-9)
        assertEquals(12_500.0 / 20_800.0, state.greenShare!!, 1e-9)
        assertTrue(state.canSave)
    }

    @Test
    fun `negative kilos flag the field, clear the total and block saving`() = runTest {
        val vm = viewModel()

        vm.fill(green = "-300", black = "0")

        val state = vm.uiState.value
        assertTrue(state.greenError)
        assertFalse(state.blackError)
        assertNull(state.totalKg)
        assertNull(state.greenShare)
        assertFalse(state.canSave)
    }

    @Test
    fun `a zero total cannot be saved but one quality alone can`() = runTest {
        val vm = viewModel()

        vm.fill(green = "0", black = "")
        assertFalse(vm.uiState.value.canSave)

        vm.fill(green = "", black = "8300")
        assertTrue(vm.uiState.value.canSave)
        assertEquals(0.0, vm.uiState.value.greenShare!!, 0.0)
    }

    @Test
    fun `a future date is ignored and a past one is accepted`() = runTest {
        val vm = viewModel()

        vm.onWeighedOnChange(LocalDate.of(2026, 10, 6))
        assertEquals(LocalDate.of(2026, 10, 5), vm.uiState.value.weighedOn)

        vm.onWeighedOnChange(LocalDate.of(2026, 5, 14))
        assertEquals(LocalDate.of(2026, 5, 14), vm.uiState.value.weighedOn)
    }

    @Test
    fun `the mill ticket is cut at thirty characters`() = runTest {
        val vm = viewModel()

        vm.onMillTicketChange("B".repeat(40))

        assertEquals(30, vm.uiState.value.millTicket.length)
    }

    @Test
    fun `an invalid typed calibre blocks saving and picking a grade clears it`() = runTest {
        val vm = viewModel()
        vm.fill()

        vm.onCalibreTextChange("5000")
        assertTrue(vm.uiState.value.calibreError)
        assertFalse(vm.uiState.value.canSave)

        vm.onCalibreGradeSelect(CommercialSizeGrade(101, 110))
        assertFalse(vm.uiState.value.calibreError)
        assertEquals("", vm.uiState.value.calibreText)
        assertTrue(vm.uiState.value.canSave)

        vm.onCalibreTextChange("105")
        assertNull(vm.uiState.value.calibreGrade)
    }

    @Test
    fun `asking to confirm an invalid form opens nothing`() = runTest {
        val vm = viewModel()

        vm.requestConfirm()

        assertNull(vm.uiState.value.dialog)
    }

    @Test
    fun `confirming sends the draft with the chosen grade midpoint and the screen key`() = runTest {
        val vm = viewModel()
        vm.fill()
        vm.onWeighedOnChange(LocalDate.of(2026, 5, 14))
        vm.onMillTicketChange(" B-004512 ")
        vm.onCalibreGradeSelect(CommercialSizeGrade(101, 110))
        vm.requestConfirm()
        assertEquals(SettleDialog.Confirm, vm.uiState.value.dialog)

        vm.confirm()

        val (draft, key) = repository.settled.single()
        assertEquals("p1", draft.plotId)
        assertEquals(2026, draft.campaignYear)
        assertEquals(12_500.0, draft.greenOlivesKg, 0.0)
        assertEquals(8_300.0, draft.blackOlivesKg, 0.0)
        assertEquals(LocalDate.of(2026, 5, 14), draft.weighedOn)
        assertEquals("B-004512", draft.millTicketNumber)
        assertEquals(105.5, draft.commercialFruitsPerKg!!, 0.0)
        assertNotNull(key)
        assertEquals(listOf("p1" to 2026), settledCalls)
        assertTrue(queuedCalls.isEmpty())
        assertNull(vm.uiState.value.dialog)
    }

    @Test
    fun `optional fields are omitted when left empty`() = runTest {
        val vm = viewModel()
        vm.fill()
        vm.requestConfirm()

        vm.confirm()

        val draft = repository.settled.single().first
        assertNull(draft.millTicketNumber)
        assertNull(draft.commercialFruitsPerKg)
    }

    @Test
    fun `an offline settlement fires the queued callback`() = runTest {
        repository.settleResult = { AppResult.Success(SettleOutcome.Queued(testPending(it, PendingStatus.PENDING))) }
        val vm = viewModel()
        vm.fill()
        vm.requestConfirm()

        vm.confirm()

        assertEquals(listOf("p1" to 2026), queuedCalls)
        assertTrue(settledCalls.isEmpty())
    }

    @Test
    fun `an already settled campaign opens the 409 dialog with the existing summary`() = runTest {
        val summary = SettlementSummary(2026, 20_600.0, "VR-26-0139", LocalDate.of(2026, 5, 12))
        repository.settleResult = { AppResult.Success(SettleOutcome.AlreadySettled(summary)) }
        val vm = viewModel()
        vm.fill()
        vm.requestConfirm()

        vm.confirm()

        assertEquals(SettleDialog.AlreadySettled(summary), vm.uiState.value.dialog)
        assertFalse(vm.uiState.value.isSaving)
        assertTrue(settledCalls.isEmpty() && queuedCalls.isEmpty())
    }

    @Test
    fun `an already settled campaign without a summary still shows the dialog`() = runTest {
        repository.settleResult = { AppResult.Success(SettleOutcome.AlreadySettled(null)) }
        val vm = viewModel()
        vm.fill()
        vm.requestConfirm()

        vm.confirm()

        assertEquals(SettleDialog.AlreadySettled(null), vm.uiState.value.dialog)

        vm.dismissDialog()
        assertNull(vm.uiState.value.dialog)
    }

    @Test
    fun `a failure closes the dialog, keeps the form and shows the error`() = runTest {
        repository.settleResult = { AppResult.Failure(AppError.Server(500)) }
        val vm = viewModel()
        vm.fill()
        vm.requestConfirm()

        vm.confirm()

        val state = vm.uiState.value
        assertNull(state.dialog)
        assertEquals(AppError.Server(500), state.error)
        assertFalse(state.isSaving)
        assertEquals("12500", state.greenText)
        assertTrue(state.canSave)
        assertTrue(settledCalls.isEmpty() && queuedCalls.isEmpty())

        vm.onGreenChange("12000")
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `a retry after a failure reuses the same idempotency key`() = runTest {
        repository.settleResult = { AppResult.Failure(AppError.Server(500)) }
        val vm = viewModel()
        vm.fill()
        vm.requestConfirm()
        vm.confirm()
        vm.requestConfirm()

        vm.confirm()

        assertEquals(2, repository.settled.size)
        assertEquals(repository.settled[0].second, repository.settled[1].second)
    }

    @Test
    fun `a double tap on confirm settles once`() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate
        val vm = viewModel()
        vm.fill()
        vm.requestConfirm()

        vm.confirm()
        vm.confirm()
        assertTrue(vm.uiState.value.isSaving)
        assertEquals(1, repository.settled.size)

        gate.complete(Unit)

        assertEquals(1, repository.settled.size)
        assertEquals(listOf("p1" to 2026), settledCalls)
        assertFalse(vm.uiState.value.isSaving)
    }

    @Test
    fun `the dialog cannot be dismissed while the settle call runs`() = runTest {
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate
        val vm = viewModel()
        vm.fill()
        vm.requestConfirm()
        vm.confirm()

        vm.dismissDialog()
        assertEquals(SettleDialog.Confirm, vm.uiState.value.dialog)

        gate.complete(Unit)
    }

    @Test
    fun `dismissing the confirm dialog keeps the typed kilos`() = runTest {
        val vm = viewModel()
        vm.fill()
        vm.requestConfirm()

        vm.dismissDialog()

        assertNull(vm.uiState.value.dialog)
        assertEquals("12500", vm.uiState.value.greenText)
        assertTrue(repository.settled.isEmpty())
    }

    @Test
    fun `a pending settlement prefills the form and switches to edit mode`() = runTest {
        val saved = testDraft("p1", 2026).copy(millTicketNumber = "B-004512", commercialFruitsPerKg = CommercialSizeGrade.SCALE[4].midpoint)
        repository.pending.value = listOf(testPending(saved, PendingStatus.PENDING))

        val state = viewModel().uiState.value

        assertTrue(state.isEditing)
        assertEquals("12500", state.greenText)
        assertEquals("8300", state.blackText)
        assertEquals("B-004512", state.millTicket)
        assertEquals(LocalDate.of(2026, 5, 14), state.weighedOn)
        assertEquals(CommercialSizeGrade.SCALE[4], state.calibreGrade)
        assertTrue(state.canSave)
    }

    @Test
    fun `without a pending settlement the form is not in edit mode`() = runTest {
        repository.pending.value = listOf(testPending(testDraft("other", 2026), PendingStatus.PENDING))

        assertFalse(viewModel().uiState.value.isEditing)
    }

    @Test
    fun `a conflict row is not edited`() = runTest {
        repository.pending.value = listOf(testPending(testDraft("p1", 2026), PendingStatus.CONFLICT))

        assertFalse(viewModel().uiState.value.isEditing)
    }

    @Test
    fun `saving in edit mode updates the pending row instead of settling anew`() = runTest {
        repository.pending.value = listOf(testPending(testDraft("p1", 2026), PendingStatus.FAILED))
        val vm = viewModel()
        vm.onGreenChange("13000")
        vm.requestConfirm()

        vm.confirm()

        assertEquals(1, repository.updated.size)
        assertEquals(13_000.0, repository.updated.single().greenOlivesKg, 0.0)
        assertTrue(repository.settled.isEmpty())
        assertEquals(listOf("p1" to 2026), queuedCalls)
        assertTrue(settledCalls.isEmpty())
    }

    @Test
    fun `a pending row that synced meanwhile sends the producer to the closed receipt`() = runTest {
        repository.pending.value = listOf(testPending(testDraft("p1", 2026), PendingStatus.PENDING))
        repository.updateResult = AppResult.Failure(AppError.NotFound("No pending settlement for the campaign"))
        val vm = viewModel()
        vm.requestConfirm()

        vm.confirm()

        assertEquals(listOf("p1" to 2026), settledCalls)
        assertTrue(queuedCalls.isEmpty())
        assertFalse(vm.uiState.value.isSaving)
    }

    @Test
    fun `another update failure keeps the form with the error`() = runTest {
        repository.pending.value = listOf(testPending(testDraft("p1", 2026), PendingStatus.PENDING))
        repository.updateResult = AppResult.Failure(AppError.Offline)
        val vm = viewModel()
        vm.requestConfirm()

        vm.confirm()

        assertEquals(AppError.Offline, vm.uiState.value.error)
        assertNull(vm.uiState.value.dialog)
        assertTrue(settledCalls.isEmpty() && queuedCalls.isEmpty())
    }
}
