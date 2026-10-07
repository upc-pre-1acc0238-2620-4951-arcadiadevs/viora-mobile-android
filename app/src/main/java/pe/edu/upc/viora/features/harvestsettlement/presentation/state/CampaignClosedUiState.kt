package pe.edu.upc.viora.features.harvestsettlement.presentation.state

import java.time.LocalDate
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementSummary
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety

/**
 * What the receipt card (P73) shows, whether the settlement is already on the server or still
 * waiting on the phone. [receiptNumber] is null until the server issues it (and with an older
 * backend); [calibre] is the sales size grade label, null when none was recorded.
 */
data class SettlementReceipt(
    val plotId: String,
    val plotName: String,
    val variety: OliveVariety?,
    val campaignYear: Int,
    val greenKg: Double,
    val blackKg: Double,
    val totalKg: Double,
    val greenShare: Double?,
    val tonnesPerHectare: Double?,
    val weighedOn: LocalDate,
    val receiptNumber: String?,
    val calibre: String?,
)

/** What the P73 screen shows for a plot and campaign. */
sealed interface CampaignClosedUiState {

    /** Looking the settlement up (cache, then one refresh). */
    data object Loading : CampaignClosedUiState

    /** P73 closed: the server stored the settlement. */
    data class Closed(val receipt: SettlementReceipt) : CampaignClosedUiState

    /** P73 offline: saved on the phone; [failed] when the server rejected it and it needs a correction. */
    data class Pending(val receipt: SettlementReceipt, val failed: Boolean) : CampaignClosedUiState

    /** The server already had this campaign when the pending settlement synced (409). */
    data class Conflict(val plotName: String, val campaignYear: Int, val existing: SettlementSummary?) : CampaignClosedUiState

    /** Neither a settlement nor a pending one exists for the campaign, even after a refresh. */
    data object Missing : CampaignClosedUiState
}
