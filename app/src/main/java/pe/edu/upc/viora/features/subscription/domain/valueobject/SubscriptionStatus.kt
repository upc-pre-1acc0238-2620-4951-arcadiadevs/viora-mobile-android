package pe.edu.upc.viora.features.subscription.domain.valueobject

enum class SubscriptionStatus {
    PENDING_PAYMENT,
    ACTIVE,
    EXPIRED,
    CANCELLED,
    UNKNOWN;

    companion object {
        fun from(value: String?): SubscriptionStatus = when (value?.trim()?.uppercase()) {
            "PENDING_PAYMENT" -> PENDING_PAYMENT
            "ACTIVE" -> ACTIVE
            "EXPIRED" -> EXPIRED
            "CANCELLED" -> CANCELLED
            else -> UNKNOWN
        }
    }
}
