package pe.edu.upc.viora.features.harvestsettlement.presentation.state

import java.time.LocalDate
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementSummary

/** The dialog over the settle form, if any. */
sealed interface SettleDialog {
    /** P72: "¿Asentar la cosecha 2026?". */
    data object Confirm : SettleDialog

    /** P72 (409): the campaign is already settled; [existing] is what the server (or cache) knows. */
    data class AlreadySettled(val existing: SettlementSummary?) : SettleDialog
}

/**
 * State of the settle form (P71) and its dialogs (P72). [totalKg], [greenShare] and
 * [tonnesPerHectare] are null while the kilos are not valid (or the plot is not cached yet).
 */
data class SettleHarvestUiState(
    val plotId: String,
    val plotName: String,
    val campaignYear: Int,
    val weighedOn: LocalDate,
    val maxDate: LocalDate,
    val greenText: String,
    val blackText: String,
    val greenError: Boolean,
    val blackError: Boolean,
    val totalKg: Double?,
    val greenShare: Double?,
    val tonnesPerHectare: Double?,
    val millTicket: String,
    val calibreText: String,
    val calibreGrade: CommercialSizeGrade?,
    val calibreError: Boolean,
    val canSave: Boolean,
    val isSaving: Boolean,
    val error: AppError?,
    val dialog: SettleDialog?,
)
