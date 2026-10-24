package pe.edu.upc.viora.features.plotmanagement.infrastructure.repository

import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import pe.edu.upc.viora.core.di.IoDispatcher
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNote
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNoteSyncResult
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNoteSyncState
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNoteType
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewFieldNote
import pe.edu.upc.viora.features.plotmanagement.domain.repository.FieldNoteRepository
import pe.edu.upc.viora.features.plotmanagement.domain.service.FieldNoteSyncScheduler
import pe.edu.upc.viora.features.plotmanagement.infrastructure.local.FieldNoteDao
import pe.edu.upc.viora.features.plotmanagement.infrastructure.local.FieldNoteEntity
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.FieldNoteDto
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.FieldNoteService
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.RecordFieldNoteRequestDto

/**
 * Offline-first logbook notes. A note is saved in Room with a UUID made here, so sending it again
 * after a lost answer never duplicates it on the server; the photo travels in a second request once
 * the note exists. Rejected notes (invalid, plot gone) stay on the phone marked as FAILED.
 */
@Singleton
class FieldNoteRepositoryImpl @Inject constructor(
    private val service: FieldNoteService,
    private val dao: FieldNoteDao,
    private val apiCaller: ApiCaller,
    private val scheduler: FieldNoteSyncScheduler,
    private val clock: Clock,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : FieldNoteRepository {

    override fun observeNotes(): Flow<List<FieldNote>> = dao.observeAll().map { rows -> rows.mapNotNull { it.toDomain() } }

    override suspend fun write(note: NewFieldNote): AppResult<FieldNote> {
        val entity = FieldNoteEntity(
            id = UUID.randomUUID().toString(),
            plotId = note.plotId,
            type = note.type.name,
            text = note.text,
            observedOn = note.observedOn.toString(),
            createdAt = clock.millis(),
            hasPhoto = note.photoPath != null,
            photoPath = note.photoPath,
            photoPending = note.photoPath != null,
            syncState = FieldNoteSyncState.PENDING.name,
            lastErrorCode = null,
        )
        dao.upsert(entity)
        scheduler.schedule()
        return AppResult.Success(requireNotNull(entity.toDomain()))
    }

    override suspend fun refresh(plotIds: List<String>): AppResult<Unit> {
        var failure: AppResult.Failure? = null
        for (plotId in plotIds) {
            when (val result = apiCaller.call { service.getNotes(plotId) }) {
                is AppResult.Success -> dao.replaceSyncedOfPlot(plotId, result.value.map { it.toEntity() })
                is AppResult.Failure -> if (failure == null) failure = result
            }
        }
        return failure ?: AppResult.Success(Unit)
    }

    override suspend fun syncPending(): FieldNoteSyncResult {
        val rows = try {
            dao.findUnsent()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Throwable) {
            return FieldNoteSyncResult.RETRY_LATER
        }
        var retryLater = false
        for (row in rows) {
            val sent = try {
                syncRow(row)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Throwable) {
                false
            }
            retryLater = retryLater || !sent
        }
        return if (retryLater) FieldNoteSyncResult.RETRY_LATER else FieldNoteSyncResult.DONE
    }

    /** Sends the note, then its photo; returns false when something must be retried later. */
    private suspend fun syncRow(row: FieldNoteEntity): Boolean {
        var current = row
        if (current.syncState == FieldNoteSyncState.PENDING.name) {
            val request = RecordFieldNoteRequestDto(current.id, current.type, current.text, current.observedOn)
            when (val result = apiCaller.call { service.record(current.plotId, request) }) {
                is AppResult.Success -> {
                    current = current.copy(syncState = FieldNoteSyncState.SYNCED.name, lastErrorCode = null)
                    dao.upsert(current)
                }
                is AppResult.Failure -> {
                    val permanent = permanentCodeOf(result.error) ?: return false
                    dao.upsert(current.copy(syncState = FieldNoteSyncState.FAILED.name, lastErrorCode = permanent))
                    return true
                }
            }
        }
        if (current.photoPending && current.syncState == FieldNoteSyncState.SYNCED.name) {
            return sendPhoto(current)
        }
        return true
    }

    private suspend fun sendPhoto(row: FieldNoteEntity): Boolean {
        val file = row.photoPath?.let(::File)?.takeIf { it.exists() }
        if (file == null) {
            // The photo was lost on the phone: keep the note without it.
            dao.upsert(row.copy(photoPending = false, hasPhoto = false, photoPath = null))
            return true
        }
        val body = file.asRequestBody(JPEG)
        return when (val result = apiCaller.call { service.attachPhoto(row.plotId, row.id, body) }) {
            is AppResult.Success -> {
                dao.upsert(row.copy(photoPending = false, photoPath = null))
                withContext(ioDispatcher) { file.delete() }
                true
            }
            is AppResult.Failure -> {
                if (permanentCodeOf(result.error) == null) return false
                // The server refused the image: the note stays, without the photo.
                dao.upsert(row.copy(photoPending = false, hasPhoto = false, photoPath = null))
                withContext(ioDispatcher) { file.delete() }
                true
            }
        }
    }

    /** 400 (rejected data), 403 (not the owner), 404 (plot gone) and 409 (id taken) will not succeed on retry. */
    private fun permanentCodeOf(error: AppError): String? = when (error) {
        is AppError.Validation -> error.code ?: "VALIDATION_ERROR"
        is AppError.Forbidden -> "FORBIDDEN"
        is AppError.NotFound -> error.code ?: "NOT_FOUND"
        is AppError.Conflict -> error.code ?: "CONFLICT"
        else -> null
    }

    private fun FieldNoteEntity.toDomain(): FieldNote? = runCatching {
        FieldNote(
            id = id,
            plotId = plotId,
            type = FieldNoteType.valueOf(type),
            text = text,
            observedOn = LocalDate.parse(observedOn),
            createdAt = Instant.ofEpochMilli(createdAt),
            hasPhoto = hasPhoto,
            syncState = FieldNoteSyncState.valueOf(syncState),
        )
    }.getOrNull()

    private fun FieldNoteDto.toEntity(): FieldNoteEntity = FieldNoteEntity(
        id = id,
        plotId = plotId,
        type = runCatching { FieldNoteType.valueOf(type) }.getOrDefault(FieldNoteType.OTHER).name,
        text = text,
        observedOn = observedOn,
        createdAt = runCatching { Instant.parse(recordedAt).toEpochMilli() }.getOrDefault(clock.millis()),
        hasPhoto = hasPhoto,
        photoPath = null,
        photoPending = false,
        syncState = FieldNoteSyncState.SYNCED.name,
        lastErrorCode = null,
    )

    companion object {
        /** Folder under `filesDir` where the photos wait to be uploaded. */
        const val PHOTO_DIR = "field-notes"
        private val JPEG = "image/jpeg".toMediaType()
    }
}
