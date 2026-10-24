package pe.edu.upc.viora.features.subscription.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import java.time.Clock
import java.time.temporal.ChronoUnit
import pe.edu.upc.viora.features.subscription.application.usecase.GetCurrentSubscriptionUseCase
import pe.edu.upc.viora.features.subscription.application.usecase.ObserveProducerPlanCatalogUseCase
import pe.edu.upc.viora.features.subscription.application.usecase.RefreshProducerPlanCatalogUseCase
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerPlanCatalog
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity
import pe.edu.upc.viora.features.subscription.presentation.state.ProducerPlanUiState
import pe.edu.upc.viora.features.subscription.presentation.state.RenewalInfo

/** Restores selection only; server-backed policy remains the source of the displayed estimate. */
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ProducerPlanViewModel @Inject constructor(
    private val observe: ObserveProducerPlanCatalogUseCase,
    private val refreshCatalog: RefreshProducerPlanCatalogUseCase,
    private val savedState: SavedStateHandle,
    private val getCurrentSubscription: GetCurrentSubscriptionUseCase,
    private val clock: Clock,
) : ViewModel() {
    private data class Refresh(val busy: Boolean = false, val succeeded: Boolean = false, val error: AppError? = null)
    private data class Observation(val catalog: ProducerPlanCatalog? = null, val error: AppError? = null)
    // Retry changes the upstream only while uiState has subscribers; it never launches a separate reader.
    private val observationVersion = MutableStateFlow(0L)
    private val catalog = observationVersion.flatMapLatest {
        observe().map { Observation(catalog = it) }
            .catch { exception ->
                if (exception is CancellationException) throw exception
                emit(Observation(error = AppError.Unknown(exception)))
            }
    }
    private val refreshState = MutableStateFlow(Refresh())
    private val selectedCode = savedState.getStateFlow<String?>(PLAN_CODE, null)
    private val selectedCapacity = savedState.getStateFlow<String?>(QUOTA_HA, null)
    /** `ProducerPlanRoute.renewal`: the plan ending is read once, to start from its hectares. */
    private val renewal = MutableStateFlow<RenewalInfo?>(null)

    val uiState = combine(catalog, refreshState, selectedCode, selectedCapacity, renewal) { observation, request, code, quota, ending ->
        val data = observation.catalog
        when {
            observation.error != null -> ProducerPlanUiState.Failed(observation.error)
            data?.fetchedAt == null -> request.error?.let { ProducerPlanUiState.Failed(it) } ?: ProducerPlanUiState.Loading
            data.offers.isEmpty() -> ProducerPlanUiState.Empty(data.fetchedAt, request.busy, request.error)
            else -> {
                val offer = data.offers.find { it.code.value == code } ?: data.offers.first()
                val start = ending?.let { runCatching { PlanCapacity.of(it.currentQuota) }.getOrNull() }
                val capacity = quota?.let { runCatching { PlanCapacity.of(it.toBigDecimal()) }.getOrNull() }
                    ?.takeIf(offer::accepts) ?: start?.takeIf(offer::accepts) ?: offer.minimumQuota
                ProducerPlanUiState.Content(data.offers, offer, capacity, data.fetchedAt, request.busy,
                    !request.succeeded, request.error, ending)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), ProducerPlanUiState.Loading)

    init {
        // The hectares of the onboarding only seed the first selection; later changes win.
        savedState.get<Int>(HECTARES)?.let { if (savedState.get<String>(QUOTA_HA) == null) savedState[QUOTA_HA] = it.toString() }
        refresh()
        if (savedState.get<Boolean>(RENEWAL) == true) loadEndingPlan()
    }

    private fun loadEndingPlan() = viewModelScope.launch {
        val current = (getCurrentSubscription() as? AppResult.Success)?.value ?: return@launch
        val endsAt = current.endsAt ?: return@launch
        if (!current.entitlementActive) return@launch
        val days = ChronoUnit.DAYS.between(clock.instant(), endsAt).toInt().coerceAtLeast(0)
        renewal.value = RenewalInfo(current.capacity.hectares, endsAt, days)
    }

    fun refresh() {
        if (refreshState.value.busy) return
        refreshState.value = Refresh(busy = true)
        observationVersion.value += 1
        viewModelScope.launch {
            try {
                refreshState.value = when (val result = refreshCatalog()) {
                    is AppResult.Success -> Refresh(succeeded = true)
                    is AppResult.Failure -> Refresh(error = result.error)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                refreshState.value = Refresh(error = AppError.Unknown(exception))
            } finally {
                refreshState.value = refreshState.value.copy(busy = false)
            }
        }
    }

    fun selectOffer(code: String) {
        val state = uiState.value as? ProducerPlanUiState.Content ?: return
        if (state.offers.none { it.code.value == code } || code == selectedCode.value) return
        savedState[QUOTA_HA] = null
        savedState[PLAN_CODE] = code
    }

    fun increase() = changeCapacity(increase = true)
    fun decrease() = changeCapacity(increase = false)

    private fun changeCapacity(increase: Boolean) {
        val state = uiState.value as? ProducerPlanUiState.Content ?: return
        val step = state.selectedOffer.quotaIncrement.hectares
        val next = if (increase) state.capacity.hectares.add(step) else state.capacity.hectares.subtract(step)
        if (next < state.selectedOffer.minimumQuota.hectares) return
        savedState[PLAN_CODE] = state.selectedOffer.code.value
        savedState[QUOTA_HA] = PlanCapacity.of(next).hectares.toPlainString()
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        const val PLAN_CODE = "producer_plan_code"
        const val QUOTA_HA = "producer_plan_quota_ha"
        const val RENEWAL = "renewal"
        const val HECTARES = "hectares"
    }
}
