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
import pe.edu.upc.viora.features.subscription.application.usecase.PreviewCooperativeCodeUseCase
import pe.edu.upc.viora.features.subscription.application.usecase.RedeemCooperativeCodeUseCase
import pe.edu.upc.viora.features.subscription.domain.entity.CodePreviewOutcome
import pe.edu.upc.viora.features.subscription.domain.entity.RedemptionOutcome
import pe.edu.upc.viora.features.subscription.domain.valueobject.InvitationCode
import pe.edu.upc.viora.features.subscription.presentation.state.RedeemCodeUiState
import pe.edu.upc.viora.features.subscription.presentation.state.RedeemFailure

/**
 * P05 → P05b → P06. "Canjear código" only asks what the code offers; the code is spent when the producer accepts to
 * share their data in P05b. "Ahora no" leaves no link and the code stays free.
 */
@HiltViewModel
class RedeemCodeViewModel @Inject constructor(
    private val previewCode: PreviewCooperativeCodeUseCase,
    private val redeemCode: RedeemCooperativeCodeUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RedeemCodeUiState())
    val uiState: StateFlow<RedeemCodeUiState> = _uiState.asStateFlow()

    init {
        // `RedeemCodeRoute.code`: the code saved in T02c is checked right away (P05 · Validando) and opens P05b;
        // a malformed one waits in the field for the producer to fix it.
        savedStateHandle.get<String>("code")?.let { saved ->
            setCode(saved)
            validate()
        }
    }

    fun setCode(text: String) = _uiState.update {
        it.copy(code = InvitationCode.normalize(text).take(InvitationCode.LENGTH), failure = null)
    }

    /** P05 "Probar otro código", and P05b "Ahora no" or back: the empty field again, nothing redeemed. */
    fun tryAnotherCode() = _uiState.update { RedeemCodeUiState() }

    /** P05 "Canjear código": asks what the code offers, without spending it. */
    fun validate() {
        val current = _uiState.value
        if (!current.canValidate) return
        val code = InvitationCode.of(current.code) ?: return
        _uiState.update { it.copy(isValidating = true, failure = null, preview = null) }
        viewModelScope.launch {
            when (val outcome = previewCode(code)) {
                is CodePreviewOutcome.Previewed -> _uiState.update { it.copy(isValidating = false, preview = outcome.preview) }
                else -> _uiState.update { it.copy(isValidating = false, failure = outcome.toFailure()) }
            }
        }
    }

    /** P05b "Aceptar y activar": redeems the code under the consent text the producer just read. */
    fun accept() {
        val current = _uiState.value
        val preview = current.preview ?: return
        if (current.isAccepting) return
        val code = InvitationCode.of(current.code) ?: return
        _uiState.update { it.copy(isAccepting = true, failure = null) }
        viewModelScope.launch {
            when (val outcome = redeemCode(code, preview)) {
                is RedemptionOutcome.Redeemed -> _uiState.update { it.copy(isAccepting = false, redeemed = outcome.membership) }
                // The text changed since P05b opened: show the new one, it must be read and accepted again.
                RedemptionOutcome.ConsentOutdated -> {
                    _uiState.update { it.copy(isAccepting = false) }
                    validate()
                }
                // The code or the plan changed meanwhile: back to P05 with its own variant.
                RedemptionOutcome.NotRedeemable, RedemptionOutcome.AlreadySubscribed ->
                    _uiState.update { it.copy(isAccepting = false, preview = null, failure = outcome.toFailure()) }
                else -> _uiState.update { it.copy(isAccepting = false, failure = outcome.toFailure()) }
            }
        }
    }

    private fun CodePreviewOutcome.toFailure(): RedeemFailure = when (this) {
        CodePreviewOutcome.NotRedeemable -> RedeemFailure.NotRedeemable
        CodePreviewOutcome.AlreadySubscribed -> RedeemFailure.AlreadySubscribed
        CodePreviewOutcome.TooManyAttempts -> RedeemFailure.TooManyAttempts
        is CodePreviewOutcome.Failed -> RedeemFailure.Other(error)
        is CodePreviewOutcome.Previewed -> error("A previewed code is not a failure")
    }

    private fun RedemptionOutcome.toFailure(): RedeemFailure = when (this) {
        RedemptionOutcome.NotRedeemable -> RedeemFailure.NotRedeemable
        RedemptionOutcome.AlreadySubscribed -> RedeemFailure.AlreadySubscribed
        RedemptionOutcome.TooManyAttempts -> RedeemFailure.TooManyAttempts
        is RedemptionOutcome.Failed -> RedeemFailure.Other(error)
        RedemptionOutcome.ConsentOutdated, is RedemptionOutcome.Redeemed -> error("Not a failure of the redemption")
    }
}
