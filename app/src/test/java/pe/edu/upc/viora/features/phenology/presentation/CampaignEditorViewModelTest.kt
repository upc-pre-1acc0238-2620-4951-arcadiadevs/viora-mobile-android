package pe.edu.upc.viora.features.phenology.presentation

import androidx.lifecycle.SavedStateHandle
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveBearingIndexUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveHarvestHistoryUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RecordHarvestYieldUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RectifyHarvestYieldUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RemoveHarvestRecordUseCase
import pe.edu.upc.viora.features.phenology.domain.entity.BearingIndex
import pe.edu.upc.viora.features.phenology.presentation.state.CampaignChangeKind
import pe.edu.upc.viora.features.phenology.presentation.state.CampaignEditorUiState
import pe.edu.upc.viora.features.phenology.presentation.state.CampaignResult
import pe.edu.upc.viora.features.phenology.presentation.state.YearCaption
import pe.edu.upc.viora.features.phenology.presentation.state.YearError
import pe.edu.upc.viora.features.phenology.presentation.viewmodel.CampaignEditorViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class CampaignEditorViewModelTest {

    private val repository = FakeHarvestRepository(figmaRecords())
    private val clock = Clock.fixed(Instant.parse("2026-10-04T12:00:00Z"), ZoneOffset.UTC)
    private val seen = mutableListOf<CampaignEditorUiState?>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository.index.value = BearingIndex(0.51, 4, null)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): CampaignEditorViewModel {
        val vm = CampaignEditorViewModel(
            savedStateHandle = SavedStateHandle(mapOf("plotId" to "plot-1")),
            observeRecords = ObserveHarvestHistoryUseCase(repository),
            observeIndex = ObserveBearingIndexUseCase(repository),
            recordYield = RecordHarvestYieldUseCase(repository),
            rectifyYield = RectifyHarvestYieldUseCase(repository),
            removeRecord = RemoveHarvestRecordUseCase(repository),
            clock = clock,
        )
        vm.uiState.onEach { seen += it }.launchIn(CoroutineScope(Dispatchers.Main))
        return vm
    }

    private fun state() = seen.last()!!

    @Test
    fun `starts closed`() = runTest {
        viewModel()

        assertNull(seen.last())
    }

    @Test
    fun `add starts one year before the oldest registered campaign`() = runTest {
        val vm = viewModel()

        vm.openAdd()

        assertEquals(2021, state().year)
        assertEquals(YearCaption.BeforeOldest(2022), state().yearCaption)
        assertFalse(state().canSave)
    }

    @Test
    fun `add with no campaigns starts at last year`() = runTest {
        repository.records.value = emptyList()
        val vm = viewModel()

        vm.openAdd()

        assertEquals(2025, state().year)
        assertEquals(YearCaption.FirstEver, state().yearCaption)
    }

    @Test
    fun `a valid campaign can be saved and is sent to the server`() = runTest {
        val vm = viewModel()
        vm.openAdd()
        vm.onKilosChange("10500")
        var result: CampaignResult? = null

        assertTrue(state().canSave)
        vm.save { result = it }

        assertEquals(listOf(2021 to 10_500.0), repository.recorded)
        assertEquals(CampaignResult(CampaignChangeKind.ADDED, 2021, "new-2021"), result)
        assertNull(seen.last())
    }

    @Test
    fun `a future year shows the error and cannot be saved`() = runTest {
        val vm = viewModel()
        vm.openAdd()
        repeat(6) { vm.onYearStep(+1) }
        vm.onKilosChange("500")

        assertEquals(2027, state().year)
        assertEquals(YearError.FUTURE, state().yearError)
        assertEquals(2026, state().maxYear)
        assertFalse(state().canSave)
        assertNull(state().preview)
    }

    @Test
    fun `a year before 2000 shows the error`() = runTest {
        val vm = viewModel()
        vm.openAdd()
        repeat(22) { vm.onYearStep(-1) }
        vm.onKilosChange("500")

        assertEquals(1999, state().year)
        assertEquals(YearError.TOO_OLD, state().yearError)
        assertFalse(state().canSave)
    }

    @Test
    fun `a registered year is a duplicate`() = runTest {
        val vm = viewModel()
        vm.openAdd()
        vm.onYearStep(+1)
        vm.onKilosChange("500")

        assertEquals(2022, state().year)
        assertEquals(YearError.DUPLICATE, state().yearError)
        assertFalse(state().canSave)
    }

    @Test
    fun `kilos of zero or less show the kilos error and block saving`() = runTest {
        val vm = viewModel()
        vm.openAdd()

        vm.onKilosChange("-500")
        assertTrue(state().kilosError)
        assertFalse(state().canSave)

        vm.onKilosChange("0")
        assertTrue(state().kilosError)
        assertFalse(state().canSave)

        vm.onKilosChange("")
        assertFalse(state().kilosError)
        assertFalse(state().canSave)
    }

    @Test
    fun `typing letters is ignored`() = runTest {
        val vm = viewModel()
        vm.openAdd()

        vm.onKilosChange("10a5b00")

        assertEquals("10500", state().kilosText)
    }

    @Test
    fun `the preview matches the Figma example in kilograms`() = runTest {
        val vm = viewModel()
        vm.openAdd()
        vm.onKilosChange("10500")

        val preview = state().preview!!

        assertEquals(0.51, preview.before!!, 0.0)
        assertEquals(0.48, preview.after!!, 0.0)
    }

    @Test
    fun `a failed save keeps the sheet open with the error and can be retried`() = runTest {
        repository.recordResult = { _, _ -> AppResult.Failure(AppError.Conflict()) }
        val vm = viewModel()
        vm.openAdd()
        vm.onKilosChange("10500")
        var saved = false

        vm.save { saved = true }

        assertFalse(saved)
        assertEquals(AppError.Conflict(), state().error)
        assertFalse(state().isSaving)
        assertTrue(state().canSave)

        repository.recordResult = null
        vm.save { saved = true }
        assertTrue(saved)
    }

    @Test
    fun `correct shows the registered year and kilos, and saving needs a change`() = runTest {
        val vm = viewModel()
        val record = repository.records.value.first { it.campaignYear == 2024 }

        vm.openCorrect(record)

        assertEquals(2024, state().year)
        assertEquals("22000", state().kilosText)
        assertFalse(state().canSave)
        assertNotNull(state().deletePreview)

        vm.onKilosChange("20500")
        assertTrue(state().canSave)
        assertEquals(0.49, state().preview!!.after!!, 0.0)
    }

    @Test
    fun `the year cannot be stepped while correcting`() = runTest {
        val vm = viewModel()
        vm.openCorrect(repository.records.value.first { it.campaignYear == 2024 })

        vm.onYearStep(+1)

        assertEquals(2024, state().year)
    }

    @Test
    fun `correcting sends the new yield and reports the correction`() = runTest {
        val vm = viewModel()
        vm.openCorrect(repository.records.value.first { it.campaignYear == 2024 })
        vm.onKilosChange("20500")
        var result: CampaignResult? = null

        vm.save { result = it }

        assertEquals(listOf("r-2024" to 20_500.0), repository.rectified)
        assertEquals(CampaignResult(CampaignChangeKind.CORRECTED, 2024, "r-2024"), result)
        assertNull(seen.last())
    }

    @Test
    fun `delete asks for confirmation with the index without that year`() = runTest {
        repository.records.value = listOf(harvest(2021, 10_500.0)) + figmaRecords()
        repository.index.value = BearingIndex(0.48, 5, null)
        val vm = viewModel()
        vm.openCorrect(repository.records.value.first { it.campaignYear == 2021 })

        vm.requestDelete()

        assertTrue(state().confirmingDelete)
        assertEquals(0.48, state().deletePreview!!.before!!, 0.0)
        assertEquals(0.51, state().deletePreview!!.after!!, 0.0)
        assertEquals(4, state().deletePreview!!.campaignsAfter)
        assertTrue(repository.removed.isEmpty())

        vm.cancelDelete()
        assertFalse(state().confirmingDelete)
    }

    @Test
    fun `confirming the delete removes the record and reports it`() = runTest {
        val vm = viewModel()
        vm.openCorrect(repository.records.value.first { it.campaignYear == 2024 })
        vm.requestDelete()
        var result: CampaignResult? = null

        vm.confirmDelete { result = it }

        assertEquals(listOf("r-2024"), repository.removed)
        assertEquals(CampaignResult(CampaignChangeKind.DELETED, 2024, null), result)
        assertNull(seen.last())
    }

    @Test
    fun `a failed delete keeps the dialog open with the error`() = runTest {
        repository.removeResult = AppResult.Failure(AppError.PreconditionFailed())
        val vm = viewModel()
        vm.openCorrect(repository.records.value.first { it.campaignYear == 2024 })
        vm.requestDelete()

        vm.confirmDelete { }

        assertTrue(state().confirmingDelete)
        assertEquals(AppError.PreconditionFailed(), state().error)
        assertFalse(state().isSaving)
    }

    @Test
    fun `dismiss closes the sheet`() = runTest {
        val vm = viewModel()
        vm.openAdd()

        vm.dismiss()

        assertNull(seen.last())
    }
}
