package pe.edu.upc.viora.features.harvestsettlement.infrastructure

import java.security.MessageDigest
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.core.network.ApiErrorMapper
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.AgronomicDossierDao
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.AgronomicDossierEntity
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.AgronomicDossierDto
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.AgronomicDossierService
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.DossierHarvestDto
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.DossierThinningDto
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.DossierVerificationDto
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.SealDossierRequestDto
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.repository.AgronomicDossierRepositoryImpl
import retrofit2.Response

private class FakeDossierDao : AgronomicDossierDao {
    val rows = MutableStateFlow<Map<Pair<String, Int>, AgronomicDossierEntity>>(emptyMap())

    override fun observe(plotId: String, campaignYear: Int): Flow<AgronomicDossierEntity?> = rows.map { it[plotId to campaignYear] }

    override suspend fun find(plotId: String, campaignYear: Int): AgronomicDossierEntity? = rows.value[plotId to campaignYear]

    override suspend fun upsert(entity: AgronomicDossierEntity) {
        rows.value = rows.value + ((entity.plotId to entity.campaignYear) to entity)
    }

    override suspend fun updatePdf(plotId: String, campaignYear: Int, pdfPath: String, savedAtEpochMs: Long) {
        val row = rows.value[plotId to campaignYear] ?: return
        upsert(row.copy(pdfPath = pdfPath, pdfSavedAtEpochMs = savedAtEpochMs))
    }
}

private class FakeDossierService : AgronomicDossierService {
    var sealResponse: () -> Response<AgronomicDossierDto> = { Response.success(dossierDto()) }
    var listResponse: () -> Response<List<AgronomicDossierDto>> = { Response.success(emptyList()) }
    var pdfBytes: ByteArray = PDF
    var verifyResponse: () -> Response<DossierVerificationDto> = { Response.success(verificationDto()) }

    override suspend fun seal(plotId: String, request: SealDossierRequestDto) = sealResponse()
    override suspend fun getDossiers(plotId: String) = listResponse()
    override suspend fun downloadPdf(plotId: String, campaignYear: Int): Response<ResponseBody> =
        Response.success(pdfBytes.toResponseBody("application/pdf".toMediaType()))
    override suspend fun verify(verificationHash: String) = verifyResponse()
}

private val PDF = "%PDF-1.4 expediente".toByteArray()
private val PDF_HASH = MessageDigest.getInstance("SHA-256").digest(PDF).joinToString("") { "%02x".format(it) }

private fun dossierDto(year: Int = 2026, hash: String = PDF_HASH) = AgronomicDossierDto(
    id = "d-$year",
    plotId = "p1",
    holderName = "Teodoro Mamani",
    campaignYear = year,
    plotName = "La Yarada 02",
    variety = "SEVILLANA",
    areaHectares = 2.5,
    thinning = DossierThinningDto(actualRemovalPercentage = 30.0, executedOn = "2026-11-14", compliance = "EXECUTED_ON_TIME"),
    harvest = DossierHarvestDto(totalKg = 20_800.0, receiptNumber = "VR-26-0142"),
    verificationHash = hash,
    sealedAt = "2026-05-14T15:40:00Z",
    pageCount = 2,
    sizeBytes = 4_812,
    fileName = "Expediente_LaYarada02_$year.pdf",
)

private fun verificationDto() = DossierVerificationDto(
    verificationHash = PDF_HASH,
    plotName = "La Yarada 02",
    areaHectares = 2.5,
    holderName = "Teodoro Mamani",
    sealedAt = "2026-05-14T15:40:00Z",
    pageCount = 2,
    historyFromYear = 2021,
    historyToYear = 2026,
)

private fun <T> notFound(): Response<T> =
    Response.error(404, "{\"status\":404}".toResponseBody("application/problem+json".toMediaType()))

@OptIn(ExperimentalCoroutinesApi::class)
class AgronomicDossierRepositoryImplTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val dao = FakeDossierDao()
    private val service = FakeDossierService()
    private val clock = Clock.fixed(Instant.parse("2026-10-18T12:00:00Z"), ZoneOffset.UTC)

    private fun repository() = AgronomicDossierRepositoryImpl(
        service = service,
        dao = dao,
        apiCaller = ApiCaller(ApiErrorMapper(Json), UnconfinedTestDispatcher()),
        clock = clock,
        directory = folder.root,
        dispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun `sealing caches the dossier with its values`() = runTest {
        val repository = repository()

        assertEquals(AppResult.Success(Unit), repository.seal("p1", 2026))

        val cached = repository.observeDossier("p1", 2026).first()!!
        assertEquals("Teodoro Mamani", cached.dossier.holderName)
        assertEquals(20_800.0, cached.dossier.harvest?.totalKg)
        assertTrue(cached.dossier.thinning!!.recorded)
        assertNull(cached.pdfPath)
    }

    @Test
    fun `sealing again keeps the pdf already downloaded`() = runTest {
        val repository = repository()
        repository.seal("p1", 2026)
        repository.downloadPdf("p1", 2026)

        repository.seal("p1", 2026)

        assertTrue(repository.observeDossier("p1", 2026).first()!!.pdfPath!!.endsWith("Expediente_LaYarada02_2026.pdf"))
    }

    @Test
    fun `a downloaded pdf with the sealed code is kept and remembered`() = runTest {
        val repository = repository()
        repository.seal("p1", 2026)

        val file = (repository.downloadPdf("p1", 2026) as AppResult.Success).value

        assertTrue(file.readBytes().contentEquals(PDF))
        val cached = repository.observeDossier("p1", 2026).first()!!
        assertEquals(file.absolutePath, cached.pdfPath)
        assertEquals(clock.instant(), cached.pdfSavedAt)
    }

    @Test
    fun `a downloaded pdf that does not match its seal is discarded`() = runTest {
        val repository = repository()
        repository.seal("p1", 2026)
        service.pdfBytes = "%PDF-1.4 cambiado".toByteArray()

        val result = repository.downloadPdf("p1", 2026)

        assertTrue(result is AppResult.Failure)
        assertNull(repository.observeDossier("p1", 2026).first()!!.pdfPath)
        assertFalse(folder.root.listFiles()!!.any { it.length() > 0 })
    }

    @Test
    fun `the newest sealed campaign is the highest year listed and every dossier is cached`() = runTest {
        service.listResponse = { Response.success(listOf(dossierDto(2026), dossierDto(2025))) }
        val repository = repository()

        assertEquals(AppResult.Success(2026), repository.latestSealedCampaign("p1"))
        assertTrue(repository.observeDossier("p1", 2025).first() != null)
    }

    @Test
    fun `a plot without dossiers has no sealed campaign`() = runTest {
        assertEquals(AppResult.Success(null), repository().latestSealedCampaign("p1"))
    }

    @Test
    fun `a code Viora does not store answers not found`() = runTest {
        service.verifyResponse = { notFound() }

        val result = repository().verify(PDF_HASH)

        assertTrue((result as AppResult.Failure).error is AppError.NotFound)
    }

    @Test
    fun `a matching code returns the record's data`() = runTest {
        val verification = (repository().verify(PDF_HASH) as AppResult.Success).value

        assertEquals("Teodoro Mamani", verification.holderName)
        assertEquals(2021, verification.historyFromYear)
    }
}
