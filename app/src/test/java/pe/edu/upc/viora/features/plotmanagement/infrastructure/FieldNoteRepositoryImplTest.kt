package pe.edu.upc.viora.features.plotmanagement.infrastructure

import java.io.File
import java.nio.file.Files
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.core.network.ApiErrorMapper
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNoteSyncResult
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNoteSyncState
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNoteType
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewFieldNote
import pe.edu.upc.viora.features.plotmanagement.domain.service.FieldNoteSyncScheduler
import pe.edu.upc.viora.features.plotmanagement.domain.service.NotePhotoStore
import pe.edu.upc.viora.features.plotmanagement.infrastructure.local.FieldNoteDao
import pe.edu.upc.viora.features.plotmanagement.infrastructure.local.FieldNoteEntity
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.FieldNoteService
import pe.edu.upc.viora.features.plotmanagement.infrastructure.repository.FieldNoteRepositoryImpl
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private class NotesDaoFake : FieldNoteDao {
    override suspend fun wipeAll() = Unit

    val rows = MutableStateFlow<Map<String, FieldNoteEntity>>(emptyMap())

    override fun observeAll(): Flow<List<FieldNoteEntity>> = MutableStateFlow(rows.value.values.toList())
    override suspend fun findUnsent() = rows.value.values.filter { it.syncState == "PENDING" || it.photoPending }
    override suspend fun find(id: String) = rows.value[id]
    override suspend fun upsert(entity: FieldNoteEntity) {
        rows.value = rows.value + (entity.id to entity)
    }
    override suspend fun deleteSyncedOfPlot(plotId: String) {
        rows.value = rows.value.filterValues { !(it.plotId == plotId && it.syncState == "SYNCED" && !it.photoPending) }
    }
    override suspend fun upsertAll(entities: List<FieldNoteEntity>) = entities.forEach { upsert(it) }
}

private class PhotoStoreFake : NotePhotoStore {
    val kept = mutableMapOf<String, ByteArray>()

    override suspend fun keep(uri: String): String? = null
    override suspend fun discard(path: String) = Unit
    override suspend fun downloaded(noteId: String): String? = "cache/$noteId.jpg".takeIf { noteId in kept }
    override suspend fun keepDownloaded(noteId: String, bytes: ByteArray): String {
        kept[noteId] = bytes
        return "cache/$noteId.jpg"
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class FieldNoteRepositoryImplTest {
    private val server = MockWebServer()
    private val dao = NotesDaoFake()
    private var scheduled = 0
    private val photos = PhotoStoreFake()
    private val json = Json { ignoreUnknownKeys = true }
    private val now = Instant.parse("2026-11-18T15:00:00Z")
    private val today = LocalDate.of(2026, 11, 18)
    private lateinit var photoDir: File

    @Before fun setUp() {
        server.start()
        photoDir = Files.createTempDirectory("notes").toFile()
    }

    @After fun tearDown() {
        server.shutdown()
        photoDir.deleteRecursively()
    }

    private fun repository(): FieldNoteRepositoryImpl {
        val service = Retrofit.Builder().baseUrl(server.url("/api/v1/"))
            .client(OkHttpClient.Builder().readTimeout(500, TimeUnit.MILLISECONDS).build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType())).build()
            .create(FieldNoteService::class.java)
        val dispatcher = UnconfinedTestDispatcher()
        return FieldNoteRepositoryImpl(
            service = service,
            dao = dao,
            apiCaller = ApiCaller(ApiErrorMapper(json), dispatcher),
            scheduler = object : FieldNoteSyncScheduler {
                override fun schedule() {
                    scheduled++
                }
            },
            photoStore = photos,
            clock = Clock.fixed(now, ZoneOffset.UTC),
            ioDispatcher = dispatcher,
        )
    }

    private fun noteJson(id: String, hasPhoto: Boolean = false) =
        """{"id":"$id","plotId":"plot-1","type":"IRRIGATION","text":"Riego a la tarde.","observedOn":"2026-11-18",
            "recordedAt":"2026-11-18T15:00:00Z","hasPhoto":$hasPhoto}"""

    @Test fun `writing keeps the note on the phone and asks for a sync without touching the network`() = runTest {
        val result = repository().write(NewFieldNote("plot-1", FieldNoteType.IRRIGATION, "Riego a la tarde.", today, null))
        val note = (result as AppResult.Success).value
        assertEquals(FieldNoteSyncState.PENDING, note.syncState)
        assertEquals(1, scheduled)
        assertEquals(0, server.requestCount)
        assertEquals("PENDING", dao.rows.value.getValue(note.id).syncState)
    }

    @Test fun `sync sends the note with its own id, then the photo, and forgets the local file`() = runTest {
        val photo = File(photoDir, "a.jpg").apply { writeBytes(byteArrayOf(-1, -40, -1, 1)) }
        val repo = repository()
        val note = (repo.write(NewFieldNote("plot-1", FieldNoteType.PESTS, "Mosca.", today, photo.path)) as AppResult.Success).value
        server.enqueue(MockResponse().setResponseCode(201).setBody(noteJson(note.id)))
        server.enqueue(MockResponse().setResponseCode(200).setBody(noteJson(note.id, hasPhoto = true)))

        assertEquals(FieldNoteSyncResult.DONE, repo.syncPending())

        val post = server.takeRequest()
        assertEquals("/api/v1/plots/plot-1/field-notes", post.path)
        assertTrue(post.body.readUtf8().contains("\"id\":\"${note.id}\""))
        val put = server.takeRequest()
        assertEquals("PUT", put.method)
        assertEquals("/api/v1/plots/plot-1/field-notes/${note.id}/photo", put.path)
        assertEquals("image/jpeg", put.getHeader("Content-Type"))
        val row = dao.rows.value.getValue(note.id)
        assertEquals("SYNCED", row.syncState)
        assertFalse(row.photoPending)
        assertNull(row.photoPath)
        assertFalse(photo.exists())
    }

    @Test fun `no signal keeps the note pending for a later pass`() = runTest {
        val repo = repository()
        val note = (repo.write(NewFieldNote("plot-1", FieldNoteType.OTHER, "Algo.", today, null)) as AppResult.Success).value
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST))

