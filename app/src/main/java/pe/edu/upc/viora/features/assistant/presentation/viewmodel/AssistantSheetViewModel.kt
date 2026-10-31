package pe.edu.upc.viora.features.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.IdGenerator
import pe.edu.upc.viora.core.domain.IdempotencyKey
import pe.edu.upc.viora.core.domain.isConnectivity
import pe.edu.upc.viora.core.network.ConnectivityObserver
import pe.edu.upc.viora.features.assistant.application.usecase.AskAssistantUseCase
import pe.edu.upc.viora.features.assistant.application.usecase.FindAnswerToReopenUseCase
import pe.edu.upc.viora.features.assistant.application.usecase.LoadAssistantQuestionsUseCase
import pe.edu.upc.viora.features.assistant.application.usecase.MoveOnFromAnswerUseCase
import pe.edu.upc.viora.features.assistant.application.usecase.ObserveRecentAnswersUseCase
import pe.edu.upc.viora.features.assistant.application.usecase.OpenAnswerUseCase
import pe.edu.upc.viora.features.assistant.application.usecase.RefreshRecentAnswersUseCase
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantAnswer
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantQuestion
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantScreen
import pe.edu.upc.viora.features.assistant.presentation.state.AskFailure
import pe.edu.upc.viora.features.assistant.presentation.state.AskPage
import pe.edu.upc.viora.features.assistant.presentation.state.AskedQuestion
import pe.edu.upc.viora.features.assistant.presentation.state.AssistantPresenter
import pe.edu.upc.viora.features.assistant.presentation.state.AssistantSheetUiState
import pe.edu.upc.viora.features.assistant.presentation.state.AssistantTab
import pe.edu.upc.viora.features.assistant.presentation.state.Catalog
import pe.edu.upc.viora.features.assistant.presentation.state.RecentAnswer
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import java.time.Clock
import javax.inject.Inject

/**
 * The assistant sheet of one screen and plot. It opens on the answer the screen keeps until "Otra
 * pregunta", or on the questions. An ask keeps its request key until it ends, so "Reintentar" never
 * makes the server write a second answer.
 */
