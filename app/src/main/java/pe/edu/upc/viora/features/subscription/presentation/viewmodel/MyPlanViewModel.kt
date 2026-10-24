package pe.edu.upc.viora.features.subscription.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.subscription.application.usecase.GetCurrentSubscriptionUseCase
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerSubscription
import pe.edu.upc.viora.features.subscription.presentation.state.MyPlanRules
import pe.edu.upc.viora.features.subscription.presentation.state.MyPlanUiState

/**
 * P99 · Mi plan and the "Tu plan" card of Mi cuenta: the plan the server confirms (GET subscriptions) against the
 * hectares of the producer's active plots.
 */
@HiltViewModel
class MyPlanViewModel @Inject constructor(
    private val currentSubscription: GetCurrentSubscriptionUseCase,
    observePlots: ObservePlotsUseCase,
    private val clock: Clock,
) : ViewModel() {

    private sealed interface Loaded {
        data object Loading : Loaded
        data class Done(val subscription: ProducerSubscription?) : Loaded
        data class Failed(val error: AppError) : Loaded
    }

    private val subscription = MutableStateFlow<Loaded>(Loaded.Loading)

    val uiState: StateFlow<MyPlanUiState> = combine(subscription, observePlots()) { loaded, plots ->
        when (loaded) {
            Loaded.Loading -> MyPlanUiState(isLoading = true)
            is Loaded.Failed -> MyPlanUiState(isLoading = false, error = loaded.error)
            is Loaded.Done -> MyPlanRules.state(loaded.subscription, plots, LocalDate.now(clock), clock.zone)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), MyPlanUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            subscription.value = when (val result = currentSubscription()) {
                is AppResult.Success -> Loaded.Done(result.value)
                is AppResult.Failure -> Loaded.Failed(result.error)
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
