package pe.edu.upc.viora.features.iam.application

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.iam.application.usecase.ChangePasswordUseCase
import pe.edu.upc.viora.features.iam.application.usecase.CheckPasswordResetLinkUseCase
import pe.edu.upc.viora.features.iam.application.usecase.DeleteAccountUseCase
import pe.edu.upc.viora.features.iam.application.usecase.RequestPasswordResetUseCase
import pe.edu.upc.viora.features.iam.application.usecase.ResendCodeUseCase
import pe.edu.upc.viora.features.iam.application.usecase.ResetPasswordUseCase
import pe.edu.upc.viora.features.iam.application.usecase.SignInUseCase
import pe.edu.upc.viora.features.iam.application.usecase.SignOutUseCase
import pe.edu.upc.viora.features.iam.application.usecase.SignUpUseCase
import pe.edu.upc.viora.features.iam.application.usecase.VerifyEmailUseCase
import pe.edu.upc.viora.features.iam.domain.model.AuthError
import pe.edu.upc.viora.features.iam.domain.model.AuthResult
import pe.edu.upc.viora.features.iam.domain.model.Registration
import pe.edu.upc.viora.features.iam.domain.repository.AuthRepository

/** Records what reaches the repository and answers what the test sets. */
private class RecordingAuth : AuthRepository {
    val calls = mutableListOf<String>()
    var authAnswer: AuthResult<Unit> = AuthResult.Success(Unit)
    var appAnswer: AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun signIn(email: String, password: String): AuthResult<Unit> =
        authAnswer.also { calls += "signIn($email,$password)" }
    override suspend fun signUp(registration: Registration): AuthResult<String> =
        AuthResult.Success(registration.email).also { calls += "signUp(${registration.email})" }
    override suspend fun verifyEmail(email: String, code: String): AuthResult<Unit> =
        authAnswer.also { calls += "verifyEmail($email,$code)" }
    override suspend fun resendCode(email: String): AuthResult<Unit> = authAnswer.also { calls += "resendCode($email)" }
    override suspend fun requestPasswordReset(email: String): AuthResult<Unit> =
        authAnswer.also { calls += "requestPasswordReset($email)" }
    override suspend fun checkPasswordResetLink(token: String): AuthResult<Unit> =
        authAnswer.also { calls += "checkPasswordResetLink($token)" }
    override suspend fun resetPassword(token: String, newPassword: String): AuthResult<Unit> =
        authAnswer.also { calls += "resetPassword($token,$newPassword)" }
    override suspend fun changePassword(currentPassword: String, newPassword: String): AppResult<Unit> =
        appAnswer.also { calls += "changePassword($currentPassword,$newPassword)" }
    override suspend fun deleteAccount(password: String): AppResult<Unit> = appAnswer.also { calls += "deleteAccount($password)" }
    override suspend fun signOut() {
        calls += "signOut"
    }
}

class AuthUseCasesTest {

    private val auth = RecordingAuth()

    @Test
    fun `sign in trims the email typed with spaces but never the password`() = runTest {
        SignInUseCase(auth)("  ana@viora.pe ", " clave 1 ")

        assertEquals(listOf("signIn(ana@viora.pe, clave 1 )"), auth.calls)
    }

    @Test
    fun `sign in passes the failure through so the screen can tell why`() = runTest {
        auth.authAnswer = AuthResult.Failure(AuthError.InvalidCredentials)

        assertEquals(AuthResult.Failure(AuthError.InvalidCredentials), SignInUseCase(auth)("ana@viora.pe", "x"))
    }

    @Test
    fun `sign up answers the email the code was sent to`() = runTest {
        val registration = Registration("ana@viora.pe", "Clave-segura1", "Ana Quispe", "PE", "952184337")

        assertEquals(AuthResult.Success("ana@viora.pe"), SignUpUseCase(auth)(registration))
    }

    @Test
    fun `password recovery trims the email`() = runTest {
        RequestPasswordResetUseCase(auth)(" ana@viora.pe\n")

        assertEquals(listOf("requestPasswordReset(ana@viora.pe)"), auth.calls)
    }

    @Test
    fun `the code, the link and the new password reach the repository as typed`() = runTest {
        VerifyEmailUseCase(auth)("ana@viora.pe", "123456")
        ResendCodeUseCase(auth)("ana@viora.pe")
        CheckPasswordResetLinkUseCase(auth)("tok")
        ResetPasswordUseCase(auth)("tok", "Nueva-clave1")

        assertEquals(
            listOf(
                "verifyEmail(ana@viora.pe,123456)",
                "resendCode(ana@viora.pe)",
                "checkPasswordResetLink(tok)",
                "resetPassword(tok,Nueva-clave1)",
            ),
            auth.calls,
        )
    }

    @Test
    fun `account operations answer what the repository answers`() = runTest {
        auth.appAnswer = AppResult.Failure(AppError.Offline)

        assertEquals(AppResult.Failure(AppError.Offline), ChangePasswordUseCase(auth)("Vieja-clave1", "Nueva-clave1"))
        assertEquals(AppResult.Failure(AppError.Offline), DeleteAccountUseCase(auth)("Vieja-clave1"))
        SignOutUseCase(auth)()

        assertEquals(
            listOf("changePassword(Vieja-clave1,Nueva-clave1)", "deleteAccount(Vieja-clave1)", "signOut"),
            auth.calls,
        )
    }
}
