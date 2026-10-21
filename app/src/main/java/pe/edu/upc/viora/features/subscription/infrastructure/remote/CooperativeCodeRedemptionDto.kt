package pe.edu.upc.viora.features.subscription.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class RedeemCooperativeCodeRequestDto(val code: String)

/** The subscription the backend creates; only what the app shows is read. */
@Serializable
data class SubscriptionDto(
    val id: String,
    val mode: String? = null,
    val status: String? = null,
    val quotaHa: Double,
    val cooperativeName: String? = null,
)
