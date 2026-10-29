package pe.edu.upc.viora.features.profiles.infrastructure.repository

import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.core.datastore.SessionStore
import pe.edu.upc.viora.core.di.SessionFree
import pe.edu.upc.viora.core.database.LocalDataOwner
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.iam.domain.valueobject.MobileNumbers
import pe.edu.upc.viora.features.iam.infrastructure.remote.RefreshTokenRequest
import pe.edu.upc.viora.features.iam.infrastructure.session.toStoredSession
import pe.edu.upc.viora.features.profiles.domain.entity.UserProfile
import pe.edu.upc.viora.features.profiles.domain.repository.ProfileRepository
import pe.edu.upc.viora.features.profiles.domain.valueobject.ProfileId
import pe.edu.upc.viora.features.profiles.infrastructure.local.AccessTokenClaims
import pe.edu.upc.viora.features.profiles.infrastructure.local.ProfileStore
import pe.edu.upc.viora.features.profiles.infrastructure.remote.ChangePasswordRequest
import pe.edu.upc.viora.features.profiles.infrastructure.remote.DeleteAccountRequest
import pe.edu.upc.viora.features.profiles.infrastructure.remote.ProfileResource
import pe.edu.upc.viora.features.profiles.infrastructure.remote.ProfileService
import pe.edu.upc.viora.features.profiles.infrastructure.remote.UpdateProfileRequest

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val service: ProfileService,
    /** The same routes without the session interceptors: signing out must not renew the token it revokes. */
    @SessionFree private val sessionFreeService: ProfileService,
    private val apiCaller: ApiCaller,
    private val profileStore: ProfileStore,
    private val sessionStore: SessionStore,
    private val clock: Clock,
    private val localDataOwner: LocalDataOwner,
) : ProfileRepository {

    /** Only the signed-in account's profile: one kept for another account never shows. */
    override fun observeProfile(): Flow<UserProfile?> = profileStore.profileFlow.map { profile ->
        profile?.takeIf { it.userId == sessionStore.session()?.userId }
    }

    override suspend fun refreshProfile(): AppResult<UserProfile> {
        val session = sessionStore.session() ?: return AppResult.Failure(AppError.Unauthorized)
        val userId = session.userId ?: return AppResult.Failure(AppError.Unauthorized)
        return when (val result = apiCaller.call { service.getProfile(userId) }) {
            is AppResult.Success -> {
                val profile = result.value.toDomain(userId, AccessTokenClaims.email(session.accessToken))
                profileStore.saveProfile(profile)
                AppResult.Success(profile)
            }
            // Offline the kept profile still answers; it belongs to this account only.
            is AppResult.Failure -> profileStore.getProfile()?.takeIf { it.userId == userId }
                ?.let { AppResult.Success(it) } ?: result
        }
    }

    override suspend fun updateProfile(fullName: String, country: String, nationalNumber: String): AppResult<UserProfile> {
        val session = sessionStore.session() ?: return AppResult.Failure(AppError.Unauthorized)
        val userId = session.userId ?: return AppResult.Failure(AppError.Unauthorized)
        val request = UpdateProfileRequest(
            fullName = fullName.trim(),
            country = country,
            phoneNumber = MobileNumbers.e164(nationalNumber.filter(Char::isDigit), country),
        )
        return when (val result = apiCaller.call { service.updateProfile(userId, request) }) {
            is AppResult.Success -> {
                val profile = result.value.toDomain(userId, AccessTokenClaims.email(session.accessToken))
                profileStore.saveProfile(profile)
                AppResult.Success(profile)
            }
            is AppResult.Failure -> result
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): AppResult<Unit> {
        val request = ChangePasswordRequest(currentPassword = currentPassword, newPassword = newPassword)
        return when (val result = apiCaller.call { service.changePassword(request) }) {
            is AppResult.Success -> {
                // Every session was closed, this one too: only the new tokens keep the phone signed in.
                sessionStore.save(result.value.toStoredSession(clock))
                AppResult.Success(Unit)
            }
            is AppResult.Failure -> {
                val error = result.error
                // The only business rule the change can break while signed in is the current password.
                if (error is AppError.Validation && error.code == BUSINESS_RULE_VIOLATION) {
                    AppResult.Failure(AppError.Validation(error.detail, ProfileRepository.WRONG_CURRENT_PASSWORD))
                } else {
                    result
                }
            }
        }
    }

    override suspend fun hasActiveMembership(): AppResult<Boolean> =
        when (val result = apiCaller.callOptional { service.currentSubscription() }) {
            is AppResult.Success -> AppResult.Success(result.value?.entitlementActive == true)
            is AppResult.Failure -> result
        }

    override suspend fun signOut() {
        // The server forgets the refresh token; offline it simply expires. Either way the phone forgets it. The call
        // goes without a session, so it never renews (and rotates) the token it is about to send.
        sessionStore.refreshToken()?.let { refreshToken ->
            apiCaller.callUnit { sessionFreeService.signOut(RefreshTokenRequest(refreshToken)) }
        }
        sessionStore.clear()
        profileStore.clear()
        // Nothing of the farm stays on the phone for whoever uses it next.
        localDataOwner.release()
    }

    override suspend fun deleteAccount(password: String): AppResult<Unit> =
        when (val result = apiCaller.callUnit { service.deleteAccount(DeleteAccountRequest(password)) }) {
            is AppResult.Success -> {
                // The server has nothing left of the account: the phone forgets its session and every record too.
                sessionStore.clear()
                profileStore.clear()
                localDataOwner.claimFor(null)
                result
            }
            is AppResult.Failure -> {
                val error = result.error
                if (error is AppError.Validation && error.code == BUSINESS_RULE_VIOLATION) {
                    AppResult.Failure(AppError.Validation(error.detail, ProfileRepository.WRONG_CURRENT_PASSWORD))
                } else {
                    result
                }
            }
        }

    private fun ProfileResource.toDomain(userId: String, email: String?): UserProfile = UserProfile(
        id = ProfileId(id?.takeIf(String::isNotBlank) ?: userId),
        userId = this.userId ?: userId,
        fullName = fullName,
        country = country,
        phoneNumber = phoneNumber,
        email = email,
    )

    private companion object {
        const val BUSINESS_RULE_VIOLATION = "BUSINESS_RULE_VIOLATION"
    }
}
