package pe.edu.upc.viora.features.subscription.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET

interface ProducerPlanService {
    @GET("subscription-plans")
    suspend fun getOffers(): Response<List<ProducerPlanOfferDto>>
}
