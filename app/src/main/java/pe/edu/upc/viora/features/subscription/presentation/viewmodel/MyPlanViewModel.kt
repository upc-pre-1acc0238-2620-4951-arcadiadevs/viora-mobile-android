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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.subscription.application.usecase.GetCurrentSubscriptionUseCase
import pe.edu.upc.viora.features.subscription.application.usecase.GetLatestDataSharingConsentUseCase
import pe.edu.upc.viora.features.subscription.application.usecase.WithdrawDataSharingConsentUseCase
import pe.edu.upc.viora.features.subscription.domain.entity.DataSharingConsent
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerSubscription
import pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionMode
import pe.edu.upc.viora.features.subscription.presentation.state.MyPlanRules
import pe.edu.upc.viora.features.subscription.presentation.state.MyPlanUiState

/**
 * P99 · Mi plan and the "Tu plan" card of Mi cuenta: the plan the server confirms (GET subscriptions) against the
 * hectares of the producer's active plots. A plan that came with a code also reads the consent to share data, so
 * P99 can say with whom and stop it.
 */
@HiltViewModel
class MyPlanViewModel @Inject constructor(
    private val currentSubscription: GetCurrentSubscriptionUseCase,
    observePlots: ObservePlotsUseCase,
    private val latestConsent: GetLatestDataSharingConsentUseCase,
    private val withdrawConsent: WithdrawDataSharingConsentUseCase,
    private val clock: Clock,
) : ViewModel() {

    private sealed interface Loaded {
        data object Loading : Loaded
        data class Done(val subscription: ProducerSubscription?) : Loaded
        data class Failed(val error: AppError) : Loaded
    }

    private data class Sharing(
        val consent: DataSharingConsent? = null,
        val isWithdrawing: Boolean = false,
        val error: AppError? = null,
    )

    private val subscription = MutableStateFlow<Loaded>(Loaded.Loading)
    private val sharing = MutableStateFlow(Sharing())

    val uiState: StateFlow<MyPlanUiState> = combine(subscription, observePlots(), sharing) { loaded, plots, shared ->
        when (loaded) {
            Loaded.Loading -> MyPlanUiState(isLoading = true)
            is Loaded.Failed -> MyPlanUiState(isLoading = false, error = loaded.error)
            is Loaded.Done -> MyPlanRules.state(loaded.subscription, plots, LocalDate.now(clock), clock.zone).copy(
                sharing = shared.consent,
                isWithdrawing = shared.isWithdrawing,
                withdrawError = shared.error,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), MyPlanUiState())

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val result = currentSubscription()
            subscription.value = when (result) {
                is AppResult.Success -> Loaded.Done(result.value)
                is AppResult.Failure -> Loaded.Failed(result.error)
            }
            val current = (result as? AppResult.Success)?.value
            if (current?.mode == SubscriptionMode.COOPERATIVE_SPONSORED || current?.cooperativeId != null) loadConsent()
        }
    }

    /** P99 · Dejar de compartir, once confirmed in its dialog. */
    fun stopSharing() {
        val current = sharing.value
        val consent = current.consent ?: return
        if (current.isWithdrawing) return
        sharing.update { it.copy(isWithdrawing = true, error = null) }
        viewModelScope.launch {
            when (val result = withdrawConsent(consent.id)) {
                is AppResult.Success -> sharing.value = Sharing(consent = result.value)
                // Already withdrawn or ended elsewhere: read it again instead of showing an error.
                is AppResult.Failure -> if (result.error is AppError.Conflict) {
                    sharing.update { it.copy(isWithdrawing = false) }
                    loadConsent()
                } else {
                    sharing.update { it.copy(isWithdrawing = false, error = result.error) }
                }
            }
        }
    }

    private suspend fun loadConsent() {
        // Without an answer the section is simply not shown; the plan itself is still right.
        val result = latestConsent()
        if (result is AppResult.Success) sharing.update { it.copy(consent = result.value, error = null) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
