package pe.edu.upc.viora.features.subscription.infrastructure.remote

import kotlinx.serialization.Serializable

@Serializable
data class PreviewCooperativeCodeRequestDto(val code: String)

/** What a code offers (POST cooperative-code-previews). */
@Serializable
data class CooperativeCodePreviewDto(
    val organizationName: String,
    val managerName: String? = null,
    val quotaHa: Double,
    val planEndsAt: String? = null,
    val registeredAreaHa: Double = 0.0,
    val exceedsQuota: Boolean = false,
    val consentTextVersion: String,
)

/** [consentTextVersion] is the text the producer accepted; without it the backend shares nothing. */
@Serializable
data class RedeemCooperativeCodeRequestDto(val code: String, val consentTextVersion: String? = null)

/** The subscription the backend creates; only what the app shows is read. */
@Serializable
data class SubscriptionDto(
    val id: String,
    val mode: String? = null,
    val status: String? = null,
    val quotaHa: Double,
    val endsAt: String? = null,
    val cooperativeName: String? = null,
)
