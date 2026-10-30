package pe.edu.upc.viora.features.subscription.presentation

import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.OliveVariety
import pe.edu.upc.viora.core.domain.PlotId
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.subscription.application.usecase.GetCurrentSubscriptionUseCase
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerSubscription
import pe.edu.upc.viora.features.subscription.domain.repository.ProducerSubscriptionRepository
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity
import pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionMode
import pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionStatus
import pe.edu.upc.viora.features.subscription.presentation.state.MyPlanUiState
import pe.edu.upc.viora.features.subscription.presentation.state.PlanStanding
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.MyPlanViewModel
import pe.edu.upc.viora.testing.unused

@OptIn(ExperimentalCoroutinesApi::class)
class MyPlanViewModelTest {

    private val clock = Clock.fixed(Instant.parse("2026-10-29T15:00:00Z"), ZoneOffset.UTC)
    private val plots = MutableStateFlow<List<Plot>>(emptyList())
    private val answers = ArrayDeque<AppResult<ProducerSubscription?>>()

    private val plotRepository = object : PlotRepository by unused<PlotRepository>() {
        override fun observePlots(): Flow<List<Plot>> = plots
    }
    private val subscriptions = object : ProducerSubscriptionRepository by unused<ProducerSubscriptionRepository>() {
        override suspend fun getCurrent(): AppResult<ProducerSubscription?> = answers.removeFirst()
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun plan(endsAt: String) = ProducerSubscription(
        id = "7f3c1e52-9a4b-4d1e-8c2a-1b6f0e9d4a21", mode = SubscriptionMode.INDIVIDUAL, planCode = null,
        status = SubscriptionStatus.ACTIVE, capacity = PlanCapacity.of(BigDecimal("5")), currency = null,
        baseAmount = null, taxAmount = null, totalAmount = null, startsAt = Instant.parse("2026-01-01T00:00:00Z"),
        endsAt = Instant.parse(endsAt), entitlementActive = true, cooperativeId = null,
    )

    private fun plot(id: String, hectares: Double, active: Boolean = true) = Plot(
        id = PlotId(id), name = "Lote $id", variety = OliveVariety.SEVILLANA, areaHectares = hectares, treesPerHectare = 100,
        rowSpacingMeters = 10.0, treeSpacingMeters = 10.0, outline = emptyList(), lastPruningDate = null,
        isActive = active, revision = 0,
    )

    private fun viewModel() = MyPlanViewModel(GetCurrentSubscriptionUseCase(subscriptions), ObservePlotsUseCase(plotRepository), clock)

    private fun TestScope.stateOf(vm: MyPlanViewModel): MyPlanUiState {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }
        return vm.uiState.value
    }

    @Test
    fun `the confirmed plan counts the hectares of the active plots only`() = runTest {
        answers += AppResult.Success(plan("2027-06-30T00:00:00Z"))
        plots.value = listOf(plot("a", 2.0), plot("b", 1.5), plot("c", 9.0, active = false))

        val state = stateOf(viewModel())

        assertFalse(state.isLoading)
        assertEquals(PlanStanding.ACTIVE, state.standing)
        assertEquals(5.0, state.quotaHectares, 0.0)
        assertEquals(3.5, state.usedHectares, 0.0)
        assertEquals(listOf("a", "b"), state.plots.map { it.plotId })
    }

    @Test
    fun `a plan ending within thirty days asks to be renewed`() = runTest {
        answers += AppResult.Success(plan("2026-11-20T00:00:00Z"))

        val state = stateOf(viewModel())

        assertEquals(PlanStanding.EXPIRING, state.standing)
        assertEquals(22, state.daysLeft)
    }

    @Test
    fun `without a plan the standing is none`() = runTest {
        answers += AppResult.Success(null)

        assertEquals(PlanStanding.NONE, stateOf(viewModel()).standing)
    }

    @Test
    fun `a failed request shows the error until a retry succeeds`() = runTest {
        answers += AppResult.Failure(AppError.Offline)
        answers += AppResult.Success(plan("2027-06-30T00:00:00Z"))
        val vm = viewModel()

        assertEquals(AppError.Offline, stateOf(vm).error)

        vm.refresh()

        assertEquals(PlanStanding.ACTIVE, vm.uiState.value.standing)
        assertEquals(null, vm.uiState.value.error)
    }
}
