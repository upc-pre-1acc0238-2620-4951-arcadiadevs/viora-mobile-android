package pe.edu.upc.viora.features.harvestsettlement.domain.entity

/**
 * A settlement saved on the phone because the server could not be reached ("por sincronizar").
 * It is unique per plot and campaign. [idempotencyKey] is generated once for the draft and sent
 * on every attempt, so a retry never creates a second settlement. Once it syncs the row is
 * deleted, so a pending settlement can only be edited while it has not reached the server.
 *
 * - [PendingStatus.PENDING]: waiting for the network; edit or discard is allowed.
 * - [PendingStatus.FAILED]: the server rejected it for good ([lastErrorCode]); correct it with
 *   `updatePending` (which queues it again) or discard it.
 * - [PendingStatus.CONFLICT]: the campaign was already settled on the server; [existing] is its
 *   summary (null when unknown). Nothing is retried; the producer dismisses it with `discardPending`.
 */
data class PendingSettlement(
    val draft: SettleHarvestDraft,
    val idempotencyKey: String,
    val status: PendingStatus,
    val lastErrorCode: String?,
    val existing: SettlementSummary?,
    val createdAt: Long,
    val updatedAt: Long,
)

enum class PendingStatus { PENDING, FAILED, CONFLICT }

/** What a sync pass tells the scheduler: finished, or some rows could not be sent yet. */
enum class SettlementSyncResult { DONE, RETRY_LATER }
