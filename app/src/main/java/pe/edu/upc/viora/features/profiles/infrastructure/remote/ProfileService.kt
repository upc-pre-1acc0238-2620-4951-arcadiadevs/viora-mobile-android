package pe.edu.upc.viora.features.profiles.infrastructure.remote

import pe.edu.upc.viora.features.iam.infrastructure.remote.AuthSession
import pe.edu.upc.viora.features.iam.infrastructure.remote.RefreshTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/** The endpoints behind Mi cuenta (P95–P98): the profile, the password, the membership and the session. */
interface ProfileService {
    @GET("profiles/{userId}")
    suspend fun getProfile(@Path("userId") userId: String): Response<ProfileResource>

    @PATCH("profiles/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: String,
        @Body request: UpdateProfileRequest,
    ): Response<ProfileResource>

    /** Closes every other session and answers a new one for this phone, which must replace the old tokens. */
    @PATCH("auth/passwords")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<AuthSession>

    /** 204 when the producer has no subscription yet. */
    @GET("subscriptions/current")
    suspend fun currentSubscription(): Response<CurrentSubscriptionResource>

    @POST("auth/sign-out")
    suspend fun signOut(@Body request: RefreshTokenRequest): Response<Unit>
}
