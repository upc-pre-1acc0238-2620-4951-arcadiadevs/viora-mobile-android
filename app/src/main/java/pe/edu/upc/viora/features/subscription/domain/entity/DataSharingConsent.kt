package pe.edu.upc.viora.features.subscription.domain.entity

import java.time.Instant

/** Whether the producer still shares their data with the cooperative's manager. */
enum class DataSharingStatus {
    ACTIVE,

    /** The producer stopped sharing; the plan goes on. */
    WITHDRAWN,

    /** The sponsored plan ended, and the sharing with it. */
    ENDED,
}

/** The producer's consent to share their data with the manager of the cooperative that pays the plan (P99). */
data class DataSharingConsent(
    val id: String,
    val organizationName: String,
    /** The manager who reads the data now; null while the license has none. */
    val managerName: String?,
    val status: DataSharingStatus,
    val acceptedAt: Instant?,
    val withdrawnAt: Instant?,
    val planEndsAt: Instant?,
)
