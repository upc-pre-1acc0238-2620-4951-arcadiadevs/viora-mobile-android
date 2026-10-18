package pe.edu.upc.viora.features.harvestsettlement.infrastructure.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity

/**
 * Cached sealed dossier of a plot and campaign (P76), so it can be read without signal. The dossier is stored as the
 * JSON the backend sent because it is only read whole and never changes once sealed. [pdfPath] is the downloaded PDF
 * inside the app's files, null until "Descargar PDF" ran once.
 */
@Entity(tableName = "agronomic_dossiers", primaryKeys = ["plot_id", "campaign_year"])
data class AgronomicDossierEntity(
    @ColumnInfo(name = "plot_id")
    val plotId: String,
    @ColumnInfo(name = "campaign_year")
    val campaignYear: Int,
    @ColumnInfo(name = "payload_json")
    val payloadJson: String,
    @ColumnInfo(name = "pdf_path")
    val pdfPath: String? = null,
    @ColumnInfo(name = "pdf_saved_at_epoch_ms")
    val pdfSavedAtEpochMs: Long? = null,
)
