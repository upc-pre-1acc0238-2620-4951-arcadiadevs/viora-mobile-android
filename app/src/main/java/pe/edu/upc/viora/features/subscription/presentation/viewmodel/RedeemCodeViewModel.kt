package pe.edu.upc.viora.features.subscription.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.features.subscription.application.usecase.RedeemCooperativeCodeUseCase
import pe.edu.upc.viora.features.subscription.domain.entity.RedemptionOutcome
import pe.edu.upc.viora.features.subscription.domain.valueobject.InvitationCode
import pe.edu.upc.viora.features.subscription.presentation.state.RedeemCodeUiState
import pe.edu.upc.viora.features.subscription.presentation.state.RedeemFailure

@HiltViewModel
class RedeemCodeViewModel @Inject constructor(
    private val redeemCode: RedeemCooperativeCodeUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RedeemCodeUiState())
    val uiState: StateFlow<RedeemCodeUiState> = _uiState.asStateFlow()

    init {
        // `RedeemCodeRoute.code`: the code saved in T02c is redeemed right away (P05 · Validando);
        // a malformed one waits in the field for the producer to fix it.
        savedStateHandle.get<String>("code")?.let { saved ->
            setCode(saved)
            redeem()
        }
    }

    fun setCode(text: String) = _uiState.update {
        it.copy(code = InvitationCode.normalize(text).take(InvitationCode.LENGTH), failure = null)
    }

    /** P05 rejected "Probar otro código": back to the empty field. */
    fun tryAnotherCode() = _uiState.update { RedeemCodeUiState() }

    fun redeem() {
        val current = _uiState.value
        if (!current.canRedeem) return
        val code = InvitationCode.of(current.code) ?: return
        _uiState.update { it.copy(isRedeeming = true, failure = null) }
        viewModelScope.launch {
            when (val outcome = redeemCode(code)) {
                is RedemptionOutcome.Redeemed -> _uiState.update { it.copy(isRedeeming = false, redeemed = outcome.membership) }
                else -> _uiState.update { it.copy(isRedeeming = false, failure = outcome.toFailure()) }
            }
        }
    }

    private fun RedemptionOutcome.toFailure(): RedeemFailure = when (this) {
        RedemptionOutcome.NotRedeemable -> RedeemFailure.NotRedeemable
        RedemptionOutcome.AlreadySubscribed -> RedeemFailure.AlreadySubscribed
        RedemptionOutcome.TooManyAttempts -> RedeemFailure.TooManyAttempts
        is RedemptionOutcome.Failed -> RedeemFailure.Other(error)
        is RedemptionOutcome.Redeemed -> error("A redeemed code is not a failure")
    }
}
