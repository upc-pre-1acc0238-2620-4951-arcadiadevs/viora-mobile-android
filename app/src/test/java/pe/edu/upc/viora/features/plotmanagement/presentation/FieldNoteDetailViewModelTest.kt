package pe.edu.upc.viora.features.plotmanagement.presentation

import java.lang.reflect.Proxy
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.plotmanagement.application.usecase.GetFieldNotePhotoUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObserveFieldNotesUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotsUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNote
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNoteSyncResult
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNoteSyncState
import pe.edu.upc.viora.features.plotmanagement.domain.entity.FieldNoteType
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewFieldNote
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.repository.FieldNoteRepository
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.plotmanagement.presentation.state.NotePhotoUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.FieldNoteDetailViewModel

private class NotesFake : FieldNoteRepository {
    val notes = MutableStateFlow<List<FieldNote>>(emptyList())
    var photoResult: AppResult<String> = AppResult.Success("cache/n1.jpg")
    var downloads = 0

    override fun observeNotes(): Flow<List<FieldNote>> = notes
    override suspend fun write(note: NewFieldNote): AppResult<FieldNote> = error("not used")
    override suspend fun refresh(plotIds: List<String>): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun syncPending(): FieldNoteSyncResult = FieldNoteSyncResult.DONE
    override suspend fun photoPath(plotId: String, noteId: String): AppResult<String> {
        downloads++
        return photoResult
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class FieldNoteDetailViewModelTest {
    private val repository = NotesFake()

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After fun tearDown() = Dispatchers.resetMain()

    private fun note(hasPhoto: Boolean = false, localPhotoPath: String? = null, id: String = "n1") = FieldNote(
        id = id,
        plotId = "plot-1",
        type = FieldNoteType.IRRIGATION,
        text = "Riego a la tarde por el calor y revisar goteros de la fila 4.",
        observedOn = LocalDate.of(2026, 11, 18),
        createdAt = Instant.parse("2026-11-18T15:00:00Z"),
        hasPhoto = hasPhoto,
        syncState = FieldNoteSyncState.SYNCED,
        localPhotoPath = localPhotoPath,
    )

    private fun CoroutineScope.viewModel(): FieldNoteDetailViewModel {
        // The name of the plot is covered by the screen; here only the empty list of plots is needed.
        val plots = Proxy.newProxyInstance(
            PlotRepository::class.java.classLoader,
            arrayOf(PlotRepository::class.java),
        ) { _, method, _ ->
            if (method.name == "observePlots") MutableStateFlow(emptyList<Plot>()) else error("not used: ${method.name}")
        } as PlotRepository
        return FieldNoteDetailViewModel(ObserveFieldNotesUseCase(repository), ObservePlotsUseCase(plots), GetFieldNotePhotoUseCase(repository))
            .also { it.uiState.launchIn(this) }
    }

    @Test fun `it shows only the note it was opened with`() = runTest(UnconfinedTestDispatcher()) {
        repository.notes.value = listOf(note(id = "other"), note())
        val viewModel = backgroundScope.viewModel()

        assertNull(viewModel.uiState.value.note)
        viewModel.open("n1")

        assertEquals("n1", viewModel.uiState.value.note?.id)
    }

    @Test fun `a note without photo downloads nothing`() = runTest(UnconfinedTestDispatcher()) {
        repository.notes.value = listOf(note())
        val viewModel = backgroundScope.viewModel()
        viewModel.open("n1")

        assertEquals(NotePhotoUiState.None, viewModel.uiState.value.photo)
        assertEquals(0, repository.downloads)
    }

    @Test fun `a photo still on the phone is shown without the network`() = runTest(UnconfinedTestDispatcher()) {
        repository.notes.value = listOf(note(hasPhoto = true, localPhotoPath = "files/a.jpg"))
        val viewModel = backgroundScope.viewModel()
        viewModel.open("n1")

        assertEquals(NotePhotoUiState.Ready("files/a.jpg"), viewModel.uiState.value.photo)
        assertEquals(0, repository.downloads)
    }

    @Test fun `a photo already on the server is downloaded`() = runTest(UnconfinedTestDispatcher()) {
        repository.notes.value = listOf(note(hasPhoto = true))
        val viewModel = backgroundScope.viewModel()
        viewModel.open("n1")

        assertEquals(NotePhotoUiState.Ready("cache/n1.jpg"), viewModel.uiState.value.photo)
        assertEquals(1, repository.downloads)
    }

    @Test fun `without signal the note still reads and the photo can be retried`() = runTest(UnconfinedTestDispatcher()) {
        repository.notes.value = listOf(note(hasPhoto = true))
        repository.photoResult = AppResult.Failure(AppError.Offline)
        val viewModel = backgroundScope.viewModel()
        viewModel.open("n1")

        assertEquals("n1", viewModel.uiState.value.note?.id)
        assertEquals(NotePhotoUiState.Unavailable, viewModel.uiState.value.photo)

        repository.photoResult = AppResult.Success("cache/n1.jpg")
        viewModel.retryPhoto()

        assertEquals(NotePhotoUiState.Ready("cache/n1.jpg"), viewModel.uiState.value.photo)
    }

    @Test fun `a photo the server no longer has disappears instead of asking to retry`() = runTest(UnconfinedTestDispatcher()) {
        repository.notes.value = listOf(note(hasPhoto = true))
        repository.photoResult = AppResult.Failure(AppError.NotFound())
        val viewModel = backgroundScope.viewModel()
        viewModel.open("n1")

        assertEquals(NotePhotoUiState.None, viewModel.uiState.value.photo)
    }

    @Test fun `a note synced while open asks for its photo`() = runTest(UnconfinedTestDispatcher()) {
        repository.notes.value = listOf(note(hasPhoto = true, localPhotoPath = "files/a.jpg"))
        val viewModel = backgroundScope.viewModel()
        viewModel.open("n1")

        repository.notes.value = listOf(note(hasPhoto = true))

        assertEquals(NotePhotoUiState.Ready("cache/n1.jpg"), viewModel.uiState.value.photo)
        assertEquals(1, repository.downloads)
    }
}
