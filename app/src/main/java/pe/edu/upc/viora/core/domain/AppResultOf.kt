package pe.edu.upc.viora.core.domain

import kotlinx.coroutines.CancellationException

/**
 * Runs [block] and wraps its value in [AppResult.Success]; any failure becomes [AppResult.Failure] with
 * [AppError.Unknown] keeping the cause. A cancellation is rethrown, so structured concurrency still works.
 * The one place for what each repository used to copy as `guarded { }`.
 */
inline fun <T> appResultOf(block: () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (throwable: Throwable) {
    AppResult.Failure(AppError.Unknown(throwable))
}
