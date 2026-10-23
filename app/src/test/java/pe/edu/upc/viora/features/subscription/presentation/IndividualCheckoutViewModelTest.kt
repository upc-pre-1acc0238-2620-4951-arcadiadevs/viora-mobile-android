package pe.edu.upc.viora.features.subscription.presentation

import androidx.lifecycle.ViewModelStore
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import pe.edu.upc.viora.core.domain.*
import pe.edu.upc.viora.features.subscription.domain.entity.*
import pe.edu.upc.viora.features.subscription.domain.repository.IndividualPurchaseRepository
import pe.edu.upc.viora.features.subscription.domain.valueobject.*
import pe.edu.upc.viora.features.subscription.application.usecase.*
import pe.edu.upc.viora.features.subscription.presentation.state.IndividualCheckoutUiState
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.IndividualCheckoutViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class IndividualCheckoutViewModelTest {
    private val stores = mutableListOf<ViewModelStore>()
    private val plan = ProducerPlanCode("PRODUCER_ANNUAL")
    private val quota = PlanCapacity.of(BigDecimal("0.5"))
    private val purchase = IndividualPurchase(UUID.randomUUID().toString(), plan, quota, "PEN",
        BigDecimal("279.66"), BigDecimal("50.34"), BigDecimal("330.00"), "PENDING_PAYMENT")
    private var creates = 0
    private var checkouts = 0
    private var recovered: IndividualPurchase? = null
    private var delayed: CompletableDeferred<Unit>? = null
    private var failure: AppError? = null
    private val repository = object : IndividualPurchaseRepository {
        override suspend fun resume(): AppResult<IndividualPurchase?> {
            delayed?.await()
            return AppResult.Success(recovered)
        }
        override suspend fun prepare(plan: ProducerPlanCode, capacity: PlanCapacity): AppResult<IndividualPurchase> {
            creates++
            return failure?.let { AppResult.Failure(it) } ?: AppResult.Success(purchase)
        }
        override suspend fun checkout(subscriptionId: String): AppResult<IndividualCheckout> {
            checkouts++
            return failure?.let { AppResult.Failure(it) } ?: AppResult.Success(IndividualCheckout(UUID.randomUUID().toString(),
                "https://sandbox.mercadopago.com.pe/checkout/v1/redirect", Instant.parse("2026-10-23T12:00:00Z")))
        }
    }

    @Before fun setup() { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    @After fun teardown() { stores.forEach { it.clear() }; Dispatchers.resetMain() }
    private fun model() = IndividualCheckoutViewModel(PrepareIndividualPurchaseUseCase(repository),
        ResumeIndividualPurchaseUseCase(repository), CreateIndividualCheckoutUseCase(repository)).also {
            ViewModelStore().apply { put("checkout", it); stores += this }
        }

    @Test fun `opening the screen creates no contract and payment requires confirmation`() {
        val vm = model()
        assertEquals(0, creates)
        vm.start(plan, quota)
        assertEquals(1, creates)
        assertTrue(vm.uiState.value is IndividualCheckoutUiState.Confirm)
        assertEquals(0, checkouts)
        vm.pay()
        assertEquals(1, checkouts)
        assertTrue(vm.uiState.value is IndividualCheckoutUiState.Ready)
    }

    @Test fun `double taps cannot launch concurrent operations`() = runTest {
        delayed = CompletableDeferred()
        val vm = model()
        vm.start(plan, quota); vm.start(plan, quota)
        assertEquals(0, creates)
        delayed!!.complete(Unit)
        assertEquals(1, creates)
    }

    @Test fun `reopening recovers existing server terms rather than creating another contract`() {
        recovered = purchase.copy(total = BigDecimal("340.00"), base = BigDecimal("289.66"))
        val vm = model()
        vm.start(plan, quota)
        assertEquals(0, creates)
        assertEquals(recovered, (vm.uiState.value as IndividualCheckoutUiState.Confirm).purchase)
    }

    @Test fun `cancel dismisses presentation without cancelling the saved purchase`() {
        recovered = purchase
        val vm = model()
        vm.start(plan, quota); vm.dismiss()
        assertEquals(IndividualCheckoutUiState.Idle, vm.uiState.value)
        vm.start(plan, quota)
        assertEquals(0, creates)
        assertTrue(vm.uiState.value is IndividualCheckoutUiState.Confirm)
    }

    @Test fun `approved or unknown contract states never open another checkout`() {
        recovered = purchase.copy(status = "ACTIVE")
        val vm = model()
        vm.start(plan, quota); vm.pay()
        assertEquals(IndividualCheckoutUiState.NotPending, vm.uiState.value)
        assertEquals(0, checkouts)
    }

    @Test fun `checkout failures are displayed without granting access`() {
        val vm = model()
        vm.start(plan, quota)
        failure = AppError.Server(503)
        vm.pay()
        assertTrue(vm.uiState.value is IndividualCheckoutUiState.Failed)
    }
}
