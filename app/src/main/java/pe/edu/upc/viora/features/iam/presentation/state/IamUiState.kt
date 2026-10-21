package pe.edu.upc.viora.features.iam.presentation.state

import androidx.annotation.StringRes
import pe.edu.upc.viora.R

/** The screens of the IAM flow, in Figma order (frames 03 and 05). */
enum class IamStep {
    ONBOARDING,
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

/** Country of the agricultural operation (ISO-3166-1 alpha-2) and how its mobile numbers look. */
enum class IamCountry(
    val iso: String,
    val dialCode: String,
    @StringRes val nameRes: Int,
    val nationalDigits: Int,
    val mobilePrefixes: String,
) {
    PE("PE", "+51", R.string.iam_country_pe, 9, "9"),
    ES("ES", "+34", R.string.iam_country_es, 9, "67"),
    CL("CL", "+56", R.string.iam_country_cl, 9, "9"),
    ;

    fun isValidMobile(national: String): Boolean =
        national.length == nationalDigits && national.all(Char::isDigit) && national.first() in mobilePrefixes

    /** E.164, the only format the backend accepts. */
    fun e164(national: String): String = dialCode + national
}

/** Failures that are not tied to one field; the UI maps each one to a string resource. */
enum class IamError { OFFLINE, GENERIC, WAIT }

/** Why the code screen shows a message under the digits. */
sealed interface CodeError {
    data class Wrong(val remainingAttempts: Int?) : CodeError
    data object Blocked : CodeError
    data object Expired : CodeError
    data object Missing : CodeError
}

data class IamUiState(
    val step: IamStep = IamStep.ONBOARDING,
    val loading: Boolean = false,
    val authenticated: Boolean = false,

    // Onboarding data. Kept in memory until the account is created (guide, option B).
    val fullName: String = "",
    val country: IamCountry = IamCountry.PE,
    val phone: String = "",
    val phoneRejectedByServer: Boolean = false,

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

    val firstName: String get() = fullName.trim().substringBefore(' ')

    val emailValid: Boolean get() = EMAIL_REGEX.matches(email.trim())
    val phoneValid: Boolean get() = country.isValidMobile(phone)
    val onboardingComplete: Boolean get() = fullName.trim().length >= 2 && phoneValid

    val passwordLongEnough: Boolean get() = password.length >= MIN_PASSWORD
    val passwordHasLettersAndDigits: Boolean get() = password.any(Char::isLetter) && password.any(Char::isDigit)
    val passwordOk: Boolean get() = passwordLongEnough && passwordHasLettersAndDigits

    val confirmOk: Boolean get() = confirmPassword.isNotEmpty() && confirmPassword == password
    val newPasswordOk: Boolean get() = passwordOk && confirmOk

    val canSignIn: Boolean get() = emailValid && password.isNotEmpty()
    val canCreateAccount: Boolean get() = emailValid && passwordOk

    private companion object {
        const val MIN_PASSWORD = 8
        val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")
    }
}
