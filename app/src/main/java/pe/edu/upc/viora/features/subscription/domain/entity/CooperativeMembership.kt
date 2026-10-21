package pe.edu.upc.viora.features.subscription.domain.entity

/** The sponsored plan a redeemed code gives the producer: who pays for it and how many hectares it covers (P06). */
data class CooperativeMembership(
    val cooperativeName: String,
    val quotaHectares: Double,
)
