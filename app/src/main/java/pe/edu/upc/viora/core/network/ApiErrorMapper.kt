package pe.edu.upc.viora.core.network

import java.io.IOException
import java.io.InterruptedIOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import okhttp3.ResponseBody
import pe.edu.upc.viora.core.domain.AppError
import retrofit2.HttpException
import retrofit2.Response

/** Translates HTTP responses and transport exceptions into [AppError]. */
@Singleton
class ApiErrorMapper @Inject constructor(private val json: Json) {

    fun fromResponse(response: Response<*>): AppError {
        val raw = readBody(response.errorBody())
        val problem = parseProblem(raw)
        val detail = problem?.detail
        val code = problem?.code
        return when (val status = response.code()) {
            400, 422 -> AppError.Validation(detail, code)
            401 -> AppError.Unauthorized
            403 -> AppError.Forbidden(detail)
            404 -> AppError.NotFound(detail, code)
            409 -> AppError.Conflict(detail, code, extraProperties(raw))
            412 -> AppError.PreconditionFailed(detail, code)
            in 500..599 -> AppError.Server(status, detail)
            else -> AppError.Unknown(HttpException(response))
        }
    }

    fun fromThrowable(throwable: Throwable): AppError = when (throwable) {
        // Must come before IOException: SocketTimeoutException is an InterruptedIOException.
        is InterruptedIOException -> AppError.Timeout
        is IOException -> AppError.Offline
        // Includes SerializationException: a success body that does not match its DTO.
        else -> AppError.Unknown(throwable)
    }

    private fun readBody(body: ResponseBody?): String? = runCatching { body?.string() }.getOrNull()

    private fun parseProblem(raw: String?): ProblemDetailDto? {
        if (raw.isNullOrBlank()) return null
        return try {
            json.decodeFromString<ProblemDetailDto>(raw)
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    /** ProblemDetail members outside the standard set, or null when there are none or the body is not an object. */
    private fun extraProperties(raw: String?): JsonObject? {
        if (raw.isNullOrBlank()) return null
        val body = try {
            json.parseToJsonElement(raw).jsonObject
        } catch (_: SerializationException) {
            return null
        } catch (_: IllegalArgumentException) {
            return null
        }
        val extras = JsonObject(body.filterKeys { it !in STANDARD_MEMBERS })
        return extras.takeIf { it.isNotEmpty() }
    }

    private companion object {
        val STANDARD_MEMBERS = setOf("type", "title", "status", "detail", "instance", "code", "timestamp")
    }
}
