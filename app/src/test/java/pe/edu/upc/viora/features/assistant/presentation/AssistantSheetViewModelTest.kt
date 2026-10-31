package pe.edu.upc.viora.features.assistant.presentation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.IdGenerator
import pe.edu.upc.viora.core.domain.OliveVariety
import pe.edu.upc.viora.core.domain.PlotId
import pe.edu.upc.viora.core.domain.Uuid
import pe.edu.upc.viora.features.assistant.FakeAssistantRepository
import pe.edu.upc.viora.features.assistant.PLOT_ID
import pe.edu.upc.viora.features.assistant.answer
import pe.edu.upc.viora.features.assistant.application.usecase.AskAssistantUseCase
import pe.edu.upc.viora.features.assistant.application.usecase.FindAnswerToReopenUseCase
import pe.edu.upc.viora.features.assistant.application.usecase.LoadAssistantQuestionsUseCase
import pe.edu.upc.viora.features.assistant.application.usecase.MoveOnFromAnswerUseCase
import pe.edu.upc.viora.features.assistant.application.usecase.ObserveRecentAnswersUseCase
import pe.edu.upc.viora.features.assistant.application.usecase.OpenAnswerUseCase
import pe.edu.upc.viora.features.assistant.application.usecase.RefreshRecentAnswersUseCase
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantScreen
import pe.edu.upc.viora.features.assistant.domain.entity.Freshness
import pe.edu.upc.viora.features.assistant.presentation.state.AskFailure
import pe.edu.upc.viora.features.assistant.presentation.state.AskPage
import pe.edu.upc.viora.features.assistant.presentation.state.AssistantSheetUiState
import pe.edu.upc.viora.features.assistant.presentation.state.AssistantTab
import pe.edu.upc.viora.features.assistant.presentation.state.Catalog
import pe.edu.upc.viora.features.assistant.presentation.viewmodel.AssistantSheetViewModel
import pe.edu.upc.viora.features.assistant.question
import pe.edu.upc.viora.features.plotmanagement.application.published.Plot
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotChanges
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

private class AssistantPlots : PlotRepository {
    val plots = MutableStateFlow<List<Plot>>(emptyList())

