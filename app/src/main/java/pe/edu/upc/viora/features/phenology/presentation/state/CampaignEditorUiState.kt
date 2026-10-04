package pe.edu.upc.viora.features.phenology.presentation.state

import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.phenology.domain.entity.HarvestRecord

/** Which campaign sheet is open. */
sealed interface CampaignMode {
    /** Registering a past campaign. */
    data object Add : CampaignMode

    /** Correcting or deleting the registered [record]. */
    data class Correct(val record: HarvestRecord) : CampaignMode
}

/** What the sheet reports to the screen once the server accepted the change. */
data class CampaignResult(val kind: CampaignChangeKind, val year: Int, val recordId: String?)

/** State of the "Agrega una campaña" / "Corrige la campaña" sheet and its delete dialog. */
data class CampaignEditorUiState(
    val mode: CampaignMode,
    val year: Int,
    val kilosText: String,
    val yearError: YearError?,
    val yearCaption: YearCaption,
    val kilosError: Boolean,
    val preview: IndexPreview?,
    val canSave: Boolean,
    val isSaving: Boolean,
    val error: AppError?,
    val confirmingDelete: Boolean,
    val deletePreview: IndexPreview?,
    val minYear: Int = CampaignRules.MIN_YEAR,
    val maxYear: Int,
)
