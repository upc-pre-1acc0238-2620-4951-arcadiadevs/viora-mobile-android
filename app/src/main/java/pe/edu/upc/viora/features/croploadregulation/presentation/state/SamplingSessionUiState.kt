package pe.edu.upc.viora.features.croploadregulation.presentation.state

import java.util.Locale
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.croploadregulation.domain.entity.SamplingSummary
import pe.edu.upc.viora.features.croploadregulation.domain.entity.TreeSample

data class SamplingSessionUiState(
    val plotId: String = "",
    val plotName: String = "",
    val campaignYear: Int = 2026,
    val samples: List<TreeSample> = emptyList(),
    val targetTreesCount: Int = 5,
    val isSubmitting: Boolean = false,
    val submissionSummary: SamplingSummary? = null,
    val isOffline: Boolean = false,
    val hasRecoveredConnection: Boolean = false,
    val error: AppError? = null,
) {
    val evaluatedTreesCount: Int
        get() = samples.size

    val treesMissing: Int
        get() = (targetTreesCount - evaluatedTreesCount).coerceAtLeast(0)

    val isRepresentative: Boolean
        get() = evaluatedTreesCount >= targetTreesCount

    val totalShootsCount: Int
        get() = samples.sumOf { it.shootsCount }

    val totalFruitsCount: Int
        get() = samples.sumOf { it.fruitSetCount }

    val meanFruitsPerShoot: Double
        get() = if (totalShootsCount > 0) totalFruitsCount.toDouble() / totalShootsCount else 0.0

    val nextTreeIdentifier: String
        get() = "A-${String.format(Locale.ROOT, "%02d", evaluatedTreesCount + 1)}"
}
