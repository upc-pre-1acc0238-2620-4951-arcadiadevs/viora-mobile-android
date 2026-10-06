package pe.edu.upc.viora.features.harvestsettlement.domain.entity

import java.time.LocalDate

/**
 * What the producer submits to settle a plot's harvest for a campaign. [weighedOn] is the
 * weighing date (never in the future); the optional fields are omitted from the request when null.
 */
data class SettleHarvestDraft(
    val plotId: String,
    val campaignYear: Int,
    val greenOlivesKg: Double,
    val blackOlivesKg: Double,
    val weighedOn: LocalDate,
    val millTicketNumber: String? = null,
    val commercialFruitsPerKg: Double? = null,
    val notes: String? = null,
)

/** Summary of a settlement that already exists for the campaign (the 409 `existingSettlement`). */
data class SettlementSummary(
    val campaignYear: Int,
    val totalYieldKg: Double,
    val receiptNumber: String?,
    val weighedOn: LocalDate?,
)

/** Result of a settle attempt that reached the server. */
sealed interface SettleOutcome {

    /** The settlement was created (201) or replayed for the same idempotency key (200). */
    data class Settled(val settlement: HarvestSettlement) : SettleOutcome

    /**
     * The campaign was already settled (409). [existing] is the server's summary, or the cached
     * one, and is null when neither is available.
     */
    data class AlreadySettled(val existing: SettlementSummary?) : SettleOutcome

    /**
     * The server could not be reached (offline or timeout): the draft was saved as [pending]
     * with its idempotency key and a sync was scheduled. The settlement is not on the server yet.
     */
    data class Queued(val pending: PendingSettlement) : SettleOutcome
}
