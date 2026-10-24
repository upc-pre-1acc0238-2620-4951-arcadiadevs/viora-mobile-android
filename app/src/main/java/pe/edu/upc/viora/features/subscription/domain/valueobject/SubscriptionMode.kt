package pe.edu.upc.viora.features.subscription.domain.valueobject

enum class SubscriptionMode {
    INDIVIDUAL,
    COOPERATIVE_SPONSORED,
    UNKNOWN;

    companion object {
        fun from(value: String?): SubscriptionMode = when (value?.trim()?.uppercase()) {
            "INDIVIDUAL", "PLAN_PRODUCTOR_INDIVIDUAL" -> INDIVIDUAL
            "COOPERATIVE_SPONSORED", "PLAN_PRODUCTOR_COOPERATIVA", "PLAN_COOPERATIVO_PATROCINADO" -> COOPERATIVE_SPONSORED
            else -> UNKNOWN
        }
    }
}
