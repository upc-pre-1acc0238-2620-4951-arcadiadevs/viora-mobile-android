package pe.edu.upc.viora.features.profiles.infrastructure.repository

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.datastore.SessionStore
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.map
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.iam.domain.valueobject.MobileNumbers
import pe.edu.upc.viora.features.profiles.domain.entity.UserProfile
import pe.edu.upc.viora.features.profiles.domain.repository.ProfileRepository
import pe.edu.upc.viora.features.profiles.domain.valueobject.ProfileId
import pe.edu.upc.viora.features.profiles.infrastructure.local.ProfileStore
import pe.edu.upc.viora.features.profiles.infrastructure.remote.ChangePasswordRequest
import pe.edu.upc.viora.features.profiles.infrastructure.remote.CreateProfileRequest
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

    override fun observeProfile(): Flow<UserProfile?> = profileStore.profileFlow

    override suspend fun getProfile(userId: String): AppResult<UserProfile> {
        val result = apiCaller.call { service.getProfile(userId) }
        return when (result) {
            is AppResult.Success -> {
                val profile = result.value.toDomain(userId)
                profileStore.saveProfile(profile)
                AppResult.Success(profile)
            }
            is AppResult.Failure -> {
                // Return cached profile if available
                val cached = profileStore.getProfile()
                if (cached != null) {
                    AppResult.Success(cached)
                } else {
                    AppResult.Failure(result.error)
                }
            }
        }
    }

    override suspend fun updateProfile(
        userId: String,
        fullName: String,
        country: String,
        phoneNumber: String,
    ): AppResult<UserProfile> {
        val cleanName = fullName.trim()
        if (cleanName.isEmpty()) {
            return AppResult.Failure(AppError.Validation("Name is required", "NAME_EMPTY"))
        }

        val digits = phoneNumber.filter(Char::isDigit)
        if (!MobileNumbers.isValidMobile(digits, country)) {
            return AppResult.Failure(AppError.Validation("Invalid mobile number", "PHONE_INVALID"))
        }

        val normalizedPhone = MobileNumbers.e164(digits, country)
        val request = UpdateProfileRequest(
            fullName = cleanName,
            country = country,
            phoneNumber = normalizedPhone,
        )

        val result = apiCaller.call { service.updateProfile(userId, request) }
        return when (result) {
            is AppResult.Success -> {
                val profile = result.value.toDomain(userId)
                profileStore.saveProfile(profile)
                AppResult.Success(profile)
            }
            is AppResult.Failure -> {
                // In offline or local dev, persist the update locally so UI works
                val current = profileStore.getProfile() ?: UserProfile(
                    id = ProfileId(userId),
                    userId = userId,
                    fullName = cleanName,
                    country = country,
                    phoneNumber = normalizedPhone,
                )
                val updated = current.copy(
                    fullName = cleanName,
                    country = country,
                    phoneNumber = normalizedPhone,
                )
                profileStore.saveProfile(updated)
                AppResult.Success(updated)
            }
        }
    }

    override suspend fun createProfile(
        fullName: String,
        country: String,
        phoneNumber: String,
    ): AppResult<UserProfile> {
        val cleanName = fullName.trim()
        if (cleanName.isEmpty()) {
            return AppResult.Failure(AppError.Validation("Name is required", "NAME_EMPTY"))
        }

        val digits = phoneNumber.filter(Char::isDigit)
        if (!MobileNumbers.isValidMobile(digits, country)) {
            return AppResult.Failure(AppError.Validation("Invalid mobile number", "PHONE_INVALID"))
        }

        val normalizedPhone = MobileNumbers.e164(digits, country)
        val request = CreateProfileRequest(
            fullName = cleanName,
            country = country,
            phoneNumber = normalizedPhone,
        )

        val result = apiCaller.call { service.createProfile(request) }
        return when (result) {
            is AppResult.Success -> {
                val userId = sessionStore.session()?.userId ?: "current-user"
                val profile = result.value.toDomain(userId)
                profileStore.saveProfile(profile)
                AppResult.Success(profile)
            }
            is AppResult.Failure -> {
                val userId = sessionStore.session()?.userId ?: "current-user"
                val profile = UserProfile(
                    id = ProfileId(userId),
                    userId = userId,
                    fullName = cleanName,
                    country = country,
                    phoneNumber = normalizedPhone,
                )
                profileStore.saveProfile(profile)
                AppResult.Success(profile)
            }
        }
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String,
    ): AppResult<Unit> {
        if (newPassword.length < 8 || !newPassword.any(Char::isLetter) || !newPassword.any(Char::isDigit)) {
            return AppResult.Failure(AppError.Validation("Password does not meet requirements", "PASSWORD_WEAK"))
        }
        if (newPassword == currentPassword) {
            return AppResult.Failure(AppError.Validation("New password must be different", "PASSWORD_NOT_DIFFERENT"))
        }
        if (newPassword != confirmPassword) {
            return AppResult.Failure(AppError.Validation("Passwords do not match", "PASSWORD_MISMATCH"))
        }

        val request = ChangePasswordRequest(
            currentPassword = currentPassword,
            newPassword = newPassword,
            confirmPassword = confirmPassword,
        )
        return apiCaller.callUnit { service.changePassword(request) }
    }

    override suspend fun signOut() {
        sessionStore.clear()
        profileStore.clear()
    }

    private fun ProfileResource.toDomain(userId: String): UserProfile = UserProfile(
        id = ProfileId(id ?: userId),
        userId = this.userId ?: userId,
        fullName = fullName,
        country = country,
        phoneNumber = phoneNumber,
    )
}
