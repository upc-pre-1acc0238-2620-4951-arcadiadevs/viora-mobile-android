package pe.edu.upc.viora.features.subscription.infrastructure.remote

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface DataSharingConsentService {

    /** The consents of the authenticated producer, newest first. */
    @GET("data-sharing-consents")
    suspend fun list(): Response<List<DataSharingConsentDto>>

    /** Stops sharing: 200 the withdrawn consent; 409 it was already withdrawn or ended with its plan. */
    @POST("data-sharing-consents/{id}/withdrawals")
    suspend fun withdraw(@Path("id") id: String): Response<DataSharingConsentDto>
}

@Serializable
data class DataSharingConsentDto(
    val id: String,
    val organizationName: String,
    val managerName: String? = null,
    val status: String,
    val acceptedAt: String? = null,
    val withdrawnAt: String? = null,
    val planEndsAt: String? = null,
)
