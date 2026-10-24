package pe.edu.upc.viora.features.subscription.domain.valueobject

/** Persisted provider evidence, independent from subscription entitlement. */
enum class IndividualPaymentStatus {
    UNKNOWN, CREATING, OPEN, PENDING, APPROVED, REJECTED, EXPIRED;

    companion object {
        fun from(value: String?): IndividualPaymentStatus = entries.firstOrNull { it.name == value } ?: UNKNOWN
    }
}
