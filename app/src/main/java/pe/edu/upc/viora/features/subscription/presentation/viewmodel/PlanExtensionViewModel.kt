package pe.edu.upc.viora.features.subscription.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.subscription.application.usecase.CreateExtensionCheckoutUseCase
import pe.edu.upc.viora.features.subscription.application.usecase.GetCurrentSubscriptionUseCase
import pe.edu.upc.viora.features.subscription.application.usecase.GetPlanExtensionTermsUseCase
import pe.edu.upc.viora.features.subscription.application.usecase.GetQuotaExtensionUseCase
import pe.edu.upc.viora.features.subscription.application.usecase.PendingExtensionPaymentUseCase
import pe.edu.upc.viora.features.subscription.application.usecase.RequestPaidExtensionUseCase
import pe.edu.upc.viora.features.subscription.domain.entity.ExtensionAction
import pe.edu.upc.viora.features.subscription.domain.entity.QuotaExtension
import pe.edu.upc.viora.features.subscription.presentation.state.PlanExtensionUiState

/**
 * P102 · Ampliar mi plan. It asks the backend how the current plan can grow: a paid plan picks the
 * hectares here and pays them in Mercado Pago; any other plan is handed over through
 * [PlanExtensionUiState.Redirect]. Back from the browser it waits for the backend to confirm the
 * payment, as P04 does, and ends in "Ampliación aprobada" or "El pago no se completó".
 */
