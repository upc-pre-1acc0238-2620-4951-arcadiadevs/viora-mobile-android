package pe.edu.upc.viora.features.harvestsettlement.presentation.state

import java.io.File
import java.time.Instant
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.AgronomicDossier
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.DossierVerification

/** P76 · Expediente del lote. */
sealed interface AgronomicDossierUiState {

    data object Loading : AgronomicDossierUiState

    /** The plot has no settled campaign yet, so there is nothing to seal (opened from "Más opciones"). */
    data class NoDossier(val plotName: String) : AgronomicDossierUiState

    /** Nothing cached and the dossier could not be sealed or downloaded. */
    data class Error(val error: AppError) : AgronomicDossierUiState

    /**
     * The sealed dossier. [offline] shows the "Sin conexión" variant: the PDF can only be downloaded online, and the last
     * downloaded one ([lastPdf], saved at [lastPdfSavedAt]) is still offered.
     */
    data class Content(
        val dossier: AgronomicDossier,
        val lastPdf: File?,
        val lastPdfSavedAt: Instant?,
        val offline: Boolean,
        val downloading: Boolean,
        val downloadFailed: Boolean,
    ) : AgronomicDossierUiState
}

/** P78 · Verificar autenticidad: the answer of the Viora record for the dossier's code. */
sealed interface DossierVerificationState {

    data object Checking : DossierVerificationState

    data class Matches(val verification: DossierVerification) : DossierVerificationState

    /** Viora stores no dossier with this code. */
    data object NoMatch : DossierVerificationState

    /** The record could not be asked (no signal, server down): only the dossier's own data is shown. */
    data object Unreachable : DossierVerificationState
}
