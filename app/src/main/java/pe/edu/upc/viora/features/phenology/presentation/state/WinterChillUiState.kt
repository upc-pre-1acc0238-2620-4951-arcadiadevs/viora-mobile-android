package pe.edu.upc.viora.features.phenology.presentation.state

import java.time.Instant
import java.time.LocalDate
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.phenology.domain.entity.ChillCurvePoint
import pe.edu.upc.viora.features.phenology.domain.entity.EnsoRiskLevel
import pe.edu.upc.viora.features.phenology.domain.entity.WinterSeasonState

sealed interface WinterChillUiState {

    data object Loading : WinterChillUiState

    data class Error(val error: AppError) : WinterChillUiState

    data class Content(
        val plotName: String,
        val varietyName: String,
        val accumulatedPortions: Int,
        val thresholdPortions: Int,
        val daysAbove24Celsius: Int,
        val seasonState: WinterSeasonState,
        val ensoRisk: EnsoRiskLevel,
        val projectedCompletionDate: LocalDate?,
        val previousWinterCompletionDate: LocalDate?,
        val curvePoints: List<ChillCurvePoint>,
        val lastSync: Instant?,
        val isRefreshing: Boolean,
        val offline: Boolean,
    ) : WinterChillUiState {
        val portionsRemaining: Int
            get() = (thresholdPortions - accumulatedPortions).coerceAtLeast(0)

        val progressFraction: Float
            get() = if (thresholdPortions > 0) {
                (accumulatedPortions.toFloat() / thresholdPortions).coerceIn(0f, 1f)
            } else {
                0f
            }

        val isCompleted: Boolean
            get() = seasonState == WinterSeasonState.COMPLETED || accumulatedPortions >= thresholdPortions
    }
}
