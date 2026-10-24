package pe.edu.upc.viora.features.profiles.presentation.state

/** Why a deletion did not go through; the account is intact in every case. */
enum class DeleteAccountError { WRONG_PASSWORD, PROTECTED_ACCOUNT, OFFLINE, GENERIC }

/** P100 · Eliminar cuenta: what is lost, the password and the "Entiendo" box, and the last confirmation. */
data class DeleteAccountUiState(
    val password: String = "",
    val understood: Boolean = false,
    /** The plots that will be deleted, for "Tus 2 lotes…". */
    val plotCount: Int = 0,
    /** Null until the server answers: the plan row only shows a known active plan. */
    val hasActivePlan: Boolean? = null,
    /** First name kept for the farewell of P101, read before the profile is forgotten. */
    val firstName: String = "",
    val isConfirming: Boolean = false,
    val isDeleting: Boolean = false,
    val error: DeleteAccountError? = null,
    val isDeleted: Boolean = false,
) {
    /** The button only wakes up with the password typed and the box ticked. */
    val canDelete: Boolean get() = password.isNotBlank() && understood && !isDeleting
}
