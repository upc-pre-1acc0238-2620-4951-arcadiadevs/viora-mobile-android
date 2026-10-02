package pe.edu.upc.viora.core.network

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import pe.edu.upc.viora.core.di.IoDispatcher
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import retrofit2.Response

/**
 * Single entry point that repositories use to run a Retrofit call and get an [AppResult]
 * instead of raw `Response<T>` or exceptions. Coroutine cancellation is never swallowed.
 *
 * ```
 * override suspend fun getPlot(id: String) =
 *     apiCaller.call { service.getPlot(id) }.map { it.toDomain() }
 * ```
 */
@Singleton
class ApiCaller @Inject constructor(
    private val errorMapper: ApiErrorMapper,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) {

    /** Runs [block] and requires a non-empty body on success. */
    suspend fun <T : Any> call(block: suspend () -> Response<T>): AppResult<T> =
        execute(block) { response ->
            val body = response.body()
            if (body != null) {
                AppResult.Success(body)
            } else {
                AppResult.Failure(AppError.Unknown(IllegalStateException("Empty response body")))
            }
        }

    /** Runs [block] ignoring the body (e.g. 204 No Content or a message-only 200). */
    suspend fun <T> callUnit(block: suspend () -> Response<T>): AppResult<Unit> =
        execute(block) { AppResult.Success(Unit) }

    private suspend fun <T, R> execute(
        block: suspend () -> Response<T>,
        onSuccess: (Response<T>) -> AppResult<R>,
    ): AppResult<R> = withContext(dispatcher) {
        try {
            val response = block()
            if (response.isSuccessful) {
                onSuccess(response)
            } else {
                AppResult.Failure(errorMapper.fromResponse(response))
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            AppResult.Failure(errorMapper.fromThrowable(throwable))
        }
    }
}
