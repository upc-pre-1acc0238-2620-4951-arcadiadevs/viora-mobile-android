package pe.edu.upc.viora.features.croploadregulation.presentation

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.croploadregulation.application.usecase.GetPlotSamplingOverviewUseCase
import pe.edu.upc.viora.features.croploadregulation.domain.entity.PlotSamplingOverview
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningRepository
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.SamplingStatus
import pe.edu.upc.viora.features.croploadregulation.presentation.state.SelectPlotSamplingUiState
import pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel.SelectPlotSamplingViewModel
import pe.edu.upc.viora.testing.unused

@OptIn(ExperimentalCoroutinesApi::class)
class SelectPlotSamplingViewModelTest {

    /** A day of the 2027 campaign, whatever year the machine running the test is in. */
    private val clock = Clock.fixed(Instant.parse("2027-03-10T15:00:00Z"), ZoneOffset.UTC)
    private val askedYears = mutableListOf<Int?>()
    private var answer: AppResult<List<PlotSamplingOverview>> = AppResult.Success(emptyList())

    private val repository = object : ThinningRepository by unused<ThinningRepository>() {
        override suspend fun getPlotSamplingOverview(campaignYear: Int?): AppResult<List<PlotSamplingOverview>> {
            askedYears += campaignYear
            return answer
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun plot(id: String, status: SamplingStatus) = PlotSamplingOverview(
        plotId = id, plotName = "Lote $id", variety = "SEVILLANA", areaHectares = 2.0, campaignYear = 2027,
        samplingStatus = status, sampledTreesCount = 0, treesNeeded = 5, isRepresentative = false,
    )

    private fun viewModel() = SelectPlotSamplingViewModel(GetPlotSamplingOverviewUseCase(repository), clock)

    private fun SelectPlotSamplingViewModel.content() = uiState.value as SelectPlotSamplingUiState.Content

    @Test
    fun `asks for the campaign of the clock's year`() {
        viewModel()

        assertEquals(listOf<Int?>(2027), askedYears)
    }

    @Test
    fun `selects the plot whose sampling is under way before one not started`() {
        answer = AppResult.Success(
            listOf(plot("a", SamplingStatus.COMPLETED), plot("b", SamplingStatus.NOT_STARTED), plot("c", SamplingStatus.IN_PROGRESS)),
        )

        assertEquals("c", viewModel().content().selectedPlotId)
    }

    @Test
    fun `without one under way it selects the first not started`() {
        answer = AppResult.Success(listOf(plot("a", SamplingStatus.COMPLETED), plot("b", SamplingStatus.NOT_STARTED)))

        assertEquals("b", viewModel().content().selectedPlotId)
    }

    @Test
    fun `a refresh keeps the plot the producer chose`() {
        answer = AppResult.Success(listOf(plot("a", SamplingStatus.NOT_STARTED), plot("b", SamplingStatus.NOT_STARTED)))
        val viewModel = viewModel()

        viewModel.selectPlot("b")
        viewModel.refresh()

        assertEquals("b", viewModel.content().selectedPlotId)
    }

    @Test
    fun `a failure on the first load shows the error with the clock's campaign`() {
        answer = AppResult.Failure(AppError.Offline)

        val content = viewModel().content()

        assertEquals(AppError.Offline, content.error)
        assertEquals(2027, content.campaignYear)
        assertNull(content.selectedPlotId)
    }
}
