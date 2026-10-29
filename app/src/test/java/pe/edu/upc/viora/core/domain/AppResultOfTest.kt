package pe.edu.upc.viora.core.domain

import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AppResultOfTest {

    @Test
    fun aReturnedValueIsASuccess() {
        assertEquals(AppResult.Success(42), appResultOf { 42 })
    }

    @Test
    fun aThrownFailureIsAnUnknownErrorKeepingTheCause() {
        val boom = IllegalStateException("disk full")
        val result = appResultOf<Unit> { throw boom }

        val error = (result as AppResult.Failure).error
        assertTrue(error is AppError.Unknown)
        assertSame(boom, (error as AppError.Unknown).cause)
    }

    @Test
    fun aCancellationIsNeverSwallowed() {
        try {
            appResultOf<Unit> { throw CancellationException("scope cancelled") }
            fail("the cancellation must reach the coroutine machinery")
        } catch (expected: CancellationException) {
            assertEquals("scope cancelled", expected.message)
        }
    }
}
