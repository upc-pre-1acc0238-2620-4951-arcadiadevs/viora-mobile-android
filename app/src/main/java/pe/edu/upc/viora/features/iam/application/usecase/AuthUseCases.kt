package pe.edu.upc.viora.features.iam.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.iam.domain.model.AuthResult
import pe.edu.upc.viora.features.iam.domain.model.Registration
import pe.edu.upc.viora.features.iam.domain.repository.AuthRepository

/** T03 · Inicia sesión. */
class SignInUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): AuthResult<Unit> =
        repository.signIn(email.trim(), password)
}

/** T04 · Crea tu cuenta: answers the email the verification code went to. */
class SignUpUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(registration: Registration): AuthResult<String> = repository.signUp(registration)
}

/** T04a · Verifica tu correo: opens the session when the code is right. */
class VerifyEmailUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, code: String): AuthResult<Unit> = repository.verifyEmail(email, code)
}

/** "Reenviar código". */
class ResendCodeUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): AuthResult<Unit> = repository.resendCode(email)
}

/** T06 · ¿Olvidaste tu contraseña?: sends the recovery link. */
class RequestPasswordResetUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String): AuthResult<Unit> = repository.requestPasswordReset(email.trim())
}

/** Checks the recovery link the email opened before asking for the new password. */
class CheckPasswordResetLinkUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(token: String): AuthResult<Unit> = repository.checkPasswordResetLink(token)
}

/** T08 · Nueva contraseña through the recovery link. */
class ResetPasswordUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(token: String, newPassword: String): AuthResult<Unit> =
        repository.resetPassword(token, newPassword)
}

/** P97 · Cambiar contraseña, after checking the current one (US04). */
class ChangePasswordUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(currentPassword: String, newPassword: String): AppResult<Unit> =
        repository.changePassword(currentPassword, newPassword)
}

/** Closes the session on this phone and forgets every record of the account. */
class SignOutUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.signOut()
}

/** P100 · Eliminar cuenta: deletes the account and everything of it, after checking the password. */
class DeleteAccountUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(password: String): AppResult<Unit> = repository.deleteAccount(password)
}
