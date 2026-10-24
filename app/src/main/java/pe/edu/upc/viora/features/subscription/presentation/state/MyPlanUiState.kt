package pe.edu.upc.viora.features.subscription.presentation.state

import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerSubscription
import pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionMode

/** How the producer's plan stands (Figma P99 and its variants). */
enum class PlanStanding {
    /** Paid or activated with a code, more than [MyPlanRules.EXPIRING_DAYS] days left. */
    ACTIVE,

    /** Still active but ending within [MyPlanRules.EXPIRING_DAYS] days: renew. */
    EXPIRING,

    /** No plan, or one that ended: plots are kept but alerts stop. */
    NONE,
}

/** One plot inside the plan: its arc of the ring and its bar. */
data class PlanPlot(
    val plotId: String,
    val name: String,
    val variety: OliveVariety,
    val trees: Int,
    val hectares: Double,
)

/** P99 · Mi plan: the hectare ring, the validity and the plots inside the plan. */
data class MyPlanUiState(
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val standing: PlanStanding = PlanStanding.NONE,
    /** True when a code activated the plan (no payment, no receipt). */
    val activatedWithCode: Boolean = false,
    val quotaHectares: Double = 0.0,
    val plots: List<PlanPlot> = emptyList(),
    val startsOn: LocalDate? = null,
    val endsOn: LocalDate? = null,
    /** Days of plan left; 0 when there is no plan. */
    val daysLeft: Int = 0,
) {
    val usedHectares: Double get() = plots.sumOf { it.hectares }

    val freeHectares: Double get() = (quotaHectares - usedHectares).coerceAtLeast(0.0)

    /** Hectares the active plots go over the quota (plots registered before a smaller plan); 0 when they fit. */
    val excessHectares: Double
        get() = if (standing == PlanStanding.NONE) 0.0 else (usedHectares - quotaHectares).coerceAtLeast(0.0)

    val isOverQuota: Boolean get() = excessHectares > 0.0

    /**
     * "Tus lotes" (P20) shows the quota card only now: a plan in force with less than
     * [MyPlanRules.TIGHT_QUOTA_HECTARES] free, or already over its quota. The rest of the time P99 has it.
     */
    val isQuotaTight: Boolean
        get() = standing != PlanStanding.NONE && (isOverQuota || freeHectares < MyPlanRules.TIGHT_QUOTA_HECTARES)

    /** How many plots of the average size of the producer's plots still fit, for the "Libre" row. */
    val plotsThatFit: Int
        get() = if (plots.isEmpty()) 0 else (freeHectares / (usedHectares / plots.size)).toInt()

    /** Share of the validity already elapsed, for the capsule. */
    val elapsedShare: Float
        get() {
            val start = startsOn ?: return 0f
            val end = endsOn ?: return 0f
            val total = ChronoUnit.DAYS.between(start, end).coerceAtLeast(1)
            return (1f - daysLeft.toFloat() / total).coerceIn(0f, 1f)
        }
}

/** Pure rules of P99, so the screen and the Mi cuenta card read the plan the same way. */
object MyPlanRules {
    /** Within this many days of the end the plan asks to be renewed (Figma P99 · Por vencer). */
    const val EXPIRING_DAYS = 30

    /** Below this many free hectares the plots list warns about the quota (Figma P20 · Cupo casi lleno). */
    const val TIGHT_QUOTA_HECTARES = 1.0

    fun state(subscription: ProducerSubscription?, plots: List<Plot>, today: LocalDate, zone: ZoneId): MyPlanUiState {
        val inPlan = plots.filter { it.isActive }.map {
            PlanPlot(it.id.value, it.name, it.variety, it.estimatedTrees, it.areaHectares)
        }
        val active = subscription?.entitlementActive == true
        val endsOn = subscription?.endsAt?.atZone(zone)?.toLocalDate()
        val daysLeft = if (active && endsOn != null) ChronoUnit.DAYS.between(today, endsOn).toInt().coerceAtLeast(0) else 0
        val standing = when {
            !active -> PlanStanding.NONE
            endsOn != null && daysLeft <= EXPIRING_DAYS -> PlanStanding.EXPIRING
            else -> PlanStanding.ACTIVE
        }
        return MyPlanUiState(
            isLoading = false,
            standing = standing,
            // A code is how a sponsored plan arrives; the sponsor id is there even if the mode is unknown.
            activatedWithCode = subscription?.mode == SubscriptionMode.COOPERATIVE_SPONSORED || subscription?.cooperativeId != null,
            quotaHectares = if (active) subscription?.capacity?.hectares?.toDouble() ?: 0.0 else 0.0,
            plots = inPlan,
            startsOn = subscription?.startsAt?.atZone(zone)?.toLocalDate(),
            endsOn = endsOn,
            daysLeft = daysLeft,
        )
    }
}
