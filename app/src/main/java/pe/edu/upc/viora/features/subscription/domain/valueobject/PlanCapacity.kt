package pe.edu.upc.viora.features.subscription.domain.valueobject

import java.math.BigDecimal

/** Exact hectare capacity; the offer owns its minimum and selectable increment. */
@JvmInline
value class PlanCapacity private constructor(val hectares: BigDecimal) {
    companion object {
        fun of(hectares: BigDecimal): PlanCapacity {
            require(hectares.signum() > 0) { "subscription.capacity.positive" }
            return PlanCapacity(hectares.stripTrailingZeros())
        }
    }
}
