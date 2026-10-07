package pe.edu.upc.viora.features.phenology.application

import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.phenology.application.usecase.ObserveChillTrackerUseCase
import pe.edu.upc.viora.features.phenology.application.usecase.RefreshChillTrackerUseCase
import pe.edu.upc.viora.features.phenology.domain.entity.ChillTracker
import pe.edu.upc.viora.features.phenology.domain.entity.EnsoRiskLevel
import pe.edu.upc.viora.features.phenology.domain.entity.WinterSeasonState
import pe.edu.upc.viora.features.phenology.domain.repository.ChillRepository

private class FakeChillRepository : ChillRepository {
    val trackerFlow = MutableStateFlow<ChillTracker?>(null)
    var refreshCalled = false

    override fun observeChillTracker(plotId: String): Flow<ChillTracker?> = trackerFlow

    override fun observeLastSync(plotId: String): Flow<Instant?> = MutableStateFlow(null)

    override suspend fun refresh(plotId: String): AppResult<Unit> {
        refreshCalled = true
        return AppResult.Success(Unit)
    }
}

class ChillUseCasesTest {

    private val fakeRepo = FakeChillRepository()
    private val observeUseCase = ObserveChillTrackerUseCase(fakeRepo)
    private val refreshUseCase = RefreshChillTrackerUseCase(fakeRepo)

    @Test
    fun observeUseCaseDelegatesToRepository() = runTest {
        val tracker = ChillTracker(
            plotId = "p-1",
            accumulatedPortions = 20.0,
            thresholdPortions = 30.0,
            daysAbove24Celsius = 1,
            seasonState = WinterSeasonState.ACCUMULATING,
            projectedCompletionDate = null,
            previousWinterCompletionDate = null,
            ensoRisk = EnsoRiskLevel.NEUTRAL,
            curvePoints = emptyList(),
            syncedAt = Instant.EPOCH,
        )
        fakeRepo.trackerFlow.value = tracker

        val emitted = observeUseCase("p-1").first()
        assertNotNull(emitted)
        assertEquals(20.0, emitted!!.accumulatedPortions, 0.001)
    }

    @Test
    fun refreshUseCaseDelegatesToRepository() = runTest {
        val result = refreshUseCase("p-1")
        assertTrue(result is AppResult.Success)
        assertTrue(fakeRepo.refreshCalled)
    }
}
