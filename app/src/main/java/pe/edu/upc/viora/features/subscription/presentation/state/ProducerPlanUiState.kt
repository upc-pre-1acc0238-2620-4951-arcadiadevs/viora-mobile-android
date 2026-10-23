package pe.edu.upc.viora.features.subscription.presentation.state

import java.time.Instant
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerPlanOffer
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity

/** Browsing an offer is separate from contracting it or authorizing access. */
sealed interface ProducerPlanUiState {
    data object Loading : ProducerPlanUiState
    data class Failed(val error: AppError) : ProducerPlanUiState
    data class Empty(val fetchedAt: Instant, val isRefreshing: Boolean, val error: AppError?) : ProducerPlanUiState
    data class Content(
        val offers: List<ProducerPlanOffer>,
        val selectedOffer: ProducerPlanOffer,
        val capacity: PlanCapacity,
        val fetchedAt: Instant,
        val isRefreshing: Boolean,
        val isSavedCopy: Boolean,
        val error: AppError?,
    ) : ProducerPlanUiState {
        val estimate get() = selectedOffer.estimate(capacity)
        val canDecrease get() = capacity.hectares > selectedOffer.minimumQuota.hectares
    }
}
