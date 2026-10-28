package pe.edu.upc.viora.features.plotmanagement.presentation.state

import pe.edu.upc.viora.core.domain.AppError

/**
 * The server's own, already translated explanation when it refuses a plot change because of the plan (backend
 * 0.58.0 on): 403 when the producer has no plan in force, 409 with `quotaHa` when the plots would not fit in its
 * hectares. Null for any other error, so a 409 without `quotaHa` keeps meaning a taken name.
 */
fun AppError.planLimitDetail(): String? = when (this) {
    is AppError.Forbidden -> detail
    is AppError.Conflict -> detail.takeIf { properties?.containsKey("quotaHa") == true }
    else -> null
}