        assertEquals(FieldNoteSyncResult.RETRY_LATER, repo.syncPending())
        assertEquals("PENDING", dao.rows.value.getValue(note.id).syncState)
    }

    @Test fun `a note the server refuses stays on the phone as failed and is not retried`() = runTest {
        val repo = repository()
        val note = (repo.write(NewFieldNote("plot-1", FieldNoteType.OTHER, "Algo.", today, null)) as AppResult.Success).value
        server.enqueue(MockResponse().setResponseCode(404).setBody("""{"status":404,"title":"Not Found"}"""))

        assertEquals(FieldNoteSyncResult.DONE, repo.syncPending())
        assertEquals("FAILED", dao.rows.value.getValue(note.id).syncState)
        assertTrue(dao.findUnsent().isEmpty())
    }

    @Test fun `refresh replaces the server notes of a plot and keeps the ones still waiting`() = runTest {
        val repo = repository()
        val waiting = (repo.write(NewFieldNote("plot-1", FieldNoteType.OTHER, "Sin señal.", today, null)) as AppResult.Success).value
        dao.upsert(dao.rows.value.getValue(waiting.id).copy(id = "old", syncState = "SYNCED"))
        server.enqueue(MockResponse().setBody("[${noteJson("server-1")}]"))

        assertTrue(repo.refresh(listOf("plot-1")) is AppResult.Success)

        assertEquals(setOf(waiting.id, "server-1"), dao.rows.value.keys)
        assertEquals("SYNCED", dao.rows.value.getValue("server-1").syncState)
        assertEquals(2, repo.observeNotes().first().size)
    }

    @Test fun `a photo on the server is downloaded once and then read from the phone`() = runTest {
        val repo = repository()
        server.enqueue(MockResponse().setBody(Buffer().write(byteArrayOf(1, 2, 3))).setHeader("Content-Type", "image/jpeg"))

        val first = repo.photoPath("plot-1", "note-1")
        val second = repo.photoPath("plot-1", "note-1")

        assertEquals("cache/note-1.jpg", (first as AppResult.Success).value)
        assertEquals("cache/note-1.jpg", (second as AppResult.Success).value)
        assertEquals(1, server.requestCount)
        assertEquals("/api/v1/plots/plot-1/field-notes/note-1/photo", server.takeRequest().path)
        assertEquals(listOf<Byte>(1, 2, 3), photos.kept.getValue("note-1").toList())
    }

    @Test fun `a photo the server does not have answers not found and keeps nothing`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        val result = repository().photoPath("plot-1", "note-1")

        assertTrue((result as AppResult.Failure).error is AppError.NotFound)
        assertTrue(photos.kept.isEmpty())
    }

    @Test fun `without signal the photo fails instead of crashing the sheet`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        assertTrue(repository().photoPath("plot-1", "note-1") is AppResult.Failure)
    }

    @Test fun `a note waiting on the phone exposes the copy of its photo`() = runTest {
        val repo = repository()
        val note = (repo.write(NewFieldNote("plot-1", FieldNoteType.OTHER, "Con foto.", today, "files/a.jpg")) as AppResult.Success).value

        assertEquals("files/a.jpg", note.localPhotoPath)
    }
}
