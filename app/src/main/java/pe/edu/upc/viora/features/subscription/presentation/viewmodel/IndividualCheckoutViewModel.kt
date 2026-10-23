package pe.edu.upc.viora.features.subscription.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.subscription.application.usecase.*
import pe.edu.upc.viora.features.subscription.domain.entity.IndividualPurchase
import pe.edu.upc.viora.features.subscription.domain.valueobject.*
import pe.edu.upc.viora.features.subscription.presentation.state.IndividualCheckoutUiState

@HiltViewModel
class IndividualCheckoutViewModel @Inject constructor(
    private val prepare: PrepareIndividualPurchaseUseCase,
    private val resume: ResumeIndividualPurchaseUseCase,
    private val checkout: CreateIndividualCheckoutUseCase,
    private val getSubscriptionById: GetSubscriptionByIdUseCase,
    private val getCurrentSubscription: GetCurrentSubscriptionUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow<IndividualCheckoutUiState>(IndividualCheckoutUiState.Idle)
    val uiState = mutableState.asStateFlow()

    private var activeSubscriptionId: String? = null
    private var pollingJob: Job? = null
    var pollIntervalMs: Long = 5_000L
    var maxPollDurationMs: Long = 60_000L

    fun start(plan: ProducerPlanCode, capacity: PlanCapacity) = launchOperation {
        // A durable operation always takes precedence over a new screen selection.
        when (val previous = resume()) {
            is AppResult.Failure -> mutableState.value = IndividualCheckoutUiState.Failed(previous.error)
            is AppResult.Success -> {
                val recovered = previous.value
                if (recovered != null) {
                    activeSubscriptionId = recovered.id
                    when (val check = getSubscriptionById(recovered.id)) {
                        is AppResult.Success -> {
                            if (check.value.entitlementActive || check.value.status == SubscriptionStatus.ACTIVE) {
                                mutableState.value = IndividualCheckoutUiState.Confirmed(check.value)
                            } else {
                                confirm(recovered)
                            }
                        }
                        is AppResult.Failure -> confirm(recovered)
                    }
                } else when (val result = prepare(plan, capacity)) {
                    is AppResult.Success -> {
                        activeSubscriptionId = result.value.id
                        confirm(result.value)
                    }
                    is AppResult.Failure -> mutableState.value = IndividualCheckoutUiState.Failed(result.error)
                }
            }
        }
    }

    private fun confirm(value: IndividualPurchase) {
        activeSubscriptionId = value.id
        mutableState.value = if (value.status == "PENDING_PAYMENT") IndividualCheckoutUiState.Confirm(value)
            else IndividualCheckoutUiState.NotPending
    }

    fun pay() {
        val value = (mutableState.value as? IndividualCheckoutUiState.Confirm)?.purchase ?: return
        activeSubscriptionId = value.id
        launchOperation {
            mutableState.value = when (val result = checkout(value.id)) {
                is AppResult.Success -> IndividualCheckoutUiState.Ready(result.value, value.id)
                is AppResult.Failure -> IndividualCheckoutUiState.Failed(result.error)
            }
        }
    }

    fun onBrowserOpened(subscriptionId: String) {
        activeSubscriptionId = subscriptionId
        mutableState.value = IndividualCheckoutUiState.Verifying(subscriptionId)
    }

    fun onAppResumed() {
        val id = activeSubscriptionId ?: return
        val current = mutableState.value
        if (current is IndividualCheckoutUiState.Ready || current is IndividualCheckoutUiState.Verifying) {
            startPolling(id)
        }
    }

    fun onAppPaused() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun checkStatus() {
        val id = activeSubscriptionId ?: return
        startPolling(id)
    }

    fun startPolling(subscriptionId: String) {
        activeSubscriptionId = subscriptionId
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            mutableState.value = IndividualCheckoutUiState.Verifying(subscriptionId)
            var elapsed = 0L
            while (isActive && elapsed <= maxPollDurationMs) {
                when (val result = getSubscriptionById(subscriptionId)) {
                    is AppResult.Success -> {
                        val sub = result.value
                        if (sub.entitlementActive || sub.status == SubscriptionStatus.ACTIVE) {
                            mutableState.value = IndividualCheckoutUiState.Confirmed(sub)
                            return@launch
                        }
                    }
                    is AppResult.Failure -> {
                        if (result.error is AppError.Unauthorized) {
                            mutableState.value = IndividualCheckoutUiState.Failed(result.error)
                            return@launch
                        }
                    }
                }
                if (elapsed >= maxPollDurationMs) break
                delay(pollIntervalMs)
                elapsed += pollIntervalMs
            }
            if (isActive) {
                mutableState.value = IndividualCheckoutUiState.Unconfirmed(subscriptionId)
            }
        }
    }

    fun dismiss() {
        pollingJob?.cancel()
        pollingJob = null
        if (mutableState.value !is IndividualCheckoutUiState.Working) {
            mutableState.value = IndividualCheckoutUiState.Idle
        }
    }

    fun browserFailed() {
        pollingJob?.cancel()
        pollingJob = null
        mutableState.value = IndividualCheckoutUiState.Failed(AppError.Unknown())
    }

    private fun launchOperation(block: suspend () -> Unit) {
        if (mutableState.value is IndividualCheckoutUiState.Working) return
        mutableState.value = IndividualCheckoutUiState.Working
        viewModelScope.launch {
            try { block() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (exception: Exception) { mutableState.value = IndividualCheckoutUiState.Failed(AppError.Unknown(exception)) }
        }
    }
}
