package pe.edu.upc.viora.features.iam.presentation

import java.math.BigDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.iam.InMemoryPreferences
import pe.edu.upc.viora.features.iam.infrastructure.local.OnboardingStore
import pe.edu.upc.viora.features.iam.presentation.state.OnboardingStep
import pe.edu.upc.viora.features.iam.presentation.viewmodel.OnboardingViewModel
import pe.edu.upc.viora.features.subscription.application.usecase.ObserveProducerPlanCatalogUseCase
import pe.edu.upc.viora.features.subscription.application.usecase.RefreshProducerPlanCatalogUseCase
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerPlanCatalog
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerPlanOffer
import pe.edu.upc.viora.features.subscription.domain.repository.ProducerPlanRepository
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity
import pe.edu.upc.viora.features.subscription.domain.valueobject.ProducerPlanCode

/** The plan catalog the onboarding reads its reference price from. */
private class FakePlanCatalog(offers: List<ProducerPlanOffer>) : ProducerPlanRepository {
    val catalog = MutableStateFlow(ProducerPlanCatalog(offers, null))
    override fun observeCatalog(): Flow<ProducerPlanCatalog> = catalog
    override suspend fun refresh(): AppResult<Unit> = AppResult.Failure(AppError.Offline)
}

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private lateinit var store: OnboardingStore
    private val annualPlan = ProducerPlanOffer(
        ProducerPlanCode("PRODUCER_ANNUAL"), "PEN", BigDecimal("55"), BigDecimal("0.18"),
        PlanCapacity.of(BigDecimal("0.5")), PlanCapacity.of(BigDecimal("0.5")), 12, "fixture-1", true,
    )
    private var plans = FakePlanCatalog(listOf(annualPlan))

    private fun viewModel() = OnboardingViewModel(
        store,
        ObserveProducerPlanCatalogUseCase(plans),
        RefreshProducerPlanCatalogUseCase(plans),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val dataStore = InMemoryPreferences()
        store = OnboardingStore(dataStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun OnboardingViewModel.fillNameAndPhone() {
        onName("Teodoro Mamani")
        continueFromName()
        onPhone("987654321")
        continueFromPhone()
    }

    /** DataStore writes run on their own scope: wait until the expected answer is on disk. */
    private fun <T> eventually(read: suspend () -> T, expected: T): T = runBlocking {
        repeat(50) {
            val value = read()
            if (value == expected) return@runBlocking value
            Thread.sleep(20)
        }
        read()
    }

    @Test
    fun `name needs two letters and the phone a valid mobile`() {
        val vm = viewModel()
        vm.onName("T")
        vm.continueFromName()
        assertEquals(OnboardingStep.NAME, vm.uiState.value.step)

        vm.onName("Teodoro")
        vm.continueFromName()
        vm.onPhone("98765")
        vm.continueFromPhone()
        assertEquals(OnboardingStep.PHONE, vm.uiState.value.step)
        assertTrue(vm.uiState.value.showPhoneError)
    }

    @Test
    fun `without a code the hectares question comes next`() {
        val vm = viewModel()
        vm.fillNameAndPhone()
        vm.skipCooperativeCode()
        assertEquals(OnboardingStep.HECTARES, vm.uiState.value.step)

        vm.onHectares(12)
        vm.continueFromHectares(unknown = false)
        assertEquals(OnboardingStep.ALERTS, vm.uiState.value.step)
        assertEquals(BigDecimal("7920"), vm.uiState.value.yearlyPrice)
        assertEquals(BigDecimal("660"), vm.uiState.value.monthlyPrice)
    }

    @Test
    fun `without the plan catalog there is no price to estimate`() {
        plans = FakePlanCatalog(emptyList())
        val vm = viewModel()
        vm.onHectares(12)

        assertNull(vm.uiState.value.yearlyPrice)
    }

    @Test
    fun `a saved code skips the hectares and back returns to the code`() {
        val vm = viewModel()
        vm.fillNameAndPhone()
        vm.onCooperativeCode("coop-7k2q")
        vm.saveCooperativeCode()

        assertEquals(OnboardingStep.ALERTS, vm.uiState.value.step)
        assertEquals("COOP-7K2Q", vm.uiState.value.savedCooperativeCode)
        vm.back()
        assertEquals(OnboardingStep.COOPERATIVE_CODE, vm.uiState.value.step)
    }

    @Test
    fun `editing from the summary returns to it`() {
        val vm = viewModel()
        vm.fillNameAndPhone()
        vm.skipCooperativeCode()
        vm.continueFromHectares(unknown = true)
        vm.onAlertsAnswered(true)
        vm.onLocationAnswered(false)
        assertEquals(OnboardingStep.SUMMARY, vm.uiState.value.step)

        vm.edit(OnboardingStep.NAME)
        vm.onName("Teodoro Mamani Quispe")
        vm.continueFromName()
        assertEquals(OnboardingStep.SUMMARY, vm.uiState.value.step)
        assertFalse(vm.uiState.value.editing)
    }

    @Test
    fun `answers are kept on the phone for sign-up`() {
        val vm = viewModel()
        vm.fillNameAndPhone()
        vm.skipCooperativeCode()
        vm.continueFromHectares(unknown = true)
        vm.onAlertsAnswered(true)
        vm.onLocationAnswered(true)

        val answers = eventually({ store.answers().locationGranted }, true).let { runBlocking { store.answers() } }
        assertEquals("Teodoro Mamani", answers.fullName)
        assertEquals("987654321", answers.nationalPhone)
        assertNull(answers.hectares)
        assertEquals(true, answers.alertsEnabled)
    }

    @Test
    fun `back on the first question leaves the onboarding`() {
        assertFalse(viewModel().back())
    }

    @Test
    fun `create my account under the legal notice records the terms as accepted before going on`() {
        val viewModel = viewModel()
        var continued = false

        assertFalse(runBlocking { store.answers().termsAccepted })
        viewModel.acceptTermsAndContinue { continued = true }

        assertTrue(eventually({ store.answers().termsAccepted }, true))
        assertTrue(continued)
    }
}
