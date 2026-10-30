package pe.edu.upc.viora.features.profiles.presentation

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
import pe.edu.upc.viora.core.locale.AppLanguage
import pe.edu.upc.viora.features.croploadregulation.application.usecase.ObservePendingDraftSamplesCountUseCase
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningRepository
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObservePendingSettlementsUseCase
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.PendingSettlement
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.HarvestSettlementRepository
import pe.edu.upc.viora.features.home.application.usecase.RestartHomeTourUseCase
import pe.edu.upc.viora.features.home.domain.repository.HomeTourRepository
import pe.edu.upc.viora.features.iam.application.usecase.SignOutUseCase
import pe.edu.upc.viora.features.iam.domain.repository.AuthRepository
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObserveFieldNotesUseCase
import pe.edu.upc.viora.features.plotmanagement.application.published.FieldNote
import pe.edu.upc.viora.features.plotmanagement.application.published.FieldNoteSyncState
import pe.edu.upc.viora.features.plotmanagement.application.published.FieldNoteType
import pe.edu.upc.viora.features.plotmanagement.domain.repository.FieldNoteRepository
import pe.edu.upc.viora.features.profiles.application.usecase.ObserveAnimatedVoiceUseCase
import pe.edu.upc.viora.features.profiles.application.usecase.ObserveProfileUseCase
import pe.edu.upc.viora.features.profiles.application.usecase.RefreshProfileUseCase
import pe.edu.upc.viora.features.profiles.application.usecase.SetAnimatedVoiceUseCase
import pe.edu.upc.viora.features.profiles.domain.entity.UserProfile
import pe.edu.upc.viora.features.profiles.domain.repository.AccountPreferencesRepository
import pe.edu.upc.viora.features.profiles.domain.repository.ProfileRepository
import pe.edu.upc.viora.features.profiles.domain.valueobject.ProfileId
import pe.edu.upc.viora.features.profiles.presentation.viewmodel.AccountViewModel
import pe.edu.upc.viora.features.subscription.application.usecase.GetCurrentSubscriptionUseCase
import pe.edu.upc.viora.features.subscription.domain.entity.ProducerSubscription
import pe.edu.upc.viora.features.subscription.domain.repository.ProducerSubscriptionRepository
import pe.edu.upc.viora.features.subscription.domain.valueobject.PlanCapacity
import pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionMode
import pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionStatus
import pe.edu.upc.viora.testing.unused

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    private val profile = UserProfile(ProfileId("p-1"), "u-1", "Teodoro Mamani", "PE", "+51952184337")
    private var subscription: AppResult<ProducerSubscription?> = AppResult.Success(subscription(entitlementActive = true))
    private val animatedVoice = MutableStateFlow(true)
    private val pendingSamples = MutableStateFlow(0)
    private val notes = MutableStateFlow<List<FieldNote>>(emptyList())
    private var tourResets = 0
    private var signOuts = 0

    private val profiles = object : ProfileRepository by unused<ProfileRepository>() {
        override fun observeProfile(): Flow<UserProfile?> = flowOf(profile)
        override suspend fun refreshProfile(): AppResult<UserProfile> = AppResult.Failure(AppError.Offline)
    }
    private val subscriptions = object : ProducerSubscriptionRepository by unused<ProducerSubscriptionRepository>() {
        override suspend fun getCurrent(): AppResult<ProducerSubscription?> = subscription
    }
    private val tours = object : HomeTourRepository by unused<HomeTourRepository>() {
        override suspend fun reset() {
            tourResets++
        }
    }
    private val auth = object : AuthRepository by unused<AuthRepository>() {
        override suspend fun signOut() {
            signOuts++
        }
    }
    private val preferences = object : AccountPreferencesRepository by unused<AccountPreferencesRepository>() {
        override val animatedVoice: Flow<Boolean> = this@AccountViewModelTest.animatedVoice
        override suspend fun setAnimatedVoice(enabled: Boolean) {
            this@AccountViewModelTest.animatedVoice.value = enabled
        }
    }
    private val thinning = object : ThinningRepository by unused<ThinningRepository>() {
        override fun observePendingDraftSamplesCount(): Flow<Int> = pendingSamples
    }
    private val settlements = object : HarvestSettlementRepository by unused<HarvestSettlementRepository>() {
        override fun observePending(): Flow<List<PendingSettlement>> = flowOf(emptyList())
    }
    private val fieldNotes = object : FieldNoteRepository by unused<FieldNoteRepository>() {
        override fun observeNotes(): Flow<List<FieldNote>> = notes
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun subscription(entitlementActive: Boolean) = ProducerSubscription(
        id = "7f3c1e52-9a4b-4d1e-8c2a-1b6f0e9d4a21", mode = SubscriptionMode.INDIVIDUAL, planCode = null,
        status = if (entitlementActive) SubscriptionStatus.ACTIVE else SubscriptionStatus.PENDING_PAYMENT,
        capacity = PlanCapacity.of(BigDecimal("5")), currency = null, baseAmount = null, taxAmount = null,
        totalAmount = null, startsAt = null, endsAt = null, entitlementActive = entitlementActive, cooperativeId = null,
    )

    private fun note(id: String, state: FieldNoteSyncState) = FieldNote(
        id = id, plotId = "plot-1", type = FieldNoteType.IRRIGATION, text = "Riego", observedOn = LocalDate.of(2026, 10, 1),
        createdAt = Instant.parse("2026-10-01T12:00:00Z"), hasPhoto = false, syncState = state,
    )

    private fun viewModel() = AccountViewModel(
        observeProfile = ObserveProfileUseCase(profiles),
        refreshProfile = RefreshProfileUseCase(profiles),
        getCurrentSubscription = GetCurrentSubscriptionUseCase(subscriptions),
        restartHomeTour = RestartHomeTourUseCase(tours),
        signOut = SignOutUseCase(auth),
        observeAnimatedVoice = ObserveAnimatedVoiceUseCase(preferences),
        setAnimatedVoice = SetAnimatedVoiceUseCase(preferences),
        observePendingSamples = ObservePendingDraftSamplesCountUseCase(thinning),
        observePendingSettlements = ObservePendingSettlementsUseCase(settlements),
        observeFieldNotes = ObserveFieldNotesUseCase(fieldNotes),
    )

    @Test
    fun `shows the kept profile and the active access even when the refresh fails`() {
        val state = viewModel().uiState.value

        assertEquals(profile, state.profile)
        assertEquals(true, state.isMember)
    }

    @Test
    fun `an inactive access is no member badge`() {
        subscription = AppResult.Success(subscription(entitlementActive = false))

        assertEquals(false, viewModel().uiState.value.isMember)
    }

    @Test
    fun `offline the badge stays unknown instead of claiming no access`() {
        subscription = AppResult.Failure(AppError.Offline)

        assertNull(viewModel().uiState.value.isMember)
    }

    @Test
    fun `unsent records add the draft samples and the notes the server does not have`() {
        val viewModel = viewModel()

        pendingSamples.value = 2
        notes.value = listOf(
            note("n-1", FieldNoteSyncState.PENDING),
            note("n-2", FieldNoteSyncState.FAILED),
            note("n-3", FieldNoteSyncState.SYNCED),
        )

        assertEquals(4, viewModel.uiState.value.unsentRecords)
    }

    @Test
    fun `with nothing unsent signing out happens at once`() {
        val viewModel = viewModel()

        viewModel.signOut()

        assertEquals(1, signOuts)
        assertTrue(viewModel.uiState.value.isSignedOut)
        assertFalse(viewModel.uiState.value.isSigningOut)
    }

    @Test
    fun `with records waiting it asks first and only signs out once confirmed`() {
        pendingSamples.value = 1
        val viewModel = viewModel()

        viewModel.signOut()

        assertTrue(viewModel.uiState.value.confirmingSignOut)
        assertEquals(0, signOuts)

        viewModel.confirmSignOut()

        assertEquals(1, signOuts)
        assertFalse(viewModel.uiState.value.confirmingSignOut)
        assertTrue(viewModel.uiState.value.isSignedOut)
    }

    @Test
    fun `dismissing the question keeps the session`() {
        pendingSamples.value = 1
        val viewModel = viewModel()

        viewModel.signOut()
        viewModel.dismissSignOut()

        assertFalse(viewModel.uiState.value.confirmingSignOut)
        assertEquals(0, signOuts)
    }

    @Test
    fun `choosing a language closes the sheet and leaves the notice for when the screen is back`() {
        val viewModel = viewModel()

        viewModel.openLanguageSheet()
        assertTrue(viewModel.uiState.value.showLanguageSheet)
        viewModel.onLanguageChosen(AppLanguage.ENGLISH)

        assertFalse(viewModel.uiState.value.showLanguageSheet)
        assertEquals(AppLanguage.ENGLISH, viewModel.uiState.value.languageNotice)

        viewModel.dismissLanguageNotice()
        assertNull(viewModel.uiState.value.languageNotice)
    }

    @Test
    fun `the animated voice switch is saved and shown`() {
        val viewModel = viewModel()

        viewModel.onAnimatedVoiceChange(false)

        assertFalse(animatedVoice.value)
        assertFalse(viewModel.uiState.value.animatedVoice)
    }

    @Test
    fun `replaying the home tour resets it before going back`() {
        val viewModel = viewModel()
        var wentBack = false

        viewModel.replayHomeTour { wentBack = true }

        assertEquals(1, tourResets)
        assertTrue(wentBack)
    }
}
