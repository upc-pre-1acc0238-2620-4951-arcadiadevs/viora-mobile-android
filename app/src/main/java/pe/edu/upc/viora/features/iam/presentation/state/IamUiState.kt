package pe.edu.upc.viora.features.iam.presentation.state

import androidx.annotation.StringRes
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.domain.validation.PasswordPolicy

/** The screens of the IAM flow, in Figma order (frames 03 and 05). */
enum class IamStep {
    SIGN_IN,
    CREATE_ACCOUNT,
    VERIFY,
    FORGOT,
    LINK_SENT,
    CHECKING_LINK,
    NEW_PASSWORD,
    RESET_DONE,
    LINK_EXPIRED,
}

/** Failures that are not tied to one field; the UI maps each one to a string resource. */
enum class IamError { OFFLINE, GENERIC, WAIT, PHONE_REJECTED }

/** Why the code screen shows a message under the digits. */
sealed interface CodeError {
    data class Wrong(val remainingAttempts: Int?) : CodeError
    data object Blocked : CodeError
    data object Expired : CodeError
    data object Missing : CodeError
}

data class IamUiState(
    val step: IamStep = IamStep.SIGN_IN,
    val loading: Boolean = false,
    val authenticated: Boolean = false,
    /** Cooperative code saved in T02c, redeemed right after the email is verified (P05 · Validando). */
    val cooperativeCodeToRedeem: String? = null,
    /** The producer just verified the email without a code: P02 opens to pay the Plan Productor. */
    val buysPlan: Boolean = false,
    /** Hectares of T02d for P02; null for "Aún no lo sé". */
    val planHectares: Int? = null,

    /** From the onboarding answers, for "Guardo lo que elegiste, Teodoro." */
    val firstName: String = "",

    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val code: String = "",

    val emailTaken: Boolean = false,
    val credentialsError: Boolean = false,
    val passwordWeak: Boolean = false,
    val passwordReused: Boolean = false,
    val codeError: CodeError? = null,
    val error: IamError? = null,

    /** Email waiting for its verification code. */
    val pendingEmail: String = "",
    /** Seconds until "Reenviar código" is allowed again (server rule: 45 s). */
    val resendSeconds: Int = 0,
    /** One-shot notice after resending ("Te envié un código nuevo"). */
    val notice: Notice? = null,
) {
    enum class Notice { CODE_RESENT, LINK_RESENT }

    val emailValid: Boolean get() = EMAIL_REGEX.matches(email.trim())

    val passwordLongEnough: Boolean get() = PasswordPolicy.isLongEnough(password)
    val passwordHasLettersAndDigits: Boolean get() = PasswordPolicy.hasLettersAndDigits(password)
    val passwordOk: Boolean get() = passwordLongEnough && passwordHasLettersAndDigits

    val confirmOk: Boolean get() = PasswordPolicy.isConfirmed(password, confirmPassword)
    val newPasswordOk: Boolean get() = passwordOk && confirmOk

    val canSignIn: Boolean get() = emailValid && password.isNotEmpty()
    val canCreateAccount: Boolean get() = emailValid && passwordOk

    private companion object {
        val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")
    }
}
