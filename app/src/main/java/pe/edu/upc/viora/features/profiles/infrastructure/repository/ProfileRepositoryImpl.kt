package pe.edu.upc.viora.features.profiles.infrastructure.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.core.datastore.SessionStore
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.MobileNumbers
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.profiles.domain.entity.UserProfile
import pe.edu.upc.viora.features.profiles.domain.repository.ProfileRepository
import pe.edu.upc.viora.features.profiles.domain.valueobject.ProfileId
import pe.edu.upc.viora.features.profiles.infrastructure.local.AccessTokenClaims
import pe.edu.upc.viora.features.profiles.infrastructure.local.ProfileStore
import pe.edu.upc.viora.features.profiles.infrastructure.remote.ProfileResource
import pe.edu.upc.viora.features.profiles.infrastructure.remote.ProfileService
import pe.edu.upc.viora.features.profiles.infrastructure.remote.UpdateProfileRequest

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val service: ProfileService,
    private val apiCaller: ApiCaller,
    private val profileStore: ProfileStore,
    private val sessionStore: SessionStore,
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

    private fun ProfileResource.toDomain(userId: String, email: String?): UserProfile = UserProfile(
        id = ProfileId(id?.takeIf(String::isNotBlank) ?: userId),
        userId = this.userId ?: userId,
        fullName = fullName,
        country = country,
        phoneNumber = phoneNumber,
        email = email,
    )
}