@HiltViewModel
class PlanExtensionViewModel @Inject constructor(
    private val getCurrentSubscription: GetCurrentSubscriptionUseCase,
    private val getTerms: GetPlanExtensionTermsUseCase,
    private val requestExtension: RequestPaidExtensionUseCase,
    private val getExtension: GetQuotaExtensionUseCase,
    private val createCheckout: CreateExtensionCheckoutUseCase,
    private val pendingPayment: PendingExtensionPaymentUseCase,
) : ViewModel() {

    private val mutableState = MutableStateFlow<PlanExtensionUiState>(PlanExtensionUiState.Loading)
    val uiState: StateFlow<PlanExtensionUiState> = mutableState.asStateFlow()

    /** The extension requested for each selection, so going back and forth reuses it. */
    private val requested = mutableMapOf<BigDecimal, QuotaExtension>()
    private val requestKeys = mutableMapOf<BigDecimal, String>()
    private var paying: QuotaExtension? = null
    private var pollingJob: Job? = null
    private var visible = false

    var pollIntervalMs: Long = 4_000L
    var maxPollDurationMs: Long = 60_000L
    private var started = false

    /**
     * Opens P102, or the result of the payment when [returning] from Mercado Pago. Later calls do
     * nothing, so a recomposition or a configuration change keeps the current state.
     */
    fun start(returning: Boolean) {
        if (started) return
        started = true
        if (returning) resumePendingPayment() else load()
    }

    fun load() {
        mutableState.value = PlanExtensionUiState.Loading
        viewModelScope.launch {
            val current = when (val result = getCurrentSubscription()) {
                is AppResult.Success -> result.value
                is AppResult.Failure -> return@launch fail(result.error)
            }
            if (current == null || !current.entitlementActive) {
                mutableState.value = PlanExtensionUiState.Redirect(ExtensionAction.NONE)
                return@launch
            }
            when (val terms = getTerms(current.id)) {
                is AppResult.Failure -> fail(terms.error)
                is AppResult.Success -> {
                    val value = terms.value
                    val pricing = value.pricing
                    mutableState.value = if (value.action == ExtensionAction.PAY && pricing != null) {
                        PlanExtensionUiState.Selecting(
                            subscriptionId = value.subscriptionId,
                            quotaHectares = value.quotaHectares,
                            endsAt = value.endsAt,
                            pricing = pricing,
                            addedHectares = pricing.minimum.max(DEFAULT_HECTARES).stripTrailingZeros(),
                        )
                    } else {
                        PlanExtensionUiState.Redirect(value.action)
                    }
                }
            }
        }
    }

    fun increase() = change { it.addedHectares.add(it.pricing.increment) }

    fun decrease() = change { (it.addedHectares.subtract(it.pricing.increment)).max(it.pricing.minimum) }

    private fun change(next: (PlanExtensionUiState.Selecting) -> BigDecimal) = mutableState.update { state ->
        if (state is PlanExtensionUiState.Selecting && !state.isWorking) {
            // Stripped, so 1 and 1.0 are the same selection (and the same requested extension).
            state.copy(addedHectares = next(state).stripTrailingZeros(), error = null)
        } else state
    }

    /** Requests the extension (once per selection) and asks for its Mercado Pago link. */
    fun pay() {
        val state = mutableState.value as? PlanExtensionUiState.Selecting ?: return
        if (state.isWorking) return
        mutableState.value = state.copy(isWorking = true, error = null)
        viewModelScope.launch {
            val added = state.addedHectares
            val extension = requested[added] ?: when (val result = requestExtension(
                state.subscriptionId, added, requestKeys.getOrPut(added) { UUID.randomUUID().toString() },
            )) {
                is AppResult.Success -> result.value.also { requested[added] = it }
                is AppResult.Failure -> {
                    mutableState.value = state.copy(isWorking = false, error = result.error)
                    return@launch
                }
            }
            when (val link = createCheckout(extension, UUID.randomUUID().toString())) {
                is AppResult.Success -> {
                    paying = extension
                    mutableState.value = state.copy(isWorking = false, checkout = link.value)
                }
                is AppResult.Failure -> mutableState.value = state.copy(isWorking = false, error = link.error)
            }
        }
    }

    /** The browser took the producer to Mercado Pago: show "Confirmando tu pago" until they return. */
    fun onBrowserOpened() {
        mutableState.value = PlanExtensionUiState.Verifying
    }

    fun onBrowserFailed() = mutableState.update {
        if (it is PlanExtensionUiState.Selecting) it.copy(checkout = null, error = AppError.Unknown()) else it
    }

    fun onResumed() {
        visible = true
        val state = mutableState.value
        if (state is PlanExtensionUiState.Verifying || state is PlanExtensionUiState.Unconfirmed) poll()
    }

    fun onPaused() {
        visible = false
        pollingJob?.cancel()
        pollingJob = null
    }

    /** "Revisar de nuevo" after the wait ran out. */
    fun checkAgain() = poll()

    /** "Intentar de nuevo" after a rejected payment: back to P102 with the same hectares. */
    fun retry() {
        paying = null
        load()
    }

    private fun resumePendingPayment() {
        mutableState.value = PlanExtensionUiState.Verifying
        viewModelScope.launch {
            val pending = pendingPayment.find()
            if (pending == null) {
                load()
                return@launch
            }
            when (val result = getExtension(pending.first, pending.second)) {
                is AppResult.Success -> {
                    paying = result.value
                    settle(result.value)
                    if (mutableState.value is PlanExtensionUiState.Verifying && visible) poll()
                }
                is AppResult.Failure -> {
                    pendingPayment.forget()
                    fail(result.error)
                }
            }
        }
    }

    private fun poll() {
        val extension = paying ?: return resumePendingPayment()
        pollingJob?.cancel()
        mutableState.value = PlanExtensionUiState.Verifying
        pollingJob = viewModelScope.launch {
            var waited = 0L
            while (waited <= maxPollDurationMs) {
                val result = getExtension(extension.subscriptionId, extension.id)
                if (result is AppResult.Success && settle(result.value)) return@launch
                delay(pollIntervalMs)
                waited += pollIntervalMs
            }
            mutableState.value = PlanExtensionUiState.Unconfirmed
        }
    }

    /** Shows the final state of [extension]; false while its payment is still unresolved. */
    private suspend fun settle(extension: QuotaExtension): Boolean = when {
        extension.isApplied -> {
            pendingPayment.forget()
            mutableState.value = PlanExtensionUiState.Applied(extension)
            true
        }
        extension.isRejected -> {
            mutableState.value = PlanExtensionUiState.Rejected
            true
        }
        else -> false
    }

    private fun fail(error: AppError) {
        mutableState.value = PlanExtensionUiState.Failed(error)
    }

    private companion object {
        /** P102 opens on one hectare, the smallest step that usually fits a new plot. */
        val DEFAULT_HECTARES: BigDecimal = BigDecimal.ONE
    }
}
