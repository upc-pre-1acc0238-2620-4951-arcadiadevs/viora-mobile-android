package pe.edu.upc.viora.features.profiles.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.profiles.domain.entity.UserProfile
import pe.edu.upc.viora.features.profiles.domain.repository.ProfileRepository

/** The profile kept on the phone; null until it is downloaded once. */
class ObserveProfileUseCase @Inject constructor(private val repository: ProfileRepository) {
    operator fun invoke(): Flow<UserProfile?> = repository.observeProfile()
}

/** Downloads the signed-in producer's profile (US03). */
class RefreshProfileUseCase @Inject constructor(private val repository: ProfileRepository) {
    suspend operator fun invoke(): AppResult<UserProfile> = repository.refreshProfile()
}

/** Saves the full name and the mobile of the profile (US03). */
class UpdateProfileUseCase @Inject constructor(private val repository: ProfileRepository) {
    suspend operator fun invoke(fullName: String, country: String, nationalNumber: String): AppResult<UserProfile> =
        repository.updateProfile(fullName, country, nationalNumber)
}

/** Changes the password after checking the current one (US04). */
class ChangePasswordUseCase @Inject constructor(private val repository: ProfileRepository) {
    suspend operator fun invoke(currentPassword: String, newPassword: String): AppResult<Unit> =
        repository.changePassword(currentPassword, newPassword)
}

/** Whether the producer's access is active today, for the badge of the account card. */
class CheckMembershipUseCase @Inject constructor(private val repository: ProfileRepository) {
    suspend operator fun invoke(): AppResult<Boolean> = repository.hasActiveMembership()
}

/** Closes the session on this phone. */
class SignOutUseCase @Inject constructor(private val repository: ProfileRepository) {
    suspend operator fun invoke() = repository.signOut()
}
