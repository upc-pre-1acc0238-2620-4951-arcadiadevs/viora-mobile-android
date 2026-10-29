package pe.edu.upc.viora.features.harvestsettlement.infrastructure.repository

import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.time.Clock
import javax.inject.Inject
import javax.inject.Named
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import pe.edu.upc.viora.core.di.IoDispatcher
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.CachedDossier
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.DossierVerification
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.AgronomicDossierRepository
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.AgronomicDossierDao
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper.toCached
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper.toDomain
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.mapper.toEntity
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.AgronomicDossierDto
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.AgronomicDossierService
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.SealDossierRequestDto
import pe.edu.upc.viora.core.domain.appResultOf

class AgronomicDossierRepositoryImpl @Inject constructor(
    private val service: AgronomicDossierService,
    private val dao: AgronomicDossierDao,
    private val apiCaller: ApiCaller,
    private val clock: Clock,
    @Named(DOSSIER_DIR) private val directory: File,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : AgronomicDossierRepository {

    override fun observeDossier(plotId: String, campaignYear: Int): Flow<CachedDossier?> =
        dao.observe(plotId, campaignYear).map { it?.toCached() }

    override suspend fun seal(plotId: String, campaignYear: Int): AppResult<Unit> =
        when (val result = apiCaller.call { service.seal(plotId, SealDossierRequestDto(campaignYear)) }) {
            is AppResult.Success -> appResultOf<Unit> { save(result.value) }
            is AppResult.Failure -> result
        }

    override suspend fun latestSealedCampaign(plotId: String): AppResult<Int?> =
        when (val result = apiCaller.call { service.getDossiers(plotId) }) {
            is AppResult.Failure -> result
            is AppResult.Success -> {
                val saved = appResultOf<Unit> { result.value.forEach { save(it) } }
                if (saved is AppResult.Failure) saved else AppResult.Success(result.value.maxOfOrNull { it.campaignYear })
            }
        }

    override suspend fun downloadPdf(plotId: String, campaignYear: Int): AppResult<File> {
        val cached = dao.find(plotId, campaignYear)?.toCached() ?: return AppResult.Failure(AppError.NotFound())
        val body = when (val result = apiCaller.call { service.downloadPdf(plotId, campaignYear) }) {
            is AppResult.Success -> result.value
            is AppResult.Failure -> return result
        }
        return try {
            val file = withContext(dispatcher) {
                write(body, File(directory, safeFileName(cached.dossier.fileName)), cached.dossier.verificationHash)
            }
            dao.updatePdf(plotId, campaignYear, file.absolutePath, clock.millis())
            AppResult.Success(file)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            AppResult.Failure(AppError.Unknown(throwable))
        }
    }

    override suspend fun verify(verificationHash: String): AppResult<DossierVerification> =
        when (val result = apiCaller.call { service.verify(verificationHash) }) {
            is AppResult.Success -> AppResult.Success(result.value.toDomain())
            is AppResult.Failure -> result
        }

    /** Writes the dossier keeping the PDF already downloaded for it: a sealed dossier never changes. */
    private suspend fun save(remote: AgronomicDossierDto) {
        val current = dao.find(remote.plotId, remote.campaignYear)
        dao.upsert(remote.toEntity(current?.pdfPath, current?.pdfSavedAtEpochMs))
    }

    /**
     * Streams the PDF into [target] through a temporary file and keeps it only when its SHA-256 is the sealed one,
     * so the phone never shows as "Íntegro" a file that was cut or changed on the way.
     */
    private fun write(body: ResponseBody, target: File, expectedHash: String): File {
        target.parentFile?.mkdirs()
        val partial = File(target.parentFile, target.name + ".part")
        val digest = MessageDigest.getInstance("SHA-256")
        body.use { response ->
            response.byteStream().use { input ->
                partial.outputStream().use { output ->
                    val buffer = ByteArray(BUFFER_SIZE)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        digest.update(buffer, 0, read)
                        output.write(buffer, 0, read)
                    }
                }
            }
        }
        val hash = digest.digest().joinToString("") { "%02x".format(it) }
        if (hash != expectedHash) {
            partial.delete()
            throw IOException("The downloaded PDF does not match its seal")
        }
        if (target.exists()) target.delete()
        if (!partial.renameTo(target)) throw IOException("Could not keep the downloaded PDF")
        return target
    }

    companion object {
        /** Folder inside the app's files where the sealed PDFs live; shared through the FileProvider (file_paths.xml). */
        const val DOSSIER_DIR = "dossiers"
        private const val BUFFER_SIZE = 8 * 1024
    }
}

/**
 * The file name the server sent, reduced to plain characters: a name with "../" or a folder could otherwise write
 * outside the dossiers folder.
 */
internal fun safeFileName(name: String): String =
    name.substringAfterLast('/').substringAfterLast('\\').replace(Regex("[^A-Za-z0-9._-]"), "_")
        .trimStart('.').ifBlank { "dossier.pdf" }
