package pe.edu.upc.viora.features.subscription.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CooperativeCodeRedemptionService {

    /**
     * Redeems an invitation code for the authenticated producer: 201 the new subscription; 400 the code is not
     * redeemable; 409 the producer already has an active subscription; 429 too many failed attempts.
     */
    @POST("cooperative-code-redemptions")
    suspend fun redeem(@Body request: RedeemCooperativeCodeRequestDto): Response<SubscriptionDto>
}
