package pe.edu.upc.viora.features.subscription.infrastructure.remote

import java.math.BigDecimal
import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

@Serializable
data class ExtensionTermsDto(
    val subscriptionId: String,
    val mode: String,
    val extendable: Boolean,
    val reason: String? = null,
    val action: String,
    @Serializable(with = ProducerPlanDecimalSerializer::class) val quotaHa: BigDecimal,
    val endsAt: String? = null,
    val billableMonths: Int? = null,
    @Serializable(with = ProducerPlanDecimalSerializer::class) val monthlyRatePerHa: BigDecimal? = null,
    val currency: String? = null,
    @Serializable(with = ProducerPlanDecimalSerializer::class) val minimumHa: BigDecimal? = null,
    @Serializable(with = ProducerPlanDecimalSerializer::class) val incrementHa: BigDecimal? = null,
)

@Serializable
data class QuotaExtensionDto(
    val id: String,
    val subscriptionId: String,
    val source: String,
    val status: String,
    @Serializable(with = ProducerPlanDecimalSerializer::class) val addedHa: BigDecimal,
    @Serializable(with = ProducerPlanDecimalSerializer::class) val quotaHa: BigDecimal,
    val endsAt: String? = null,
    val billedMonths: Int? = null,
    val currency: String? = null,
    @Serializable(with = ProducerPlanDecimalSerializer::class) val totalAmount: BigDecimal? = null,
    val paymentStatus: String? = null,
)

@Serializable
data class RequestQuotaExtensionDto(
    @Serializable(with = ProducerPlanDecimalSerializer::class) val addedHa: BigDecimal,
)

@Serializable
data class RedeemExtensionCodeDto(val code: String)

/** Plan extensions of the backend Subscription context. */
interface PlanExtensionService {
    @GET("subscriptions/{id}/extension-terms")
    suspend fun terms(@Path("id") subscriptionId: String): Response<ExtensionTermsDto>

    @POST("subscriptions/{id}/extensions")
    suspend fun request(
        @Path("id") subscriptionId: String,
        @Header("Idempotency-Key") key: String,
        @Body body: RequestQuotaExtensionDto,
    ): Response<QuotaExtensionDto>

    @GET("subscriptions/{id}/extensions/{extensionId}")
    suspend fun get(@Path("id") subscriptionId: String, @Path("extensionId") extensionId: String): Response<QuotaExtensionDto>

    @POST("subscriptions/{id}/extensions/{extensionId}/checkouts")
    suspend fun checkout(
        @Path("id") subscriptionId: String,
        @Path("extensionId") extensionId: String,
        @Header("Idempotency-Key") key: String,
    ): Response<IndividualCheckoutDto>

    @POST("quota-extension-redemptions")
    suspend fun redeem(@Body body: RedeemExtensionCodeDto): Response<QuotaExtensionDto>
}
