package pe.edu.upc.viora.features.subscription.presentation

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import pe.edu.upc.viora.features.subscription.application.usecase.RedeemCooperativeCodeUseCase
import pe.edu.upc.viora.features.subscription.domain.entity.CooperativeMembership
import pe.edu.upc.viora.features.subscription.domain.entity.RedemptionOutcome
import pe.edu.upc.viora.features.subscription.domain.repository.CooperativeCodeRepository
import pe.edu.upc.viora.features.subscription.domain.valueobject.InvitationCode
import pe.edu.upc.viora.features.subscription.presentation.state.RedeemFailure
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.RedeemCodeViewModel

private class FakeCodeRepository : CooperativeCodeRepository {
    var outcome = CompletableDeferred<RedemptionOutcome>()
    val redeemed = mutableListOf<InvitationCode>()

    override suspend fun redeem(code: InvitationCode): RedemptionOutcome {
        redeemed += code
        return outcome.await()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class RedeemCodeViewModelTest {

    private val repository = FakeCodeRepository()
    private lateinit var viewModel: RedeemCodeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = RedeemCodeViewModel(RedeemCooperativeCodeUseCase(repository), SavedStateHandle())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the field starts with the prefix and cannot be redeemed until the code is complete`() {
        assertEquals("COOP-", viewModel.uiState.value.code)
        assertFalse(viewModel.uiState.value.canRedeem)

        viewModel.setCode("coop-7k2")
        assertFalse(viewModel.uiState.value.canRedeem)

        viewModel.setCode("coop-7k2q")
        assertEquals("COOP-7K2Q", viewModel.uiState.value.code)
        assertTrue(viewModel.uiState.value.canRedeem)
    }

    @Test
    fun `the field never grows past the length of a code`() {
        viewModel.setCode("COOP-7K2QXYZ")

        assertEquals("COOP-7K2Q", viewModel.uiState.value.code)
    }

    @Test
    fun `redeeming shows the progress, then hands over the membership`() {
        viewModel.setCode("COOP-7K2Q")
        viewModel.redeem()

        assertTrue(viewModel.uiState.value.isRedeeming)
        assertFalse(viewModel.uiState.value.canRedeem)

        val membership = CooperativeMembership("Coop. Olivarera La Yarada", 12.0)
        repository.outcome.complete(RedemptionOutcome.Redeemed(membership))

        assertFalse(viewModel.uiState.value.isRedeeming)
        assertEquals(membership, viewModel.uiState.value.redeemed)
        assertEquals(listOf("COOP-7K2Q"), repository.redeemed.map { it.value })
    }

    @Test
    fun `a code that does not work turns the screen into its rejected state, and another code clears it`() {
        viewModel.setCode("COOP-7K2Q")
        viewModel.redeem()
        repository.outcome.complete(RedemptionOutcome.NotRedeemable)

        assertTrue(viewModel.uiState.value.isCodeRejected)
        assertEquals("COOP-7K2Q", viewModel.uiState.value.code)

        viewModel.tryAnotherCode()

        assertNull(viewModel.uiState.value.failure)
        assertEquals("COOP-", viewModel.uiState.value.code)
    }

    @Test
    fun `typing again clears a failure`() {
        viewModel.setCode("COOP-7K2Q")
        viewModel.redeem()
        repository.outcome.complete(RedemptionOutcome.NotRedeemable)

        viewModel.setCode("COOP-7K2")

        assertNull(viewModel.uiState.value.failure)
    }

    @Test
    fun `the other failures keep the code and say why`() {
        val cases = mapOf(
            RedemptionOutcome.AlreadySubscribed to RedeemFailure.AlreadySubscribed,
            RedemptionOutcome.TooManyAttempts to RedeemFailure.TooManyAttempts,
            RedemptionOutcome.Failed(AppError.Offline) to RedeemFailure.Other(AppError.Offline),
        )
        cases.forEach { (outcome, failure) ->
            repository.outcome = CompletableDeferred(outcome)
            viewModel.setCode("COOP-7K2Q")
            viewModel.redeem()

            assertEquals(failure, viewModel.uiState.value.failure)
            assertFalse(viewModel.uiState.value.isCodeRejected)
            assertEquals("COOP-7K2Q", viewModel.uiState.value.code)
        }
    }

    @Test
    fun `an incomplete code is never sent`() {
        viewModel.setCode("COOP-7K")
        viewModel.redeem()

        assertTrue(repository.redeemed.isEmpty())
    }

    @Test
    fun `a code handed over from the onboarding is redeemed as soon as the screen opens`() {
        val opened = RedeemCodeViewModel(RedeemCooperativeCodeUseCase(repository), SavedStateHandle(mapOf("code" to "coop-7k2q")))

        assertEquals("COOP-7K2Q", opened.uiState.value.code)
        assertTrue(opened.uiState.value.isRedeeming)
        assertEquals(listOf("COOP-7K2Q"), repository.redeemed.map { it.value })
    }

    @Test
    fun `a malformed code handed over from the onboarding waits in the field instead of being sent`() {
        val opened = RedeemCodeViewModel(RedeemCooperativeCodeUseCase(repository), SavedStateHandle(mapOf("code" to "coop-7k")))

        assertEquals("COOP-7K", opened.uiState.value.code)
        assertFalse(opened.uiState.value.isRedeeming)
        assertTrue(repository.redeemed.isEmpty())
    }
}
