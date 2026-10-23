package pe.edu.upc.viora.features.subscription.infrastructure.remote

import java.math.BigDecimal
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.*

@Serializable
data class IndividualPurchaseRequest(
    val planCode: String,
    @Serializable(with = ProducerPlanDecimalSerializer::class) val requestedQuotaHa: BigDecimal,
)

@Serializable
data class IndividualPurchaseDto(
    val id: String,
    val mode: String,
    val planCode: String?,
    val status: String,
    @Serializable(with = ProducerPlanDecimalSerializer::class) val quotaHa: BigDecimal,
    val currency: String?,
    @Serializable(with = ProducerPlanDecimalSerializer::class) val baseAmount: BigDecimal?,
    @Serializable(with = ProducerPlanDecimalSerializer::class) val taxAmount: BigDecimal?,
    @Serializable(with = ProducerPlanDecimalSerializer::class) val totalAmount: BigDecimal?,
    val startsAt: String?,
    val endsAt: String?,
    val entitlementActive: Boolean,
    val cooperativeId: String?,
)

@Serializable
data class IndividualCheckoutDto(val intentId: String, val checkoutUrl: String, val expiresAt: String)

interface IndividualPurchaseService {
    @POST("subscriptions")
    suspend fun create(@Header("Idempotency-Key") key: String, @Body request: IndividualPurchaseRequest): Response<IndividualPurchaseDto>

    @GET("subscriptions/{id}")
    suspend fun get(@Path("id") id: String): Response<IndividualPurchaseDto>

    @POST("subscriptions/{id}/checkouts")
    suspend fun checkout(@Path("id") id: String, @Header("Idempotency-Key") key: String): Response<IndividualCheckoutDto>
}
