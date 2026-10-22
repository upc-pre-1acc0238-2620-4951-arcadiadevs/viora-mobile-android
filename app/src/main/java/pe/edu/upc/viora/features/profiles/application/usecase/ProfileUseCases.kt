package pe.edu.upc.viora.features.profiles.application.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.profiles.domain.entity.UserProfile
import pe.edu.upc.viora.features.profiles.domain.repository.AccountPreferencesRepository
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

/** Whether Viora's fixed texts are written letter by letter (Mi cuenta › Voz de Viora animada). */
class ObserveAnimatedVoiceUseCase @Inject constructor(private val repository: AccountPreferencesRepository) {
    operator fun invoke(): Flow<Boolean> = repository.animatedVoice
}

/** Turns the writing of Viora's fixed texts on or off; the assistant's replies are always written. */
class SetAnimatedVoiceUseCase @Inject constructor(private val repository: AccountPreferencesRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setAnimatedVoice(enabled)
}
