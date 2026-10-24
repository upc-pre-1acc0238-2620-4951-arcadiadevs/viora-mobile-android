package pe.edu.upc.viora.features.subscription.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ProducerSubscriptionService {
    @GET("subscriptions")
    suspend fun getCurrent(): Response<ProducerSubscriptionDto>

    @GET("subscriptions/{id}")
    suspend fun getById(@Path("id") id: String): Response<ProducerSubscriptionDto>
}
