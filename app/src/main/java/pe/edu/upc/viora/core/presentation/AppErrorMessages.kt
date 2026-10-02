package pe.edu.upc.viora.core.presentation

import androidx.annotation.StringRes
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.domain.AppError

/**
 * Default user-facing message for an [AppError]. Screens may show something more specific
 * for their own case, but should fall back to this so tone and wording stay consistent.
 */
@StringRes
fun AppError.messageRes(): Int = when (this) {
    AppError.Offline -> R.string.error_offline
    AppError.Timeout -> R.string.error_timeout
    AppError.Unauthorized -> R.string.error_unauthorized
    is AppError.Forbidden -> R.string.error_forbidden
    is AppError.NotFound -> R.string.error_not_found
    is AppError.Conflict -> R.string.error_conflict
    is AppError.PreconditionFailed -> R.string.error_precondition_failed
    is AppError.Validation -> R.string.error_validation
    is AppError.Server -> R.string.error_server
    is AppError.Unknown -> R.string.error_unknown
}
