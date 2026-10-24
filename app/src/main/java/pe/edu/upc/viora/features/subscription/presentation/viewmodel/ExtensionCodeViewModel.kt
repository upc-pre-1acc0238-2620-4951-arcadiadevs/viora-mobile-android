package pe.edu.upc.viora.features.subscription.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.subscription.application.usecase.RedeemExtensionCodeUseCase
import pe.edu.upc.viora.features.subscription.domain.valueobject.ExtensionCode
import pe.edu.upc.viora.features.subscription.presentation.state.ExtensionCodeUiState
import pe.edu.upc.viora.features.subscription.presentation.state.RedeemFailure

/**
 * P103 · Ampliar con código: redeems an extension code of the license that sponsors the producer.
 * Every reason a code does not add hectares comes back as one validation error, shown as
 * "Este código no suma".
 */
@HiltViewModel
class ExtensionCodeViewModel @Inject constructor(
    private val redeemCode: RedeemExtensionCodeUseCase,
) : ViewModel() {

    private val mutableState = MutableStateFlow(ExtensionCodeUiState())
    val uiState: StateFlow<ExtensionCodeUiState> = mutableState.asStateFlow()

    fun setCode(text: String) = mutableState.update {
        it.copy(code = ExtensionCode.normalize(text).take(ExtensionCode.LENGTH), failure = null)
    }

    /** "Probar otro código": back to the empty field. */
    fun tryAnotherCode() = mutableState.update { ExtensionCodeUiState() }

    fun redeem() {
        val current = mutableState.value
        if (!current.canRedeem) return
        val code = ExtensionCode.of(current.code) ?: return
        mutableState.update { it.copy(isRedeeming = true, failure = null) }
        viewModelScope.launch {
            when (val result = redeemCode(code)) {
                is AppResult.Success -> mutableState.update { it.copy(isRedeeming = false, applied = result.value) }
                is AppResult.Failure -> mutableState.update { it.copy(isRedeeming = false, failure = result.error.toFailure()) }
            }
        }
    }

    private fun AppError.toFailure(): RedeemFailure = when (this) {
        is AppError.Validation -> RedeemFailure.NotRedeemable
        is AppError.RateLimited -> RedeemFailure.TooManyAttempts
        else -> RedeemFailure.Other(this)
    }
}
