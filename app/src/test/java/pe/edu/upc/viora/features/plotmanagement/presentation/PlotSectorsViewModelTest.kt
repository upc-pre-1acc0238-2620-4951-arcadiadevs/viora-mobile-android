package pe.edu.upc.viora.features.plotmanagement.presentation

import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.domain.OliveVariety
import pe.edu.upc.viora.core.domain.PlotId
import pe.edu.upc.viora.features.plotmanagement.application.usecase.AssignPlotSectorUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.GetLicenseSectorsUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.GetPlotsWithoutSectorUseCase
import pe.edu.upc.viora.features.plotmanagement.application.usecase.MarkSectorPromptAnsweredUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotChanges
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotSector
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotSectorStatus
import pe.edu.upc.viora.features.plotmanagement.domain.entity.SectorChoice
import pe.edu.upc.viora.features.plotmanagement.domain.entity.SectorZone
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotSectorRepository
import pe.edu.upc.viora.features.plotmanagement.domain.repository.SectorPromptRepository
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.plotMark
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.PlotSectorsViewModel
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.SectorPromptViewModel
import pe.edu.upc.viora.features.subscription.application.usecase.GetLatestDataSharingConsentUseCase
import pe.edu.upc.viora.features.subscription.domain.entity.DataSharingConsent
import pe.edu.upc.viora.features.subscription.domain.repository.DataSharingConsentRepository

private fun plot(id: String, name: String) = Plot(
    id = PlotId(id), name = name, variety = OliveVariety.SEVILLANA, areaHectares = 2.5, treesPerHectare = 204,
    rowSpacingMeters = 7.0, treeSpacingMeters = 7.0, outline = emptyList(), lastPruningDate = null, isActive = true, revision = 0,
)

private class Plots(list: List<Plot>) : PlotRepository {
    val plots = MutableStateFlow(list)
    override fun observePlots(): Flow<List<Plot>> = plots
    override fun observePlot(id: PlotId): Flow<Plot?> = MutableStateFlow(null)
    override fun observeArchivedPlots(): Flow<List<Plot>> = MutableStateFlow(emptyList())
    override fun observeLastRefresh(): Flow<Instant?> = MutableStateFlow(null)
    override suspend fun refresh(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun refreshArchived(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun register(newPlot: NewPlot): AppResult<Plot> = AppResult.Failure(AppError.Offline)
    override suspend fun update(id: PlotId, changes: PlotChanges): AppResult<Plot> = AppResult.Failure(AppError.Offline)
    override suspend fun archive(id: PlotId): AppResult<Unit> = AppResult.Failure(AppError.Offline)
    override suspend fun restore(id: PlotId): AppResult<Plot> = AppResult.Failure(AppError.Offline)
}

private class Sectors : PlotSectorRepository {
    var placed: AppResult<List<PlotSector>> = AppResult.Success(emptyList())
    val failFor = mutableSetOf<String>()
    val assigned = mutableListOf<String>()

    override suspend fun sectors(): AppResult<List<SectorZone>> = AppResult.Success(listOf(SectorZone("s1", "Los Palos")))
    override suspend fun plotSectors(): AppResult<List<PlotSector>> = placed
    override suspend fun assign(plotId: String, choice: SectorChoice): AppResult<PlotSector> {
        if (plotId in failFor) return AppResult.Failure(AppError.Offline)
        assigned += plotId
        return AppResult.Success(PlotSector(plotId, PlotSectorStatus.ASSIGNED, "Los Palos", null))
    }
}

private class Prompt : SectorPromptRepository {
    override val answered = MutableStateFlow(false)
    override suspend fun markAnswered() {
        answered.value = true
    }
}

private object NoConsent : DataSharingConsentRepository {
    override suspend fun latest(): AppResult<DataSharingConsent?> = AppResult.Success(null)
    override suspend fun withdraw(consentId: String): AppResult<DataSharingConsent> = AppResult.Failure(AppError.Offline)
}

@OptIn(ExperimentalCoroutinesApi::class)
class PlotSectorsViewModelTest {

    private val plots = Plots(listOf(plot("p1", "La Yarada 02"), plot("p2", "Lote Norte")))
    private val sectors = Sectors()
    private val prompt = Prompt()
    private val withoutSector = GetPlotsWithoutSectorUseCase(plots, sectors, prompt)
    private val palos = SectorChoice.Listed(SectorZone("s1", "Los Palos"))

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun screen() = PlotSectorsViewModel(
        plotsWithoutSector = withoutSector,
        licenseSectors = GetLicenseSectorsUseCase(sectors),
        latestConsent = GetLatestDataSharingConsentUseCase(NoConsent),
        assignSector = AssignPlotSectorUseCase(sectors),
        markAnswered = MarkSectorPromptAnsweredUseCase(prompt),
    )

    @Test
    fun `the Home asks once a member whose plots have no sector`() {
        sectors.placed = AppResult.Success(listOf(PlotSector("p1", PlotSectorStatus.PENDING, null, "Cerro Blanco")))
        assertTrue(SectorPromptViewModel(withoutSector).ask.value)

        prompt.answered.value = true
        assertFalse(SectorPromptViewModel(withoutSector).ask.value)
    }

    @Test
    fun `the Home does not ask an individual producer or a member whose plots all have a sector`() {
        sectors.placed = AppResult.Failure(AppError.Forbidden("territory.producer.not_sponsored"))
        assertFalse(SectorPromptViewModel(withoutSector).ask.value)

        sectors.placed = AppResult.Success(
            listOf(PlotSector("p1", PlotSectorStatus.ASSIGNED, "Los Palos", null), PlotSector("p2", PlotSectorStatus.PENDING, null, "X")),
        )
        assertFalse(SectorPromptViewModel(withoutSector).ask.value)
    }

    @Test
    fun `saving sends each answer and is not asked again`() {
        val vm = screen()
        assertEquals(listOf("p1", "p2"), vm.uiState.value.plots.map { it.id.value })
        assertFalse(vm.uiState.value.canSave)

        vm.choose("p1", palos)
        vm.save()

        assertEquals(listOf("p1"), sectors.assigned)
        assertTrue(vm.uiState.value.done)
        assertTrue(prompt.answered.value)
    }

    @Test
    fun `a sector that does not get through stays to try again`() {
        sectors.failFor += "p2"
        val vm = screen()
        vm.choose("p1", palos)
        vm.choose("p2", SectorChoice.Written("Cerro Blanco"))

        vm.save()

        assertTrue(vm.uiState.value.saveFailed)
        assertFalse(vm.uiState.value.done)
        assertEquals(listOf("p2"), vm.uiState.value.plots.map { it.id.value })
        assertEquals(setOf("p2"), vm.uiState.value.choices.keys)
        assertFalse(prompt.answered.value)
    }

    @Test
    fun `later sends nothing and is not asked again`() {
        val vm = screen()
        vm.later()

        assertTrue(sectors.assigned.isEmpty())
        assertTrue(vm.uiState.value.done)
        assertTrue(prompt.answered.value)
    }

    @Test
    fun `a written sector outside 2 to 60 letters is refused before sending`() {
        val vm = screen()
        vm.choose("p1", SectorChoice.Written(" a "))
        vm.save()

        assertTrue(sectors.assigned.isEmpty())
        assertTrue(vm.uiState.value.saveFailed)
    }

    @Test
    fun `the mark of a plot is its number or its initials`() {
        assertEquals("02", plotMark("La Yarada 02"))
        assertEquals("LN", plotMark("Lote Norte"))
        assertEquals("P", plotMark("Parcela"))
    }
}
