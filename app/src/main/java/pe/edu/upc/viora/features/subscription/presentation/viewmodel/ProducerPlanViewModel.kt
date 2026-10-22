package pe.edu.upc.viora.features.subscription.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.subscription.application.usecase.ObserveProducerPlanCatalogUseCase
import pe.edu.upc.viora.features.subscription.application.usecase.RefreshProducerPlanCatalogUseCase
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerPlanCatalog
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity
import pe.edu.upc.viora.features.subscription.presentation.state.ProducerPlanUiState

/** Restores selection only; server-backed policy remains the source of the displayed estimate. */
@HiltViewModel
class ProducerPlanViewModel @Inject constructor(
    private val observe: ObserveProducerPlanCatalogUseCase,
    private val refreshCatalog: RefreshProducerPlanCatalogUseCase,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private data class Refresh(val busy: Boolean = false, val succeeded: Boolean = false, val error: AppError? = null)
    private val catalog = MutableStateFlow(ProducerPlanCatalog(emptyList(), null))
    private val refreshState = MutableStateFlow(Refresh())
    private val readError = MutableStateFlow<AppError?>(null)
    private val selectedCode = savedState.getStateFlow<String?>(PLAN_CODE, null)
    private val selectedCapacity = savedState.getStateFlow<String?>(QUOTA_HA, null)
    private var observation: Job? = null

    val uiState = combine(catalog, refreshState, selectedCode, selectedCapacity, readError) { data, request, code, quota, failure ->
        when {
            failure != null -> ProducerPlanUiState.Failed(failure)
            data.fetchedAt == null -> request.error?.let { ProducerPlanUiState.Failed(it) } ?: ProducerPlanUiState.Loading
            data.offers.isEmpty() -> ProducerPlanUiState.Empty(data.fetchedAt, request.busy, request.error)
            else -> {
                val offer = data.offers.find { it.code.value == code } ?: data.offers.first()
                val capacity = quota?.let { runCatching { PlanCapacity.of(it.toBigDecimal()) }.getOrNull() }
                    ?.takeIf(offer::accepts) ?: offer.minimumQuota
                ProducerPlanUiState.Content(data.offers, offer, capacity, data.fetchedAt, request.busy,
                    !request.succeeded, request.error)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ProducerPlanUiState.Loading)

    init {
        observeCatalog()
        refresh()
    }

    private fun observeCatalog() {
        observation?.cancel()
        observation = viewModelScope.launch {
            try {
                observe().collect { catalog.value = it; readError.value = null }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                readError.value = AppError.Unknown(exception)
            }
        }
    }

    fun refresh() {
        if (refreshState.value.busy) return
        refreshState.value = Refresh(busy = true)
        if (observation?.isActive != true) observeCatalog()
        viewModelScope.launch {
            try {
                refreshState.value = when (val result = refreshCatalog()) {
                    is AppResult.Success -> {
                        if (observation?.isActive != true) observeCatalog()
                        Refresh(succeeded = true)
                    }
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
        if (catalog.value.offers.none { it.code.value == code } || code == selectedCode.value) return
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
        const val PLAN_CODE = "producer_plan_code"
        const val QUOTA_HA = "producer_plan_quota_ha"
    }
}
