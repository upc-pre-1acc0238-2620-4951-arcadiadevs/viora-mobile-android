package pe.edu.upc.viora.features.profiles.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Endpoints for User Profiles (EP10: TS04, TS05, TS35, TS36).
 */
interface ProfileService {
    @GET("profiles/{userId}")
    suspend fun getProfile(@Path("userId") userId: String): Response<ProfileResource>

    @PATCH("profiles/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: String,
        @Body request: UpdateProfileRequest,
    ): Response<ProfileResource>

    @POST("profiles")
    suspend fun createProfile(@Body request: CreateProfileRequest): Response<ProfileResource>

    @PATCH("auth/passwords")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>
}
