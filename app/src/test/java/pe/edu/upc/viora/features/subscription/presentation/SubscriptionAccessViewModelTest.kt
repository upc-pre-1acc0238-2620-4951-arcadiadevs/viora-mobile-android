package pe.edu.upc.viora.features.subscription.presentation

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.subscription.application.usecase.GetCurrentSubscriptionUseCase
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerSubscription
import pe.edu.upc.viora.features.subscription.domain.repository.ProducerSubscriptionRepository
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity
import pe.edu.upc.viora.features.subscription.domain.valueobject.ProducerPlanCode
import pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionMode
import pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionStatus
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.SubscriptionAccessViewModel

class SubscriptionAccessViewModelTest {

    private val sampleSubscription = ProducerSubscription(
        id = UUID.randomUUID().toString(),
        mode = SubscriptionMode.INDIVIDUAL,
        planCode = ProducerPlanCode("PRODUCER_ANNUAL"),
        status = SubscriptionStatus.ACTIVE,
        capacity = PlanCapacity.of(BigDecimal("0.5")),
        currency = "PEN",
        baseAmount = BigDecimal("279.66"),
        taxAmount = BigDecimal("50.34"),
        totalAmount = BigDecimal("330.00"),
        startsAt = Instant.parse("2026-10-22T00:00:00Z"),
        endsAt = Instant.parse("2027-10-22T00:00:00Z"),
        entitlementActive = true,
        cooperativeId = null,
    )

    @Test
    fun `initial uiState is null`() {
        val repo = object : ProducerSubscriptionRepository {
            override suspend fun getCurrent(): AppResult<ProducerSubscription?> = AppResult.Success(sampleSubscription)
            override suspend fun getById(id: String): AppResult<ProducerSubscription> = AppResult.Success(sampleSubscription)
        }
        val vm = SubscriptionAccessViewModel(GetCurrentSubscriptionUseCase(repo))

        assertNull(vm.uiState.value)
    }

    @Test
    fun `refresh populates uiState with active subscription on success`() = runTest {
        val repo = object : ProducerSubscriptionRepository {
            override suspend fun getCurrent(): AppResult<ProducerSubscription?> = AppResult.Success(sampleSubscription)
            override suspend fun getById(id: String): AppResult<ProducerSubscription> = AppResult.Success(sampleSubscription)
        }
        val vm = SubscriptionAccessViewModel(GetCurrentSubscriptionUseCase(repo))

        vm.refresh()

        val state = vm.uiState.value
        assertTrue(state is AppResult.Success)
        assertEquals(sampleSubscription, (state as AppResult.Success).value)
    }

    @Test
    fun `refresh populates uiState with null when no subscription exists`() = runTest {
        val repo = object : ProducerSubscriptionRepository {
            override suspend fun getCurrent(): AppResult<ProducerSubscription?> = AppResult.Success(null)
            override suspend fun getById(id: String): AppResult<ProducerSubscription> = AppResult.Success(sampleSubscription)
        }
        val vm = SubscriptionAccessViewModel(GetCurrentSubscriptionUseCase(repo))

        vm.refresh()

        val state = vm.uiState.value
        assertTrue(state is AppResult.Success)
        assertNull((state as AppResult.Success).value)
    }

    @Test
    fun `refresh populates uiState with failure on error`() = runTest {
        val repo = object : ProducerSubscriptionRepository {
            override suspend fun getCurrent(): AppResult<ProducerSubscription?> = AppResult.Failure(AppError.Offline)
            override suspend fun getById(id: String): AppResult<ProducerSubscription> = AppResult.Success(sampleSubscription)
        }
        val vm = SubscriptionAccessViewModel(GetCurrentSubscriptionUseCase(repo))

        vm.refresh()

        val state = vm.uiState.value
        assertTrue(state is AppResult.Failure)
        assertEquals(AppError.Offline, (state as AppResult.Failure).error)
    }
}
