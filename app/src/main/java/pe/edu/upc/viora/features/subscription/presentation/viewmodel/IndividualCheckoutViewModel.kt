package pe.edu.upc.viora.features.subscription.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
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
    private val clock: Clock,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    constructor(
        prepare: PrepareIndividualPurchaseUseCase,
        resume: ResumeIndividualPurchaseUseCase,
        checkout: CreateIndividualCheckoutUseCase,
        getSubscriptionById: GetSubscriptionByIdUseCase,
        getCurrentSubscription: GetCurrentSubscriptionUseCase,
    ) : this(
        prepare,
        resume,
        checkout,
        getSubscriptionById,
        getCurrentSubscription,
        Clock.systemDefaultZone(),
        SavedStateHandle(),
    )

    private val mutableState = MutableStateFlow<IndividualCheckoutUiState>(IndividualCheckoutUiState.Idle)
    val uiState = mutableState.asStateFlow()

    private var activeSubscriptionId: String?
        get() = savedStateHandle[KEY_ACTIVE_SUBSCRIPTION_ID] ?: inMemoryActiveSubscriptionId
        set(value) {
            inMemoryActiveSubscriptionId = value
            if (value != null) {
                savedStateHandle[KEY_ACTIVE_SUBSCRIPTION_ID] = value
            } else {
                savedStateHandle.remove<String>(KEY_ACTIVE_SUBSCRIPTION_ID)
            }
        }
    private var inMemoryActiveSubscriptionId: String? = null
    private var userDismissed: Boolean = false

    private var pollingJob: Job? = null
    private var recoveryJob: Job? = null
    private var visible = false
    var pollIntervalMs: Long = 5_000L
    var maxPollDurationMs: Long = 60_000L

    fun start(plan: ProducerPlanCode, capacity: PlanCapacity) = launchOperation {
        userDismissed = false
        // A durable operation always takes precedence over a new screen selection.
        when (val previous = resume()) {
            is AppResult.Failure -> mutableState.value = IndividualCheckoutUiState.Failed(previous.error)
            is AppResult.Success -> {
                val recovered = previous.value
                if (recovered != null) {
                    activeSubscriptionId = recovered.id
                    when (val check = getSubscriptionById(recovered.id)) {
                        is AppResult.Success -> {
                            if (check.value.entitlementActive) {
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
        userDismissed = false
        activeSubscriptionId = value.id
        launchOperation {
            mutableState.value = when (val result = checkout(value.id)) {
                is AppResult.Success -> IndividualCheckoutUiState.Ready(result.value, value.id)
                is AppResult.Failure -> IndividualCheckoutUiState.Failed(result.error)
            }
        }
    }

    fun onBrowserOpened(subscriptionId: String) {
        userDismissed = false
        activeSubscriptionId = subscriptionId
        mutableState.value = IndividualCheckoutUiState.Verifying(subscriptionId)
    }

    fun onAppResumed() {
        visible = true
        if (userDismissed) return
        val id = activeSubscriptionId
        if (id != null) {
            val current = mutableState.value
            if (current is IndividualCheckoutUiState.Ready ||
                current is IndividualCheckoutUiState.Verifying ||
                current is IndividualCheckoutUiState.Idle
            ) {
                startPolling(id)
            }
        } else {
            if (recoveryJob?.isActive == true) return
            recoveryJob = viewModelScope.launch {
                when (val previous = resume()) {
                    is AppResult.Success -> {
                        val recovered = previous.value
                        if (recovered != null && visible && !userDismissed) {
                            activeSubscriptionId = recovered.id
                            startPolling(recovered.id)
                        }
                    }
                    is AppResult.Failure -> Unit
                }
            }
        }
    }

    fun onAppPaused() {
        visible = false
        recoveryJob?.cancel()
        recoveryJob = null
        pollingJob?.cancel()
        pollingJob = null
    }

    fun checkStatus() {
        val id = activeSubscriptionId ?: return
        userDismissed = false
        startPolling(id)
    }

    fun startPolling(subscriptionId: String) {
        activeSubscriptionId = subscriptionId
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            mutableState.value = IndividualCheckoutUiState.Verifying(subscriptionId)
            val startTimeMs = clock.millis()
            withTimeoutOrNull(maxPollDurationMs) {
                loop@ while (isActive) {
                    if (clock.millis() - startTimeMs >= maxPollDurationMs) {
                        break@loop
                    }
                    var backoffNeeded = false
                    var retryAfterMillis = 0L
                    when (val result = getSubscriptionById(subscriptionId)) {
                        is AppResult.Success -> {
                            val sub = result.value
                            if (sub.entitlementActive) {
                                mutableState.value = IndividualCheckoutUiState.Confirmed(sub)
                                break@loop
                            }
                        }
                        is AppResult.Failure -> {
                            val error = result.error
                            when {
                                error is AppError.Unauthorized || error is AppError.Forbidden -> {
                                    mutableState.value = IndividualCheckoutUiState.Failed(error)
                                    break@loop
                                }
                                error is AppError.NotFound -> {
                                    mutableState.value = IndividualCheckoutUiState.Unconfirmed(subscriptionId)
                                    break@loop
                                }
                                error is AppError.Server -> {
                                    backoffNeeded = true
                                }
                                error is AppError.RateLimited -> {
                                    backoffNeeded = true
                                    retryAfterMillis = error.retryAfterMillis ?: 0L
                                }
                            }
                        }
                    }

                    val desiredDelay = if (backoffNeeded) pollIntervalMs * 2 else pollIntervalMs
                    val waitMs = maxOf(desiredDelay, retryAfterMillis)
                    val elapsed = clock.millis() - startTimeMs
                    val remainingMs = maxPollDurationMs - elapsed
                    if (remainingMs <= 0) {
                        break@loop
                    }
                    delay(minOf(waitMs, remainingMs))
                }
            }
            if (isActive && mutableState.value is IndividualCheckoutUiState.Verifying) {
                mutableState.value = IndividualCheckoutUiState.Unconfirmed(subscriptionId)
            }
        }
    }

    fun dismiss() {
        userDismissed = true
        recoveryJob?.cancel()
        recoveryJob = null
        pollingJob?.cancel()
        pollingJob = null
        activeSubscriptionId = null
        if (mutableState.value !is IndividualCheckoutUiState.Working) {
            mutableState.value = IndividualCheckoutUiState.Idle
        }
    }

    fun browserFailed() {
        pollingJob?.cancel()
        pollingJob = null
        activeSubscriptionId = null
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

    private companion object {
        const val KEY_ACTIVE_SUBSCRIPTION_ID = "checkout_active_sub_id"
    }
}
