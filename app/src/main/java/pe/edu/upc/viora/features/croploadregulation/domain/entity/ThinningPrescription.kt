package pe.edu.upc.viora.features.croploadregulation.domain.entity

import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.LoadState
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.PrescriptionBlocker
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.PrescriptionStatus
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.ThinningProfileStatus
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.YieldEstimateBasis
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.YieldEstimateStatus

/**
 * The thinning plan of a plot for one campaign (US26-US28): what the sampling says about the load, what is still
 * missing to prescribe, and once prescribed how much fruit to remove and in which window.
 *
 * @property sampledTrees trees sampled so far; the prescription needs [SAMPLE_SIZE]
 * @property windowOpensDaysAfterBloom offsets of the technical profile, known before the full bloom is recorded
 */
data class ThinningPrescription(
    val id: String,
    val plotId: String,
    val campaignYear: Int,
    val status: PrescriptionStatus,
    val percentageToRemove: Double?,
    val targetFruitsPerShoot: Double?,
    val fullBloomOn: LocalDate?,
    val windowOpensOn: LocalDate?,
    val windowClosesOn: LocalDate?,
    val profileStatus: ThinningProfileStatus?,
    val blockers: Set<PrescriptionBlocker>,
    val currentLoad: CurrentLoad?,
    val sampledTrees: Int,
    val treesNeeded: Int,
    val lastSampledOn: LocalDate?,
    val windowOpensDaysAfterBloom: Int?,
    val windowClosesDaysAfterBloom: Int?,
    val yieldEstimate: YieldEstimate?,
) {
    /** True once the backend issued the prescription: the percentage and the window are final. */
    val isIssued: Boolean
        get() = status != PrescriptionStatus.SAMPLING_IN_PROGRESS && percentageToRemove != null

    /** Where today falls against the intervention window; null while there is no window. */
    fun windowPhase(today: LocalDate): WindowPhase? {
        val opens = windowOpensOn ?: return null
        val closes = windowClosesOn ?: return null
        return when {
            today.isBefore(opens) -> WindowPhase.NotOpenYet(ChronoUnit.DAYS.between(today, opens).toInt())
            today.isAfter(closes) -> WindowPhase.Closed(ChronoUnit.DAYS.between(closes, today).toInt())
            else -> WindowPhase.Open(
                daysLeft = ChronoUnit.DAYS.between(today, closes).toInt(),
                progress = ChronoUnit.DAYS.between(opens, today).toFloat() /
                    ChronoUnit.DAYS.between(opens, closes).coerceAtLeast(1),
            )
        }
    }

    /** The window a full bloom on [bloom] would give, by the profile offsets; null without a profile. */
    fun windowFor(bloom: LocalDate): Pair<LocalDate, LocalDate>? {
        val opens = windowOpensDaysAfterBloom ?: return null
        val closes = windowClosesDaysAfterBloom ?: return null
        return bloom.plusDays(opens.toLong()) to bloom.plusDays(closes.toLong())
    }

    /** Classifies an execution against the prescribed intervention window. */
    fun timelinessOf(date: LocalDate): ThinningTimeliness? =
        thinningTimeliness(date, windowOpensOn, windowClosesOn)

    companion object {
        /** Trees a representative sampling round needs. */
        const val SAMPLE_SIZE = 5
    }
}

/** Load before thinning against the target of the technical profile (US26). */
data class CurrentLoad(
    val fruitsPerShoot: Double,
    val targetFruitsPerShoot: Double,
    val loadRatio: Double,
    val loadState: LoadState,
)

/**
 * Campaign yield in kg/ha (the app shows t/ha). [estimatedKgPerHa] is a ceiling when [basis] is
 * [YieldEstimateBasis.FRUIT_COUNT_ONLY], because without a calibrated caliber the fruit is assumed not to shrink.
 */
data class YieldEstimate(
    val status: YieldEstimateStatus,
    val sustainableKgPerHa: Double?,
    val estimatedKgPerHa: Double?,
    val basis: YieldEstimateBasis?,
    val harvestCampaigns: Int,
    val requiredHarvestCampaigns: Int,
)

sealed interface WindowPhase {
    data class NotOpenYet(val daysUntilOpen: Int) : WindowPhase
    data class Open(val daysLeft: Int, val progress: Float) : WindowPhase
    data class Closed(val daysSinceClose: Int) : WindowPhase
}

/** One plot's line of the Plan tab (P60): its plan and recorded thinning as cached, both null when never sampled. */
data class CampaignPlan(val plotId: String, val prescription: ThinningPrescription?, val execution: ThinningExecution?)

/** A prescription read from the cache, with the moment it was downloaded. */
data class CachedPrescription(val prescription: ThinningPrescription?, val syncedAt: Instant)

/** Whether an intervention falls before, within or after its prescribed window. */
enum class ThinningTimeliness { BEFORE, WITHIN, LATE }

fun thinningTimeliness(date: LocalDate, opens: LocalDate?, closes: LocalDate?): ThinningTimeliness? {
    if (opens == null || closes == null) return null
    return when {
        date.isBefore(opens) -> ThinningTimeliness.BEFORE
        date.isAfter(closes) -> ThinningTimeliness.LATE
        else -> ThinningTimeliness.WITHIN
    }
}
