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
import pe.edu.upc.viora.features.subscription.domain.repository.ProducerSubscriptionRepository
import pe.edu.upc.viora.features.subscription.domain.valueobject.*
import pe.edu.upc.viora.features.subscription.application.usecase.*
import pe.edu.upc.viora.features.subscription.presentation.state.IndividualCheckoutUiState
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.IndividualCheckoutViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class IndividualCheckoutViewModelTest {
    private val stores = mutableListOf<ViewModelStore>()
    private val plan = ProducerPlanCode("PRODUCER_ANNUAL")
    private val quota = PlanCapacity.of(BigDecimal("0.5"))
    private val purchase = IndividualPurchase(
        UUID.randomUUID().toString(), plan, quota, "PEN",
        BigDecimal("279.66"), BigDecimal("50.34"), BigDecimal("330.00"), "PENDING_PAYMENT"
    )
    private var creates = 0
    private var checkouts = 0
    private var recovered: IndividualPurchase? = null
    private var delayed: CompletableDeferred<Unit>? = null
    private var failure: AppError? = null

    private var subscriptionStatus: SubscriptionStatus = SubscriptionStatus.PENDING_PAYMENT
    private var entitlementActive: Boolean = false
    private var queryFailure: AppError? = null
    private var queryCount = 0

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
            return failure?.let { AppResult.Failure(it) } ?: AppResult.Success(
                IndividualCheckout(
                    UUID.randomUUID().toString(),
                    "https://sandbox.mercadopago.com.pe/checkout/v1/redirect",
                    Instant.parse("2026-10-23T12:00:00Z")
                )
            )
        }
    }

    private val subscriptionRepo = object : ProducerSubscriptionRepository {
        override suspend fun getCurrent(): AppResult<ProducerSubscription?> {
            return queryFailure?.let { AppResult.Failure(it) } ?: AppResult.Success(
                ProducerSubscription(
                    id = purchase.id,
                    mode = SubscriptionMode.INDIVIDUAL,
                    planCode = plan,
                    status = subscriptionStatus,
                    capacity = quota,
                    currency = "PEN",
                    baseAmount = purchase.base,
                    taxAmount = purchase.tax,
                    totalAmount = purchase.total,
                    startsAt = if (entitlementActive) Instant.parse("2026-10-22T00:00:00Z") else null,
                    endsAt = if (entitlementActive) Instant.parse("2027-10-22T00:00:00Z") else null,
                    entitlementActive = entitlementActive,
                    cooperativeId = null,
                )
            )
        }

        override suspend fun getById(id: String): AppResult<ProducerSubscription> {
            queryCount++
            return queryFailure?.let { AppResult.Failure(it) } ?: AppResult.Success(
                ProducerSubscription(
                    id = id,
                    mode = SubscriptionMode.INDIVIDUAL,
                    planCode = plan,
                    status = subscriptionStatus,
                    capacity = quota,
                    currency = "PEN",
                    baseAmount = purchase.base,
                    taxAmount = purchase.tax,
                    totalAmount = purchase.total,
                    startsAt = if (entitlementActive) Instant.parse("2026-10-22T00:00:00Z") else null,
                    endsAt = if (entitlementActive) Instant.parse("2027-10-22T00:00:00Z") else null,
                    entitlementActive = entitlementActive,
                    cooperativeId = null,
                )
            )
        }
    }

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        creates = 0
        checkouts = 0
        queryCount = 0
        recovered = null
        delayed = null
        failure = null
        subscriptionStatus = SubscriptionStatus.PENDING_PAYMENT
        entitlementActive = false
        queryFailure = null
    }

    @After
    fun teardown() {
        stores.forEach { it.clear() }
        Dispatchers.resetMain()
    }

    private fun model() = IndividualCheckoutViewModel(
        PrepareIndividualPurchaseUseCase(repository),
        ResumeIndividualPurchaseUseCase(repository),
        CreateIndividualCheckoutUseCase(repository),
        GetSubscriptionByIdUseCase(subscriptionRepo),
        GetCurrentSubscriptionUseCase(subscriptionRepo),
    ).also {
        ViewModelStore().apply { put("checkout", it); stores += this }
    }

    @Test
    fun `opening the screen creates no contract and payment requires confirmation`() = runTest(testDispatcher) {
        val vm = model()
        assertEquals(0, creates)
        vm.start(plan, quota)
        testScheduler.advanceUntilIdle()
        assertEquals(1, creates)
        assertTrue(vm.uiState.value is IndividualCheckoutUiState.Confirm)
        assertEquals(0, checkouts)
        vm.pay()
        testScheduler.advanceUntilIdle()
        assertEquals(1, checkouts)
        assertTrue(vm.uiState.value is IndividualCheckoutUiState.Ready)
    }

    @Test
    fun `double taps cannot launch concurrent operations`() = runTest(testDispatcher) {
        delayed = CompletableDeferred()
        val vm = model()
        vm.start(plan, quota); vm.start(plan, quota)
        testScheduler.runCurrent()
        assertEquals(0, creates)
        delayed!!.complete(Unit)
        testScheduler.advanceUntilIdle()
        assertEquals(1, creates)
    }

    @Test
    fun `reopening recovers existing server terms rather than creating another contract`() = runTest(testDispatcher) {
        recovered = purchase.copy(total = BigDecimal("340.00"), base = BigDecimal("289.66"))
        val vm = model()
        vm.start(plan, quota)
        testScheduler.advanceUntilIdle()
        assertEquals(0, creates)
        assertEquals(recovered, (vm.uiState.value as IndividualCheckoutUiState.Confirm).purchase)
    }

    @Test
    fun `cancel dismisses presentation without cancelling the saved purchase`() = runTest(testDispatcher) {
        recovered = purchase
        val vm = model()
        vm.start(plan, quota)
        testScheduler.advanceUntilIdle()
        vm.dismiss()
        assertEquals(IndividualCheckoutUiState.Idle, vm.uiState.value)
        vm.start(plan, quota)
        testScheduler.advanceUntilIdle()
        assertEquals(0, creates)
        assertTrue(vm.uiState.value is IndividualCheckoutUiState.Confirm)
    }

    @Test
    fun `approved or unknown contract states never open another checkout`() = runTest(testDispatcher) {
        recovered = purchase.copy(status = "ACTIVE")
        val vm = model()
        vm.start(plan, quota)
        testScheduler.advanceUntilIdle()
        vm.pay()
        testScheduler.advanceUntilIdle()
        assertEquals(IndividualCheckoutUiState.NotPending, vm.uiState.value)
        assertEquals(0, checkouts)
    }

    @Test
    fun `checkout failures are displayed without granting access`() = runTest(testDispatcher) {
        val vm = model()
        vm.start(plan, quota)
        testScheduler.advanceUntilIdle()
        failure = AppError.Server(503)
        vm.pay()
        testScheduler.advanceUntilIdle()
        assertTrue(vm.uiState.value is IndividualCheckoutUiState.Failed)
    }

    @Test
    fun `browser opened transitions to verifying state and onAppResumed initiates polling`() = runTest(testDispatcher) {
        val vm = model()
        vm.start(plan, quota)
        testScheduler.advanceUntilIdle()
        vm.pay()
        testScheduler.advanceUntilIdle()
        assertTrue(vm.uiState.value is IndividualCheckoutUiState.Ready)

        vm.onBrowserOpened(purchase.id)
        assertTrue(vm.uiState.value is IndividualCheckoutUiState.Verifying)

        vm.onAppResumed()
        testScheduler.runCurrent()
        assertTrue(queryCount >= 1)
        assertTrue(vm.uiState.value is IndividualCheckoutUiState.Verifying)
    }

    @Test
    fun `polling confirms access when entitlementActive becomes true`() = runTest(testDispatcher) {
        val vm = model()
        vm.onBrowserOpened(purchase.id)
        vm.onAppResumed()
        testScheduler.runCurrent()
        assertEquals(1, queryCount)
        assertTrue(vm.uiState.value is IndividualCheckoutUiState.Verifying)

        // Webhook arrives in background:
        subscriptionStatus = SubscriptionStatus.ACTIVE
        entitlementActive = true

        // Advance 5 seconds:
        testScheduler.advanceTimeBy(5_000L)
        testScheduler.runCurrent()
        assertEquals(2, queryCount)
        assertTrue(vm.uiState.value is IndividualCheckoutUiState.Confirmed)
        val confirmed = (vm.uiState.value as IndividualCheckoutUiState.Confirmed).subscription
        assertTrue(confirmed.entitlementActive)
        assertEquals(SubscriptionStatus.ACTIVE, confirmed.status)
    }

    @Test
    fun `polling times out after one minute visible and transitions to unconfirmed without claiming rejection`() = runTest(testDispatcher) {
        val vm = model()
        vm.onBrowserOpened(purchase.id)
        vm.onAppResumed()
        testScheduler.runCurrent()
        assertEquals(1, queryCount)

        // Advance 60 seconds (12 intervals of 5s):
        testScheduler.advanceTimeBy(60_000L)
        testScheduler.runCurrent()

        assertTrue(vm.uiState.value is IndividualCheckoutUiState.Unconfirmed)
        assertEquals(purchase.id, (vm.uiState.value as IndividualCheckoutUiState.Unconfirmed).subscriptionId)
    }

    @Test
    fun `checkStatus from unconfirmed state restarts verification immediately`() = runTest(testDispatcher) {
        val vm = model()
        vm.onBrowserOpened(purchase.id)
        vm.onAppResumed()
        testScheduler.advanceTimeBy(60_000L)
        testScheduler.runCurrent()
        assertTrue(vm.uiState.value is IndividualCheckoutUiState.Unconfirmed)

        val countBefore = queryCount
        // User taps "Consultar estado":
        vm.checkStatus()
        testScheduler.runCurrent()
        assertTrue(queryCount > countBefore)
        assertTrue(vm.uiState.value is IndividualCheckoutUiState.Verifying)
    }

    @Test
    fun `transient network failure during polling does not claim payment rejected`() = runTest(testDispatcher) {
        val vm = model()
        vm.onBrowserOpened(purchase.id)
        vm.onAppResumed()
        testScheduler.runCurrent()

        // Temporary network drop:
        queryFailure = AppError.Offline
        testScheduler.advanceTimeBy(5_000L)
        testScheduler.runCurrent()
        // It stays in Verifying, never jumps to payment failed/rejected:
        assertTrue(vm.uiState.value is IndividualCheckoutUiState.Verifying)

        // Connection restored:
        queryFailure = null
        testScheduler.advanceTimeBy(5_000L)
        testScheduler.runCurrent()
        assertTrue(vm.uiState.value is IndividualCheckoutUiState.Verifying)
    }

    @Test
    fun `app paused cancels polling coroutine`() = runTest(testDispatcher) {
        val vm = model()
        vm.onBrowserOpened(purchase.id)
        vm.onAppResumed()
        testScheduler.runCurrent()
        val countBefore = queryCount

        vm.onAppPaused()
        testScheduler.advanceTimeBy(20_000L)
        testScheduler.runCurrent()

        // No queries fired while paused:
        assertEquals(countBefore, queryCount)
    }
}
