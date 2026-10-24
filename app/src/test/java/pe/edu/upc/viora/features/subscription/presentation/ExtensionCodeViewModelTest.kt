package pe.edu.upc.viora.features.subscription.presentation

import java.math.BigDecimal
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
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
import pe.edu.upc.viora.features.subscription.application.usecase.RedeemExtensionCodeUseCase
import pe.edu.upc.viora.features.subscription.domain.entity.*
import pe.edu.upc.viora.features.subscription.domain.repository.PlanExtensionRepository
import pe.edu.upc.viora.features.subscription.domain.valueobject.ExtensionCode
import pe.edu.upc.viora.features.subscription.presentation.state.RedeemFailure
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.ExtensionCodeViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class ExtensionCodeViewModelTest {

    private var answer: AppResult<QuotaExtension> = AppResult.Failure(AppError.Validation())
    private var redeemed: String? = null

    private val repository = object : PlanExtensionRepository {
        override suspend fun terms(subscriptionId: String) = AppResult.Failure(AppError.NotFound())
        override suspend fun request(subscriptionId: String, addedHectares: BigDecimal, operationKey: String) = AppResult.Failure(AppError.NotFound())
        override suspend fun get(subscriptionId: String, extensionId: String) = AppResult.Failure(AppError.NotFound())
        override suspend fun checkout(extension: QuotaExtension, operationKey: String) = AppResult.Failure(AppError.NotFound())
        override suspend fun pendingPayment(): Pair<String, String>? = null
        override suspend fun forgetPendingPayment() = Unit
        override suspend fun redeem(code: ExtensionCode): AppResult<QuotaExtension> {
            redeemed = code.value
            return answer
        }
    }

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `the field normalizes what the producer types and waits for a whole code`() {
        val model = ExtensionCodeViewModel(RedeemExtensionCodeUseCase(repository))
        assertEquals("AMPL-", model.uiState.value.code)
        model.setCode("ampl-3r")
        assertFalse(model.uiState.value.canRedeem)
        model.redeem()
        assertNull(redeemed)
        model.setCode("ampl-3r8t-extra")
        assertEquals("AMPL-3R8T", model.uiState.value.code)
        assertTrue(model.uiState.value.canRedeem)
    }

    @Test
    fun `any code that does not add hectares shows the single rejected variant`() = runTest {
        val model = ExtensionCodeViewModel(RedeemExtensionCodeUseCase(repository))
        model.setCode("AMPL-3R8T")
        model.redeem()
        assertEquals("AMPL-3R8T", redeemed)
        assertEquals(RedeemFailure.NotRedeemable, model.uiState.value.failure)
        assertTrue(model.uiState.value.isCodeRejected)
        model.tryAnotherCode()
        assertEquals("AMPL-", model.uiState.value.code)
    }

    @Test
    fun `an applied code opens P104 with the grown quota`() = runTest {
        answer = AppResult.Success(QuotaExtension("e1", "s1", ExtensionStatus.APPLIED, BigDecimal("500"),
            BigDecimal("512"), Instant.parse("2027-10-21T12:00:00Z"), null, null, null, null))
        val model = ExtensionCodeViewModel(RedeemExtensionCodeUseCase(repository))
        model.setCode("AMPL-D500")
        model.redeem()
        assertEquals(0, BigDecimal("512").compareTo(model.uiState.value.applied!!.quotaHectares))
    }
}
