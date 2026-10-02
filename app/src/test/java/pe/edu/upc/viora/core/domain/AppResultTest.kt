package pe.edu.upc.viora.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppResultTest {

    private val failure: AppResult<Int> = AppResult.Failure(AppError.Offline)

    @Test
    fun `map transforms a success`() {
        assertEquals(AppResult.Success(4), AppResult.Success(2).map { it * 2 })
    }

    @Test
    fun `map leaves a failure untouched`() {
        assertEquals(failure, failure.map { it * 2 })
    }

    @Test
    fun `fold picks the branch matching the outcome`() {
        assertEquals("ok:2", AppResult.Success(2).fold({ "ok:$it" }, { "err" }))
        assertEquals("err", failure.fold({ "ok:$it" }, { "err" }))
    }

    @Test
    fun `getOrNull returns the value only for success`() {
        assertEquals(7, AppResult.Success(7).getOrNull())
        assertNull(failure.getOrNull())
    }

    @Test
    fun `onSuccess and onFailure run only for their case`() {
        var successes = 0
        var failures = 0

        AppResult.Success(1).onSuccess { successes++ }.onFailure { failures++ }
        failure.onSuccess { successes++ }.onFailure { failures++ }

        assertEquals(1, successes)
        assertEquals(1, failures)
    }
}
