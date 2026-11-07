package pe.edu.upc.viora.features.subscription.presentation

import androidx.lifecycle.SavedStateHandle
import java.time.Instant
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
import pe.edu.upc.viora.features.subscription.application.usecase.PreviewCooperativeCodeUseCase
import pe.edu.upc.viora.features.subscription.application.usecase.RedeemCooperativeCodeUseCase
import pe.edu.upc.viora.features.subscription.domain.entity.CodePreview
import pe.edu.upc.viora.features.subscription.domain.entity.CodePreviewOutcome
import pe.edu.upc.viora.features.subscription.domain.entity.CooperativeMembership
import pe.edu.upc.viora.features.subscription.domain.entity.RedemptionOutcome
import pe.edu.upc.viora.features.subscription.domain.repository.CooperativeCodeRepository
import pe.edu.upc.viora.features.subscription.domain.valueobject.InvitationCode
import pe.edu.upc.viora.features.subscription.presentation.state.RedeemFailure
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.RedeemCodeViewModel

private val PREVIEW = CodePreview(
    organizationName = "Coop. La Yarada",
    managerName = "Rubén Ticona",
    quotaHectares = 12.0,
    planEndsAt = Instant.parse("2027-12-31T05:00:00Z"),
    registeredHectares = 4.9,
    exceedsQuota = false,
    consentTextVersion = "2026-11-v1",
)

private class FakeCodeRepository : CooperativeCodeRepository {
    val previews = ArrayDeque<CompletableDeferred<CodePreviewOutcome>>()
    var redemption = CompletableDeferred<RedemptionOutcome>()
    val previewed = mutableListOf<String>()
    val redeemed = mutableListOf<Pair<String, String>>()

    override suspend fun preview(code: InvitationCode): CodePreviewOutcome {
        previewed += code.value
        return (previews.removeFirstOrNull() ?: CompletableDeferred()).await()
    }

