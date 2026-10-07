package pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper

import java.time.LocalDate
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.PendingSettlement
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.PendingStatus
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettleHarvestDraft
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.SettlementSummary
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.PendingSettlementEntity

/** A fresh PENDING row for the draft; [createdAt] is kept when an existing row is being replaced. */
fun SettleHarvestDraft.toPendingEntity(idempotencyKey: String, createdAt: Long, updatedAt: Long) =
    PendingSettlementEntity(
        plotId = plotId,
        campaignYear = campaignYear,
        greenOlivesKg = greenOlivesKg,
        blackOlivesKg = blackOlivesKg,
        weighedOn = weighedOn.toString(),
        millTicketNumber = millTicketNumber,
        commercialFruitsPerKg = commercialFruitsPerKg,
        notes = notes,
        idempotencyKey = idempotencyKey,
        status = PendingStatus.PENDING.name,
        lastErrorCode = null,
        attemptCount = 0,
        existingTotalYieldKg = null,
        existingReceiptNumber = null,
        existingWeighedOn = null,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

/** Throws `DateTimeParseException` when the stored date is corrupt; the sync marks such a row FAILED. */
fun PendingSettlementEntity.toDraft(): SettleHarvestDraft = SettleHarvestDraft(
    plotId = plotId,
    campaignYear = campaignYear,
    greenOlivesKg = greenOlivesKg,
    blackOlivesKg = blackOlivesKg,
    weighedOn = LocalDate.parse(weighedOn),
    millTicketNumber = millTicketNumber,
    commercialFruitsPerKg = commercialFruitsPerKg,
    notes = notes,
)

fun PendingSettlementEntity.toDomain(): PendingSettlement = PendingSettlement(
    draft = runCatching { toDraft() }.getOrElse { toDraftWithToday() },
    idempotencyKey = idempotencyKey,
    status = runCatching { PendingStatus.valueOf(status) }.getOrDefault(PendingStatus.FAILED),
    lastErrorCode = lastErrorCode,
    existing = existingTotalYieldKg?.let {
        SettlementSummary(
            campaignYear = campaignYear,
            totalYieldKg = it,
            receiptNumber = existingReceiptNumber,
            weighedOn = existingWeighedOn?.let { raw -> runCatching { LocalDate.parse(raw) }.getOrNull() },
        )
    },
    createdAt = createdAt,
    updatedAt = updatedAt,
)

/** A corrupt date must not break the observed list: show the row with today's date so the user can fix or discard it. */
private fun PendingSettlementEntity.toDraftWithToday() = SettleHarvestDraft(
    plotId = plotId,
    campaignYear = campaignYear,
    greenOlivesKg = greenOlivesKg,
    blackOlivesKg = blackOlivesKg,
    weighedOn = LocalDate.now(),
    millTicketNumber = millTicketNumber,
    commercialFruitsPerKg = commercialFruitsPerKg,
    notes = notes,
)
