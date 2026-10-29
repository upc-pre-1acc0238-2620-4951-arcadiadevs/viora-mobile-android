package pe.edu.upc.viora.features.profiles.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

/** The profile behind Mi cuenta (P95, P96). The password, the session and the account belong to IAM. */
interface ProfileService {
    @GET("profiles/{userId}")
    suspend fun getProfile(@Path("userId") userId: String): Response<ProfileResource>

    @PATCH("profiles/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: String,
        @Body request: UpdateProfileRequest,
    ): Response<ProfileResource>
}
