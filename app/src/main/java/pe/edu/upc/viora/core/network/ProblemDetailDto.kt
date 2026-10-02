package pe.edu.upc.viora.core.network

import kotlinx.serialization.Serializable

/**
 * RFC 7807 error body emitted by the Viora backend (Spring `ProblemDetail`).
 * `code` is a backend extension such as `PLOT_CONFLICT` or `VALIDATION_ERROR`.
 */
@Serializable
data class ProblemDetailDto(
    val type: String? = null,
    val title: String? = null,
    val status: Int? = null,
    val detail: String? = null,
    val instance: String? = null,
    val code: String? = null,
    val timestamp: String? = null,
)
