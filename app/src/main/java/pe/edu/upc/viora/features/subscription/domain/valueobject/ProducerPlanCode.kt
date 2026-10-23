package pe.edu.upc.viora.features.subscription.domain.valueobject

/** Identity supplied by the subscription catalog, never inferred from a display name. */
@JvmInline
value class ProducerPlanCode(val value: String) {
    init {
        require(value.isNotBlank()) { "subscription.plan.code.blank" }
    }
}
