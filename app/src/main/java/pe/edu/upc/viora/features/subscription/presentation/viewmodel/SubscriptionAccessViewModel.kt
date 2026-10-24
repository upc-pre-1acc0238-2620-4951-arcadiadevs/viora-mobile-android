package pe.edu.upc.viora.features.subscription.presentation.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.subscription.application.usecase.GetCurrentSubscriptionUseCase
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerSubscription

@HiltViewModel
class SubscriptionAccessViewModel @Inject constructor(
    private val current: GetCurrentSubscriptionUseCase,
) : ViewModel() {
    private val state = MutableStateFlow<AppResult<ProducerSubscription?>?>(null)
    val uiState = state.asStateFlow()

    suspend fun refresh() {
        state.value = null
        state.value = current()
    }
}
