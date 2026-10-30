package pe.edu.upc.viora.features.iam.infrastructure.remote

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
)

@Serializable
data class DeleteAccountRequest(val password: String)

/** The account endpoints that need the session: changing the password and deleting the account (P97, P100). */
interface AccountService {
    /** Closes every other session and answers a new one for this phone, which must replace the old tokens. */
    @PATCH("auth/passwords")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<AuthSession>

    /** 204 once the account and all its data are gone; 422 for a wrong password, 403 for the demo account. */
    @POST("auth/account-deletions")
    suspend fun deleteAccount(@Body request: DeleteAccountRequest): Response<Unit>
}
