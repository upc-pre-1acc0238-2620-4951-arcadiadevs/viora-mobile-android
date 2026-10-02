package pe.edu.upc.viora.core.network

import java.io.IOException
import java.io.InterruptedIOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import pe.edu.upc.viora.core.domain.AppError
import retrofit2.HttpException
import retrofit2.Response

/** Translates HTTP responses and transport exceptions into [AppError]. */
@Singleton
class ApiErrorMapper @Inject constructor(private val json: Json) {

    fun fromResponse(response: Response<*>): AppError {
        val problem = parseProblem(response.errorBody())
        val detail = problem?.detail
        val code = problem?.code
        return when (val status = response.code()) {
            400, 422 -> AppError.Validation(detail, code)
            401 -> AppError.Unauthorized
            403 -> AppError.Forbidden(detail)
            404 -> AppError.NotFound(detail, code)
            409 -> AppError.Conflict(detail, code)
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

    private fun parseProblem(body: ResponseBody?): ProblemDetailDto? {
        val raw = runCatching { body?.string() }.getOrNull()
        if (raw.isNullOrBlank()) return null
        return try {
            json.decodeFromString<ProblemDetailDto>(raw)
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }
}
