package pe.edu.upc.viora.features.subscription.domain.entity

import java.time.Instant

/**
 * The sponsored plan a redeemed code gives the producer (P06): who pays for it, how many hectares it covers, until
 * when, and the manager the producer now shares their plots with.
 */
data class CooperativeMembership(
    val cooperativeName: String,
    val quotaHectares: Double,
    val endsAt: Instant? = null,
    val managerName: String? = null,
)