@HiltViewModel
class AssistantSheetViewModel @Inject constructor(
    private val loadQuestions: LoadAssistantQuestionsUseCase,
    private val askAssistant: AskAssistantUseCase,
    private val findAnswerToReopen: FindAnswerToReopenUseCase,
    private val openAnswer: OpenAnswerUseCase,
    private val observeRecent: ObserveRecentAnswersUseCase,
    private val refreshRecent: RefreshRecentAnswersUseCase,
    private val moveOnFromAnswer: MoveOnFromAnswerUseCase,
    private val observePlots: ObservePlotsUseCase,
    private val connectivity: ConnectivityObserver,
    private val ids: IdGenerator,
    private val clock: Clock,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AssistantSheetUiState?>(null)

    /** Null until [bind] names the screen and the plot. */
    val uiState: StateFlow<AssistantSheetUiState?> = _uiState.asStateFlow()

    private var plotId: String = ""
    private var pending: PendingAsk? = null
    private var askJob: Job? = null
    private var recentJob: Job? = null

    /** The ask in flight or that failed, kept so "Reintentar" sends the same request. */
    private data class PendingAsk(val question: AskedQuestion, val key: IdempotencyKey, val forceRefresh: Boolean)

    /** Opens the sheet of [screen] about the plot; binding the same screen and plot again keeps what is on screen. */
    fun bind(screen: AssistantScreen, plotId: String, plotName: String) {
        val current = _uiState.value
        if (current != null && current.screen == screen && this.plotId == plotId) return
        this.plotId = plotId
        _uiState.value = AssistantSheetUiState(screen = screen, plotName = plotName, offline = !connectivity.isOnline())
        recentJob?.cancel()
        recentJob = viewModelScope.launch { observeRecentWithPlotNames() }
        viewModelScope.launch {
            val catalog = async { fetchCatalog(screen) }
            val reopened = findAnswerToReopen(screen, plotId)
            val catalogResult = catalog.await()
            val answer = (reopened as? AppResult.Success)?.value
            update { state ->
                state.copy(
                    catalog = catalogResult,
                    page = if (answer != null) answeredPage(answer, catalogResult) else AskPage.Questions,
                )
            }
            if (reopened is AppResult.Failure && reopened.error.isConnectivity) markOffline()
        }
    }

    fun selectTab(tab: AssistantTab) {
        update { it.copy(tab = tab) }
        if (tab == AssistantTab.RECENT) viewModelScope.launch { syncRecent() }
    }

    fun ask(question: AssistantQuestion) {
        if (!question.enabled) return
        send(PendingAsk(AskedQuestion(question.id, question.label, question.icon), ids.newIdempotencyKey(), forceRefresh = false))
    }

    /** "Reintentar" (P127): the same request again. */
    fun retry() {
        pending?.let { send(it) }
    }

    /** "Volver a preguntar con datos nuevos" (P119): a new answer with the plot's current data. */
    fun askAgainWithNewData() {
        val page = _uiState.value?.page as? AskPage.Answered ?: return
        send(PendingAsk(page.question, ids.newIdempotencyKey(), forceRefresh = true), keepAnswer = true)
    }

    /** "Otra pregunta": back to the questions; the screen no longer reopens on this answer. */
    fun anotherQuestion() {
        val state = _uiState.value ?: return
        askJob?.cancel()
        pending = null
        (state.page as? AskPage.Answered)?.let { page -> viewModelScope.launch { moveOnFromAnswer(page.answer.id) } }
        update { it.copy(page = AskPage.Questions, tab = AssistantTab.ASK) }
        // The labels carry figures, and the data may have changed since the sheet opened
        viewModelScope.launch {
            val catalog = fetchCatalog(state.screen)
            update { it.copy(catalog = catalog) }
        }
    }

    /** Reads the questions again after they failed to load. */
    fun reloadQuestions() {
        val screen = _uiState.value?.screen ?: return
        update { it.copy(catalog = Catalog.Loading, offline = !connectivity.isOnline()) }
        viewModelScope.launch {
            val catalog = fetchCatalog(screen)
            update { it.copy(catalog = catalog) }
        }
    }

    /** Reopens an answer of "Recientes" (P118) on the "Preguntar" tab, then brings its freshness. */
    fun openRecent(recent: RecentAnswer) {
        askJob?.cancel()
        pending = null
        update { it.copy(tab = AssistantTab.ASK, page = answeredPage(recent.answer, it.catalog)) }
        viewModelScope.launch {
            val fresh = (openAnswer(recent.answer.id) as? AppResult.Success)?.value ?: return@launch
            update { state ->
                val page = state.page as? AskPage.Answered
                if (page?.answer?.id == fresh.id) state.copy(page = page.copy(answer = fresh)) else state
            }
        }
    }

    private fun send(ask: PendingAsk, keepAnswer: Boolean = false) {
        if (!connectivity.isOnline()) {
            markOffline()
            if (!keepAnswer) update { it.copy(page = AskPage.Failed(ask.question, AskFailure.Offline)) }
            return
        }
        pending = ask
        askJob?.cancel()
        update { state ->
            val answered = state.page as? AskPage.Answered
            val page = if (keepAnswer && answered != null) answered.copy(refreshing = true) else AskPage.Thinking(ask.question)
            state.copy(page = page, tab = AssistantTab.ASK, offline = false)
        }
        askJob = viewModelScope.launch {
            val startedAt = clock.millis()
            val result = askAssistant(ask.question.id, plotId, ask.key, ask.forceRefresh)
            // A saved answer comes back at once; the brote still thinks long enough to be seen
            val elapsed = clock.millis() - startedAt
            if (result is AppResult.Success && elapsed < MIN_THINKING_MILLIS) delay(MIN_THINKING_MILLIS - elapsed)
            when (result) {
                is AppResult.Success -> {
                    pending = null
                    val question = ask.question.copy(label = result.value.question ?: ask.question.label)
                    update { it.copy(page = AskPage.Answered(question, result.value)) }
                }
                is AppResult.Failure -> {
                    val reason = failureOf(result.error)
                    if (reason == AskFailure.Offline) markOffline()
                    if (reason == AskFailure.Unavailable) {
                        val screen = _uiState.value?.screen
                        if (screen != null) launch { fetchCatalog(screen).let { catalog -> update { it.copy(catalog = catalog) } } }
                    }
                    update { it.copy(page = AskPage.Failed(ask.question, reason)) }
                }
            }
        }
    }

    private suspend fun fetchCatalog(screen: AssistantScreen): Catalog =
        when (val result = loadQuestions(screen, plotId)) {
            is AppResult.Success -> Catalog.Ready(result.value)
            is AppResult.Failure -> {
                if (result.error.isConnectivity) markOffline()
                // Offline the questions already listed stay, disabled, as P126 shows them
                val previous = _uiState.value?.catalog as? Catalog.Ready
                previous?.takeIf { result.error.isConnectivity } ?: Catalog.Failed(failureOf(result.error))
            }
        }

    private suspend fun syncRecent() {
        val result = refreshRecent()
        if (result is AppResult.Failure && result.error.isConnectivity) markOffline()
    }

    private suspend fun observeRecentWithPlotNames() {
        combine(observeRecent(), observePlots()) { answers, plots ->
            val names = plots.associate { it.id.value to it.name }
            answers.map { RecentAnswer(it, names[it.plotId]) }
        }.collect { recent -> update { it.copy(recent = recent) } }
    }

    private fun answeredPage(answer: AssistantAnswer, catalog: Catalog): AskPage.Answered =
        AskPage.Answered(AssistantPresenter.askedQuestionOf(answer, catalog), answer)

    private fun markOffline() = update { it.copy(offline = true) }

    private fun update(transform: (AssistantSheetUiState) -> AssistantSheetUiState) {
        _uiState.update { state -> state?.let(transform) }
    }

    private fun failureOf(error: AppError): AskFailure = when (error) {
        AppError.Offline, AppError.Timeout -> AskFailure.Offline
        is AppError.RateLimited -> AskFailure.RateLimited(
            error.retryAfterMillis?.let { millis -> ((millis + MINUTE_MILLIS - 1) / MINUTE_MILLIS).toInt().coerceAtLeast(1) },
        )
        is AppError.Conflict -> AskFailure.Unavailable
        is AppError.Forbidden -> AskFailure.NoPlan
        else -> AskFailure.Generic
    }

    private companion object {
        /** The thinking state (P116) stays at least this long, so a quick answer does not flash. */
        const val MIN_THINKING_MILLIS = 900L
        const val MINUTE_MILLIS = 60_000L
    }
}
