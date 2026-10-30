package pe.edu.upc.viora.features.croploadregulation.infrastructure

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.croploadregulation.application.usecase.SampleSyncOutcome
import pe.edu.upc.viora.features.croploadregulation.infrastructure.sync.SamplingSyncWorker

class SamplingSyncWorkerTest {

    private val failing = SampleSyncOutcome(pendingLeft = true)
    private val offline = SampleSyncOutcome(pendingLeft = true, offline = true)

    @Test
    fun `nothing left to send ends the work`() {
        assertFalse(SamplingSyncWorker.shouldRetry(SampleSyncOutcome(), runAttemptCount = 0))
    }

    @Test
    fun `a failure that is not the connection retries until the attempts run out`() {
        assertTrue(SamplingSyncWorker.shouldRetry(failing, runAttemptCount = 0))
        assertTrue(SamplingSyncWorker.shouldRetry(failing, runAttemptCount = SamplingSyncWorker.MAX_ATTEMPTS - 2))
        assertFalse(SamplingSyncWorker.shouldRetry(failing, runAttemptCount = SamplingSyncWorker.MAX_ATTEMPTS - 1))
    }

    @Test
    fun `losing the connection never gives up`() {
        assertTrue(SamplingSyncWorker.shouldRetry(offline, runAttemptCount = 100))
    }
}