    override suspend fun redeem(code: InvitationCode, consentTextVersion: String): RedemptionOutcome {
        redeemed += code.value to consentTextVersion
        return redemption.await()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class RedeemCodeViewModelTest {

    private val repository = FakeCodeRepository()
    private lateinit var viewModel: RedeemCodeViewModel

    private fun open(saved: Map<String, Any?> = emptyMap()) = RedeemCodeViewModel(
        PreviewCooperativeCodeUseCase(repository),
        RedeemCooperativeCodeUseCase(repository),
        SavedStateHandle(saved),
    )

    private fun previewWith(outcome: CodePreviewOutcome) {
        repository.previews += CompletableDeferred(outcome)
    }

    /** P05 with a complete code, already answered with [PREVIEW]: P05b is open. */
    private fun openConsent() {
        previewWith(CodePreviewOutcome.Previewed(PREVIEW))
        viewModel.setCode("COOP-7K2Q")
        viewModel.validate()
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = open()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the field starts with the prefix and cannot be checked until the code is complete`() {
        assertEquals("COOP-", viewModel.uiState.value.code)
        assertFalse(viewModel.uiState.value.canValidate)

        viewModel.setCode("coop-7k2")
        assertFalse(viewModel.uiState.value.canValidate)

        viewModel.setCode("coop-7k2q")
        assertEquals("COOP-7K2Q", viewModel.uiState.value.code)
        assertTrue(viewModel.uiState.value.canValidate)
    }

    @Test
    fun `the field never grows past the length of a code`() {
        viewModel.setCode("COOP-7K2QXYZ")

        assertEquals("COOP-7K2Q", viewModel.uiState.value.code)
    }

    @Test
    fun `checking a code only previews it and opens the consent, nothing is redeemed`() {
        val pending = CompletableDeferred<CodePreviewOutcome>()
        repository.previews += pending
        viewModel.setCode("COOP-7K2Q")
        viewModel.validate()

        assertTrue(viewModel.uiState.value.isValidating)

        pending.complete(CodePreviewOutcome.Previewed(PREVIEW))

        assertFalse(viewModel.uiState.value.isValidating)
        assertEquals(PREVIEW, viewModel.uiState.value.preview)
        assertEquals(listOf("COOP-7K2Q"), repository.previewed)
        assertTrue(repository.redeemed.isEmpty())
    }

    @Test
    fun `accepting redeems with the consent text that was shown and hands over the membership with its manager`() {
        openConsent()

        viewModel.accept()
        assertTrue(viewModel.uiState.value.isAccepting)

        repository.redemption.complete(
            RedemptionOutcome.Redeemed(CooperativeMembership("Coop. La Yarada", 12.0, Instant.parse("2027-12-31T05:00:00Z"))),
        )

        assertEquals(listOf("COOP-7K2Q" to "2026-11-v1"), repository.redeemed)
        assertEquals(
            CooperativeMembership("Coop. La Yarada", 12.0, Instant.parse("2027-12-31T05:00:00Z"), "Rubén Ticona"),
            viewModel.uiState.value.redeemed,
        )
    }

    @Test
    fun `not now goes back to an empty field and redeems nothing`() {
        openConsent()

        viewModel.tryAnotherCode()

        assertNull(viewModel.uiState.value.preview)
        assertEquals("COOP-", viewModel.uiState.value.code)
        assertTrue(repository.redeemed.isEmpty())
    }

    @Test
    fun `a code that does not work turns P05 into its rejected state, and another code clears it`() {
        previewWith(CodePreviewOutcome.NotRedeemable)
        viewModel.setCode("COOP-7K2Q")
        viewModel.validate()

        assertTrue(viewModel.uiState.value.isCodeRejected)
        assertEquals("COOP-7K2Q", viewModel.uiState.value.code)

        viewModel.tryAnotherCode()

        assertNull(viewModel.uiState.value.failure)
        assertEquals("COOP-", viewModel.uiState.value.code)
    }

    @Test
    fun `a producer with a plan in force sees why the code cannot be used`() {
        previewWith(CodePreviewOutcome.AlreadySubscribed)
        viewModel.setCode("COOP-7K2Q")
        viewModel.validate()

        assertTrue(viewModel.uiState.value.hasActivePlan)
        assertFalse(viewModel.uiState.value.isCodeRejected)
    }

    @Test
    fun `typing again clears a failure`() {
        previewWith(CodePreviewOutcome.NotRedeemable)
        viewModel.setCode("COOP-7K2Q")
        viewModel.validate()

        viewModel.setCode("COOP-7K2")

        assertNull(viewModel.uiState.value.failure)
    }

    @Test
    fun `the other preview failures keep the code and say why`() {
        val cases = mapOf(
            CodePreviewOutcome.TooManyAttempts to RedeemFailure.TooManyAttempts,
            CodePreviewOutcome.Failed(AppError.Offline) to RedeemFailure.Other(AppError.Offline),
        )
        cases.forEach { (outcome, failure) ->
            previewWith(outcome)
            viewModel.setCode("COOP-7K2Q")
            viewModel.validate()

            assertEquals(failure, viewModel.uiState.value.failure)
            assertFalse(viewModel.uiState.value.isCodeRejected)
            assertEquals("COOP-7K2Q", viewModel.uiState.value.code)
        }
    }

    @Test
    fun `a code spent while the consent was open goes back to P05 as rejected`() {
        openConsent()

        viewModel.accept()
        repository.redemption.complete(RedemptionOutcome.NotRedeemable)

        assertNull(viewModel.uiState.value.preview)
        assertTrue(viewModel.uiState.value.isCodeRejected)
    }

    @Test
    fun `a consent text that changed is shown again to be accepted`() {
        openConsent()
        val newer = PREVIEW.copy(consentTextVersion = "2026-12-v2")
        previewWith(CodePreviewOutcome.Previewed(newer))

        viewModel.accept()
        repository.redemption.complete(RedemptionOutcome.ConsentOutdated)

        assertEquals(newer, viewModel.uiState.value.preview)
        assertFalse(viewModel.uiState.value.isAccepting)
        assertNull(viewModel.uiState.value.redeemed)
    }

    @Test
    fun `a redemption that fails offline keeps the consent open and says why`() {
        openConsent()

        viewModel.accept()
        repository.redemption.complete(RedemptionOutcome.Failed(AppError.Offline))

        assertEquals(PREVIEW, viewModel.uiState.value.preview)
        assertEquals(RedeemFailure.Other(AppError.Offline), viewModel.uiState.value.failure)
    }

    @Test
    fun `an incomplete code is never sent`() {
        viewModel.setCode("COOP-7K")
        viewModel.validate()

        assertTrue(repository.previewed.isEmpty())
    }

    @Test
    fun `a code handed over from the onboarding is checked as soon as the screen opens`() {
        val opened = open(mapOf("code" to "coop-7k2q"))

        assertEquals("COOP-7K2Q", opened.uiState.value.code)
        assertTrue(opened.uiState.value.isValidating)
        assertEquals(listOf("COOP-7K2Q"), repository.previewed)
    }

    @Test
    fun `a malformed code handed over from the onboarding waits in the field instead of being sent`() {
        val opened = open(mapOf("code" to "coop-7k"))

        assertEquals("COOP-7K", opened.uiState.value.code)
        assertFalse(opened.uiState.value.isValidating)
        assertTrue(repository.previewed.isEmpty())
    }
}
