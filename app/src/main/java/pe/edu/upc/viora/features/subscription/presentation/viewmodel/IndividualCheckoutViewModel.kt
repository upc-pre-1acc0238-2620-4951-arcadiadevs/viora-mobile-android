package pe.edu.upc.viora.features.subscription.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
) : ViewModel() {
    private val mutableState = MutableStateFlow<IndividualCheckoutUiState>(IndividualCheckoutUiState.Idle)
    val uiState = mutableState.asStateFlow()

    fun start(plan: ProducerPlanCode, capacity: PlanCapacity) = launchOperation {
        // A durable operation always takes precedence over a new screen selection.
        when (val previous = resume()) {
            is AppResult.Failure -> mutableState.value = IndividualCheckoutUiState.Failed(previous.error)
            is AppResult.Success -> {
                val recovered = previous.value
                if (recovered != null) confirm(recovered)
                else when (val result = prepare(plan, capacity)) {
                    is AppResult.Success -> confirm(result.value)
                    is AppResult.Failure -> mutableState.value = IndividualCheckoutUiState.Failed(result.error)
                }
            }
        }
    }

    private fun confirm(value: IndividualPurchase) {
        mutableState.value = if (value.status == "PENDING_PAYMENT") IndividualCheckoutUiState.Confirm(value)
            else IndividualCheckoutUiState.NotPending
    }

    fun pay() {
        val value = (mutableState.value as? IndividualCheckoutUiState.Confirm)?.purchase ?: return
        launchOperation {
            mutableState.value = when (val result = checkout(value.id)) {
                is AppResult.Success -> IndividualCheckoutUiState.Ready(result.value)
                is AppResult.Failure -> IndividualCheckoutUiState.Failed(result.error)
            }
        }
    }

    fun dismiss() {
        if (mutableState.value !is IndividualCheckoutUiState.Working) mutableState.value = IndividualCheckoutUiState.Idle
    }

    fun browserFailed() { mutableState.value = IndividualCheckoutUiState.Failed(AppError.Unknown()) }

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
