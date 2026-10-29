package pe.edu.upc.viora.features.iam.infrastructure.remote

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

// The shared Json does not encode default values, so every field the backend requires
// (role, termsAccepted...) is passed explicitly and has no default here.

@Serializable
data class SignInRequest(val email: String, val password: String)

@Serializable
data class SignUpProfile(val fullName: String, val country: String, val phoneNumber: String)

@Serializable
data class SignUpRequest(
    val email: String,
    val password: String,
    val role: String,
    val termsAccepted: Boolean,
    val profile: SignUpProfile,
)

@Serializable
data class VerifyEmailRequest(val email: String, val code: String)

@Serializable
data class ResendCodeRequest(val email: String)

@Serializable
data class PasswordResetRequest(val email: String)

@Serializable
data class NewPasswordRequest(val newPassword: String)

@Serializable
data class RefreshTokenRequest(val refreshToken: String)

@Serializable
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Int = 900,
    val userId: String,
    val role: String,
)

@Serializable
data class SignUpResponse(val userId: String, val email: String, val status: String)

interface IamService {
    @POST("auth/sign-in")
    suspend fun signIn(@Body request: SignInRequest): AuthSession

    @POST("auth/sign-up")
    suspend fun signUp(@Body request: SignUpRequest): SignUpResponse

    @POST("auth/email-verifications")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): AuthSession

    @POST("auth/email-verification-codes")
    suspend fun resendCode(@Body request: ResendCodeRequest)

    @POST("auth/password-reset-tokens")
    suspend fun requestPasswordReset(@Body request: PasswordResetRequest)

    /** 204 when the link still works; 404 `PASSWORD_RESET_TOKEN_NOT_FOUND` when expired, used or replaced. */
    @GET("auth/password-reset-tokens/{token}")
    suspend fun checkPasswordResetToken(@Path("token") token: String)

    /**
     * Rotates the session: the refresh token works once and comes back renewed. 403 when it expired,
     * was already used (then every session of the account is closed) or the password changed.
     */
    @POST("auth/refresh-tokens")
    suspend fun refresh(@Body request: RefreshTokenRequest): AuthSession

    /** 204 on success. The token identifies the account, so no email is sent. */
    @PUT("auth/password-reset-tokens/{token}")
    suspend fun resetPassword(@Path("token") token: String, @Body request: NewPasswordRequest)

    /**
     * Forgets [request]'s refresh token on the server. It goes out without the session interceptors, so
     * it never renews (and rotates) the token it is about to revoke.
     */
    @POST("auth/sign-out")
    suspend fun signOut(@Body request: RefreshTokenRequest): Response<Unit>
}
