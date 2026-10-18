package pe.edu.upc.viora.features.harvestsettlement.presentation

import androidx.lifecycle.SavedStateHandle
import java.io.File
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.DownloadDossierPdfUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.FindLatestDossierCampaignUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObserveAgronomicDossierUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.ObserveSettledHarvestsUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.SealAgronomicDossierUseCase
import pe.edu.upc.viora.features.harvestsettlement.application.usecase.VerifyDossierUseCase
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.AgronomicDossier
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.CachedDossier
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.DossierVerification
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.AgronomicDossierRepository
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.AgronomicDossierUiState
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.DossierVerificationState
import pe.edu.upc.viora.features.harvestsettlement.presentation.viewmodel.AgronomicDossierViewModel

private fun dossier(year: Int) = AgronomicDossier(
    id = "d-$year",
    plotId = "p1",
    campaignYear = year,
    plotName = "La Yarada 02",
    holderName = "Teodoro Mamani",
    variety = "SEVILLANA",
    areaHectares = 2.5,
    fruitLoad = null,
    prescription = null,
    thinning = null,
    harvest = null,
    climate = null,
    history = null,
    verificationHash = "a".repeat(64),
    sealedAt = Instant.parse("2026-05-14T15:40:00Z"),
    pageCount = 2,
    sizeBytes = 4_812,
    fileName = "Expediente_LaYarada02_$year.pdf",
)

private class FakeDossierRepository : AgronomicDossierRepository {
    val cache = MutableStateFlow<Map<Int, CachedDossier>>(emptyMap())
    var sealResult: AppResult<Unit> = AppResult.Success(Unit)
    var latestResult: AppResult<Int?> = AppResult.Success(null)
    var verifyResult: AppResult<DossierVerification> = AppResult.Failure(AppError.NotFound())
    val sealed = mutableListOf<Int>()

    override fun observeDossier(plotId: String, campaignYear: Int): Flow<CachedDossier?> = cache.map { it[campaignYear] }

    override suspend fun seal(plotId: String, campaignYear: Int): AppResult<Unit> {
        sealed += campaignYear
        if (sealResult is AppResult.Success) cache.value = cache.value + (campaignYear to CachedDossier(dossier(campaignYear), null, null))
        return sealResult
    }

    override suspend fun latestSealedCampaign(plotId: String): AppResult<Int?> = latestResult

    override suspend fun downloadPdf(plotId: String, campaignYear: Int): AppResult<File> = AppResult.Failure(AppError.Offline)

    override suspend fun verify(verificationHash: String): AppResult<DossierVerification> = verifyResult
}

@OptIn(ExperimentalCoroutinesApi::class)
class AgronomicDossierViewModelTest {

    private val dossiers = FakeDossierRepository()
    private val settlements = FakeSettleRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(campaignYear: Int = 2026): AgronomicDossierViewModel {
        val vm = AgronomicDossierViewModel(
            savedStateHandle = SavedStateHandle(mapOf("plotId" to "p1", "plotName" to "La Yarada 02", "campaignYear" to campaignYear)),
            observeDossier = ObserveAgronomicDossierUseCase(dossiers),
            observeSettledHarvests = ObserveSettledHarvestsUseCase(settlements),
            findLatestCampaign = FindLatestDossierCampaignUseCase(dossiers),
            sealDossier = SealAgronomicDossierUseCase(dossiers),
            downloadPdf = DownloadDossierPdfUseCase(dossiers),
            verifyDossier = VerifyDossierUseCase(dossiers),
        )
        vm.uiState.launchIn(CoroutineScope(Dispatchers.Main))
        return vm
    }

    @Test
    fun `opening the dossier of a campaign seals it and shows it`() = runTest {
        val state = viewModel().uiState.value as AgronomicDossierUiState.Content

        assertEquals(listOf(2026), dossiers.sealed)
        assertEquals("La Yarada 02", state.dossier.plotName)
        assertEquals(false, state.offline)
    }

    @Test
    fun `without signal the cached dossier is the offline variant`() = runTest {
        dossiers.cache.value = mapOf(2026 to CachedDossier(dossier(2026), null, null))
        dossiers.sealResult = AppResult.Failure(AppError.Offline)

        val state = viewModel().uiState.value as AgronomicDossierUiState.Content

        assertTrue(state.offline)
    }

    @Test
    fun `without signal and nothing cached it is an error`() = runTest {
        dossiers.sealResult = AppResult.Failure(AppError.Offline)

        assertEquals(AgronomicDossierUiState.Error(AppError.Offline), viewModel().uiState.value)
    }

    @Test
    fun `from more options the newest settled campaign is sealed`() = runTest {
        dossiers.latestResult = AppResult.Success(2025)
        settlements.cache.value = listOf(testSettlement("p1", 2026), testSettlement("p2", 2027))

        viewModel(campaignYear = 0)

        assertEquals(listOf(2026), dossiers.sealed)
    }

    @Test
    fun `a plot with no settled campaign has no dossier`() = runTest {
        assertEquals(AgronomicDossierUiState.NoDossier("La Yarada 02"), viewModel(campaignYear = 0).uiState.value)
        assertTrue(dossiers.sealed.isEmpty())
    }

    @Test
    fun `a code the record does not store is no match`() = runTest {
        val vm = viewModel()

        vm.verify()

        assertEquals(DossierVerificationState.NoMatch, vm.verification.value)
    }

    @Test
    fun `a record that cannot be reached is not a no match`() = runTest {
        dossiers.verifyResult = AppResult.Failure(AppError.Offline)
        val vm = viewModel()

        vm.verify()

        assertEquals(DossierVerificationState.Unreachable, vm.verification.value)
    }

    @Test
    fun `a failed download says so and keeps the sheet closed`() = runTest {
        val vm = viewModel()

        vm.openPdf()

        val state = vm.uiState.value as AgronomicDossierUiState.Content
        assertTrue(state.downloadFailed)
        assertTrue(state.offline)
        assertEquals(null, vm.pdfSheet.value)
    }
}