    override fun observePlots(): Flow<List<Plot>> = plots
    override fun observePlot(id: PlotId): Flow<Plot?> = plots.map { all -> all.firstOrNull { it.id == id } }
    override fun observeArchivedPlots(): Flow<List<Plot>> = MutableStateFlow(emptyList())
    override fun observeLastRefresh(): Flow<Instant?> = MutableStateFlow(null)
    override suspend fun refresh(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun refreshArchived(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun register(newPlot: NewPlot): AppResult<Plot> = AppResult.Failure(AppError.Offline)
    override suspend fun update(id: PlotId, changes: PlotChanges): AppResult<Plot> = AppResult.Failure(AppError.Offline)
    override suspend fun archive(id: PlotId): AppResult<Unit> = AppResult.Failure(AppError.Offline)
    override suspend fun restore(id: PlotId): AppResult<Plot> = AppResult.Failure(AppError.Offline)
}

/** Hands out 00000000-0000-4000-8000-00000000000N, one after the other. */
private class SequentialIds : IdGenerator {
    private var next = 1
    override fun newUuid(): Uuid = Uuid.parse("00000000-0000-4000-8000-%012d".format(next++))
}

@OptIn(ExperimentalCoroutinesApi::class)
class AssistantSheetViewModelTest {

    private val repository = FakeAssistantRepository()
    private val plots = AssistantPlots()
    private var online = true
    private val clock = Clock.fixed(Instant.parse("2026-10-30T15:00:00Z"), ZoneOffset.UTC)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = AssistantSheetViewModel(
        loadQuestions = LoadAssistantQuestionsUseCase(repository),
        askAssistant = AskAssistantUseCase(repository),
        findAnswerToReopen = FindAnswerToReopenUseCase(repository),
        openAnswer = OpenAnswerUseCase(repository),
        observeRecent = ObserveRecentAnswersUseCase(repository),
        refreshRecent = RefreshRecentAnswersUseCase(repository),
        moveOnFromAnswer = MoveOnFromAnswerUseCase(repository),
        observePlots = ObservePlotsUseCase(plots),
        connectivity = { online },
        ids = SequentialIds(),
        clock = clock,
    ).also { it.bind(AssistantScreen.ALTERNATION, PLOT_ID, "La Yarada 02") }

    private val AssistantSheetViewModel.state: AssistantSheetUiState get() = uiState.value!!

    private fun plot(id: String, name: String) = Plot(
        id = PlotId(id),
        name = name,
        variety = OliveVariety.SEVILLANA,
        areaHectares = 2.5,
        treesPerHectare = 72,
        rowSpacingMeters = 7.0,
        treeSpacingMeters = 4.0,
        outline = emptyList(),
        lastPruningDate = null,
        isActive = true,
        revision = 0,
    )

    @Test
    fun opensOnTheQuestionsWhenTheScreenHasNoAnswer() = runTest {
        val vm = viewModel()

        assertEquals(AskPage.Questions, vm.state.page)
        assertEquals(Catalog.Ready(listOf(question())), vm.state.catalog)
        assertEquals(AssistantTab.ASK, vm.state.tab)
    }

    @Test
    fun reopensOnTheAnswerTheScreenKeeps() = runTest {
        repository.reopen = AppResult.Success(answer())

        val page = viewModel().state.page as AskPage.Answered

        assertEquals(answer(), page.answer)
        assertEquals("¿Por qué mi índice es 0,51?", page.question.label)
    }

    @Test
    fun asksThinksAndAnswers() = runTest {
        val vm = viewModel()

        vm.ask(question())
        assertTrue(vm.state.page is AskPage.Thinking)
        advanceUntilIdle()

        val page = vm.state.page as AskPage.Answered
        assertEquals(answer(), page.answer)
        assertEquals(false, repository.asks.single().forceRefresh)
    }

    @Test
    fun aDisabledQuestionIsNotAsked() = runTest {
        val vm = viewModel()

        vm.ask(question(enabled = false))

        assertTrue(repository.asks.isEmpty())
        assertEquals(AskPage.Questions, vm.state.page)
    }

    @Test
    fun retrySendsTheSameRequestSoTheServerAnswersItOnce() = runTest {
        repository.askResult = AppResult.Failure(AppError.Server(500))
        val vm = viewModel()
        vm.ask(question())
        advanceUntilIdle()
        assertEquals(AskFailure.Generic, (vm.state.page as AskPage.Failed).reason)

        repository.askResult = AppResult.Success(answer())
        vm.retry()
        advanceUntilIdle()

        assertEquals(2, repository.asks.size)
        assertEquals(repository.asks[0].key, repository.asks[1].key)
        assertTrue(vm.state.page is AskPage.Answered)
    }

    @Test
    fun withoutSignalTheQuestionIsNotSentAndTheSheetSaysSo() = runTest {
        val vm = viewModel()
        online = false

        vm.ask(question())

        assertTrue(repository.asks.isEmpty())
        assertEquals(AskFailure.Offline, (vm.state.page as AskPage.Failed).reason)
        assertTrue(vm.state.offline)
    }

    @Test
    fun theLimitOfAnswersSaysHowManyMinutesToWait() = runTest {
        repository.askResult = AppResult.Failure(AppError.RateLimited(retryAfterMillis = 61_000))
        val vm = viewModel()

        vm.ask(question())
        advanceUntilIdle()

        assertEquals(AskFailure.RateLimited(2), (vm.state.page as AskPage.Failed).reason)
    }

    @Test
    fun aQuestionLeftWithoutDataReadsTheQuestionsAgain() = runTest {
        repository.askResult = AppResult.Failure(AppError.Conflict(code = "ASSISTANT_CONFLICT"))
        val vm = viewModel()
        val before = repository.questionCalls

        vm.ask(question())
        advanceUntilIdle()

        assertEquals(AskFailure.Unavailable, (vm.state.page as AskPage.Failed).reason)
        assertEquals(before + 1, repository.questionCalls)
    }

    @Test
    fun anExpiredPlanIsTold() = runTest {
        repository.questions = AppResult.Failure(AppError.Forbidden())

        assertEquals(Catalog.Failed(AskFailure.NoPlan), viewModel().state.catalog)
    }

    @Test
    fun anotherQuestionStopsReopeningTheAnswer() = runTest {
        repository.reopen = AppResult.Success(answer())
        val vm = viewModel()

        vm.anotherQuestion()

        assertEquals(listOf(answer().id), repository.movedOn)
        assertEquals(AskPage.Questions, vm.state.page)
    }

    @Test
    fun askingAgainWithNewDataKeepsTheAnswerUntilTheNewOneArrives() = runTest {
        repository.reopen = AppResult.Success(answer(freshness = Freshness.STALE))
        val fresh = answer(id = "1c6a5a3e-5c4f-4b8e-9a35-4b7e3e5f1a11")
        repository.askResult = AppResult.Success(fresh)
        val vm = viewModel()

        vm.askAgainWithNewData()
        assertTrue((vm.state.page as AskPage.Answered).refreshing)
        advanceUntilIdle()

        assertEquals(fresh, (vm.state.page as AskPage.Answered).answer)
        assertTrue(repository.asks.single().forceRefresh)
    }

    @Test
    fun eachNewAskHasItsOwnKey() = runTest {
        val vm = viewModel()
        vm.ask(question())
        advanceUntilIdle()
        vm.anotherQuestion()
        vm.ask(question())
        advanceUntilIdle()

        assertNotEquals(repository.asks[0].key, repository.asks[1].key)
    }

    @Test
    fun recentAnswersCarryTheNameOfTheirPlotAndOpenWithTheirFreshness() = runTest {
        plots.plots.value = listOf(plot(PLOT_ID, "La Yarada 02"))
        val saved = answer()
        repository.recent.value = listOf(saved)
        repository.opened = AppResult.Success(saved.copy(freshness = Freshness.STALE))
        val vm = viewModel()

        vm.selectTab(AssistantTab.RECENT)
        val recent = vm.state.recent!!.single()
        assertEquals("La Yarada 02", recent.plotName)
        assertEquals(1, repository.refreshCalls)

        vm.openRecent(recent)

        assertEquals(AssistantTab.ASK, vm.state.tab)
        assertEquals(Freshness.STALE, (vm.state.page as AskPage.Answered).answer.freshness)
    }

    @Test
    fun bindingTheSameScreenAgainKeepsWhatIsOnScreen() = runTest {
        val vm = viewModel()
        vm.ask(question())
        advanceUntilIdle()

        vm.bind(AssistantScreen.ALTERNATION, PLOT_ID, "La Yarada 02")

        assertTrue(vm.state.page is AskPage.Answered)
    }
}
