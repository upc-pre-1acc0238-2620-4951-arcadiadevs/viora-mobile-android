package pe.edu.upc.viora.features.croploadregulation.presentation.state

import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.croploadregulation.domain.entity.ThinningExecution
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.CaliberProjectionStatus
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.LoadState
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety

/** P63 · Aclareo registrado. */
sealed interface ThinningResultUiState {

    data object Loading : ThinningResultUiState

    data class Error(val error: AppError) : ThinningResultUiState

    data class Content(
        val plotName: String,
        val variety: OliveVariety?,
        val campaignYear: Int,
        val execution: ThinningExecution,
    ) : ThinningResultUiState {

        /** Which of the four caliber cards of Figma P63 applies; null hides the card (no projection). */
        val caliberCard: CaliberCard?
            get() = when (execution.caliberProjection?.status) {
                CaliberProjectionStatus.ESTIMATED -> CaliberCard.ESTIMATED.takeIf {
                    execution.caliberProjection.mostLikelySizeGrade != null
                }
                CaliberProjectionStatus.NOT_CALIBRATED -> CaliberCard.LEARNING
                CaliberProjectionStatus.NOT_ESTIMATED_LATE -> CaliberCard.LATE
                CaliberProjectionStatus.OUTSIDE_CALIBRATION_RANGE -> CaliberCard.OUT_OF_RANGE
                CaliberProjectionStatus.NOT_APPLICABLE, null -> null
            }

        /** The residual load is still above the target: the load tile turns terracotta. */
        val stillOverloaded: Boolean
            get() = execution.loadBalance?.let { it.loadState != LoadState.BALANCED } ?: false

        /** The line under the headline: late first, then a load still too high, otherwise the good news. */
        val voice: ResultVoice
            get() = when {
                !execution.isOpportune || caliberCard == CaliberCard.LATE -> ResultVoice.LATE
                stillOverloaded -> ResultVoice.STILL_OVERLOADED
                else -> ResultVoice.GOOD_JOB
            }
    }
}

enum class CaliberCard {
    /** "101/110": the most likely grade with its 80 % range. */
    ESTIMATED,

    /** "3 de 8": the harvests the variety model still needs. */
    LEARNING,

    /** "No estimado": thinned after the pit hardened. */
    LATE,

    /** "No estimado": the residual load is outside what the model knows. */
    OUT_OF_RANGE,
}

enum class ResultVoice { GOOD_JOB, LATE, STILL_OVERLOADED }
