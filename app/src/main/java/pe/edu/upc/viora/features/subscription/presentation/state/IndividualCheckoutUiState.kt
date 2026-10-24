package pe.edu.upc.viora.features.subscription.presentation.state

import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.subscription.domain.entity.IndividualPurchase
import pe.edu.upc.viora.features.subscription.domain.entity.IndividualCheckout
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerSubscription

sealed interface IndividualCheckoutUiState {
    data object Idle : IndividualCheckoutUiState
    data object Working : IndividualCheckoutUiState
    data class Confirm(val purchase: IndividualPurchase) : IndividualCheckoutUiState
    data class Ready(val link: IndividualCheckout, val subscriptionId: String = "") : IndividualCheckoutUiState
    data class Verifying(val subscriptionId: String) : IndividualCheckoutUiState
    data class Confirmed(val subscription: ProducerSubscription) : IndividualCheckoutUiState
    data class Unconfirmed(val subscriptionId: String) : IndividualCheckoutUiState
    data class Rejected(val subscriptionId: String) : IndividualCheckoutUiState
    data class Failed(val error: AppError) : IndividualCheckoutUiState
    data object NotPending : IndividualCheckoutUiState
}
