package pe.edu.upc.viora.core.domain.validation

/** The password rules the backend enforces, shown live while the producer types. */
object PasswordPolicy {
    const val MIN_LENGTH = 8

    fun isLongEnough(password: String): Boolean = password.length >= MIN_LENGTH

    fun hasLettersAndDigits(password: String): Boolean = password.any(Char::isLetter) && password.any(Char::isDigit)

    /** Length and letters-with-digits: what a new password must meet before the server is asked. */
    fun isValid(password: String): Boolean = isLongEnough(password) && hasLettersAndDigits(password)

    /** A new password must be filled in and not repeat the current one. */
    fun differsFromCurrent(newPassword: String, currentPassword: String): Boolean =
        newPassword.isNotEmpty() && newPassword != currentPassword

    /** The confirmation is filled in and equal to [password]. */
    fun isConfirmed(password: String, confirmation: String): Boolean =
        confirmation.isNotEmpty() && confirmation == password
}
