package pe.edu.upc.viora.features.subscription.presentation

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.subscription.application.usecase.*
import pe.edu.upc.viora.features.subscription.domain.entity.*
import pe.edu.upc.viora.features.subscription.domain.repository.PlanExtensionRepository
import pe.edu.upc.viora.features.subscription.domain.repository.ProducerSubscriptionRepository
import pe.edu.upc.viora.features.subscription.domain.valueobject.*
import pe.edu.upc.viora.features.subscription.presentation.state.PlanExtensionUiState
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.PlanExtensionViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class PlanExtensionViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val subscriptionId = UUID.randomUUID().toString()
    private val endsAt = Instant.parse("2027-10-21T12:00:00Z")
    private var action = ExtensionAction.PAY
    private var requests = 0
    private var extensionStatus = ExtensionStatus.PENDING_PAYMENT
    private var paymentStatus: String? = "OPEN"
    private var pending: Pair<String, String>? = null

    private val subscriptions = object : ProducerSubscriptionRepository {
        override suspend fun getCurrent(): AppResult<ProducerSubscription?> = AppResult.Success(
            ProducerSubscription(subscriptionId, SubscriptionMode.INDIVIDUAL, ProducerPlanCode("PRODUCER_ANNUAL"),
                SubscriptionStatus.ACTIVE, PlanCapacity.of(BigDecimal("12")), "PEN", null, null, null,
                Instant.parse("2026-10-21T12:00:00Z"), endsAt, true, null),
        )
        override suspend fun getById(id: String): AppResult<ProducerSubscription> = AppResult.Failure(AppError.NotFound())
    }

    private fun extension(added: BigDecimal) = QuotaExtension(
        id = "ext-$added", subscriptionId = subscriptionId, status = extensionStatus, addedHectares = added,
        quotaHectares = if (extensionStatus == ExtensionStatus.APPLIED) BigDecimal("12").add(added) else BigDecimal("12"),
        endsAt = endsAt, billedMonths = 10, currency = "PEN", total = added.multiply(BigDecimal(550)), paymentStatus = paymentStatus,
    )

    private val extensions = object : PlanExtensionRepository {
        override suspend fun terms(subscriptionId: String) = AppResult.Success(
            PlanExtensionTerms(subscriptionId, action, BigDecimal("12"), endsAt,
                if (action == ExtensionAction.PAY) ExtensionPricing(10, BigDecimal("55"), "PEN", BigDecimal("0.5"), BigDecimal("0.5")) else null),
        )
        override suspend fun request(subscriptionId: String, addedHectares: BigDecimal, operationKey: String): AppResult<QuotaExtension> {
            requests++
            return AppResult.Success(extension(addedHectares))
        }
        override suspend fun get(subscriptionId: String, extensionId: String): AppResult<QuotaExtension> =
            AppResult.Success(extension(BigDecimal(extensionId.removePrefix("ext-"))))
        override suspend fun checkout(extension: QuotaExtension, operationKey: String): AppResult<IndividualCheckout> {
            pending = extension.subscriptionId to extension.id
            return AppResult.Success(IndividualCheckout(UUID.randomUUID().toString(),
                "https://sandbox.mercadopago.com.pe/checkout/v1/redirect", Instant.parse("2026-10-25T12:00:00Z")))
        }
        override suspend fun pendingPayment() = pending
        override suspend fun forgetPendingPayment() { pending = null }
        override suspend fun redeem(code: ExtensionCode): AppResult<QuotaExtension> = AppResult.Failure(AppError.Validation())
    }

    private fun model() = PlanExtensionViewModel(
        GetCurrentSubscriptionUseCase(subscriptions),
        GetPlanExtensionTermsUseCase(extensions),
        RequestPaidExtensionUseCase(extensions),
        GetQuotaExtensionUseCase(extensions),
        CreateExtensionCheckoutUseCase(extensions),
        PendingExtensionPaymentUseCase(extensions),
    ).apply { pollIntervalMs = 1_000L; maxPollDurationMs = 3_000L }

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `a paid plan picks hectares and pays only the months left`() = runTest(dispatcher) {
        val model = model()
        model.start(returning = false)
        advanceUntilIdle()
        var state = model.uiState.value as PlanExtensionUiState.Selecting
        assertEquals(0, BigDecimal.ONE.compareTo(state.addedHectares))
        model.increase(); model.increase()
        state = model.uiState.value as PlanExtensionUiState.Selecting
        assertEquals(0, BigDecimal("2").compareTo(state.addedHectares))
        assertEquals(0, BigDecimal("1100").compareTo(state.total))
        assertEquals(0, BigDecimal("14").compareTo(state.newQuota))
    }

    @Test
    fun `going back and forth to the same hectares requests the extension once`() = runTest(dispatcher) {
        val model = model()
        model.start(returning = false)
        advanceUntilIdle()
        model.increase(); model.decrease()
        model.pay(); advanceUntilIdle()
        model.onBrowserOpened()
        model.retry(); advanceUntilIdle()
        model.pay(); advanceUntilIdle()
        assertEquals(1, requests)
    }

    @Test
    fun `back from Mercado Pago the applied extension shows the grown quota`() = runTest(dispatcher) {
        val first = model()
        first.start(returning = false)
        advanceUntilIdle()
        first.pay(); advanceUntilIdle()
        extensionStatus = ExtensionStatus.APPLIED
        paymentStatus = "APPROVED"
        val returning = model()
        returning.onResumed()
        returning.start(returning = true)
        advanceUntilIdle()
        val applied = returning.uiState.value as PlanExtensionUiState.Applied
        assertEquals(0, BigDecimal("13").compareTo(applied.extension.quotaHectares))
        assertEquals(0, BigDecimal("12").compareTo(applied.previousQuota))
        assertEquals(null, pending)
    }

    @Test
    fun `a payment that never arrives ends unconfirmed and a rejected one offers to retry`() = runTest(dispatcher) {
        val model = model()
        model.start(returning = false)
        advanceUntilIdle()
        model.pay(); advanceUntilIdle()
        model.onBrowserOpened()
        model.onResumed()
        advanceTimeBy(5_000L); advanceUntilIdle()
        assertTrue(model.uiState.value is PlanExtensionUiState.Unconfirmed)
        paymentStatus = "REJECTED"
        model.checkAgain(); advanceUntilIdle()
        assertTrue(model.uiState.value is PlanExtensionUiState.Rejected)
    }

    @Test
    fun `a plan activated with a code is handed over to P103`() = runTest(dispatcher) {
        action = ExtensionAction.CODE
        val model = model()
        model.start(returning = false)
        advanceUntilIdle()
        assertEquals(PlanExtensionUiState.Redirect(ExtensionAction.CODE), model.uiState.value)
    }
}
