package pe.edu.upc.viora.core.domain

import kotlinx.serialization.json.JsonObject

/**
 * Failure vocabulary shared by every bounded context. Pure Kotlin: infrastructure maps
 * transport/persistence problems into these types so presentation never sees HTTP or IO details.
 */
sealed interface AppError {

    /** No connectivity, DNS failure or the server could not be reached. */
    data object Offline : AppError

    /** The request was sent but the server did not answer in time (e.g. cold start). */
    data object Timeout : AppError

    /** Missing, expired or invalid credentials (HTTP 401). */
    data object Unauthorized : AppError

    /** Authenticated but not allowed to touch the resource (HTTP 403). */
    data class Forbidden(val detail: String? = null) : AppError

    /** The resource does not exist or is no longer active (HTTP 404). */
    data class NotFound(val detail: String? = null, val code: String? = null) : AppError

    /**
     * A business conflict such as a duplicated name or campaign (HTTP 409). [properties] keeps the
     * ProblemDetail members beyond the standard ones (e.g. `existingSettlement`) so a caller can
     * decode the payload it expects; null when the body had none.
     */
    data class Conflict(
        val detail: String? = null,
        val code: String? = null,
        val properties: JsonObject? = null,
    ) : AppError

    /** Optimistic locking failed: the resource changed on the server, reload it (HTTP 412). */
    data class PreconditionFailed(val detail: String? = null, val code: String? = null) : AppError

    /** The server rejected the payload or a business rule (HTTP 400 / 422). */
    data class Validation(val detail: String? = null, val code: String? = null) : AppError

    /** Unexpected server-side failure (HTTP 5xx). */
    data class Server(val status: Int, val detail: String? = null) : AppError

    /** Anything else, including malformed responses. Keep the cause for logging. */
    data class Unknown(val cause: Throwable? = null) : AppError
}
