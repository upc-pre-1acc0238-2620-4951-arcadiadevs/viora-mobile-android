package pe.edu.upc.viora.features.climate.presentation

import androidx.lifecycle.SavedStateHandle
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.climate.application.usecase.ObserveForecastLastRefreshUseCase
import pe.edu.upc.viora.features.climate.application.usecase.ObserveWeatherForecastUseCase
import pe.edu.upc.viora.features.climate.application.usecase.RefreshWeatherForecastUseCase
import pe.edu.upc.viora.features.climate.domain.entity.DayForecast
import pe.edu.upc.viora.features.climate.domain.entity.WeatherForecast
import pe.edu.upc.viora.features.climate.domain.repository.WeatherForecastRepository
import pe.edu.upc.viora.features.climate.presentation.state.ClimateUiState
import pe.edu.upc.viora.features.climate.presentation.viewmodel.ClimateViewModel
import pe.edu.upc.viora.features.plotmanagement.application.usecase.ObservePlotUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.NewPlot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotChanges
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId

@OptIn(ExperimentalCoroutinesApi::class)
class ClimateViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val plotId = "plot-123"
    private val plot = Plot(
        id = PlotId(plotId),
        name = "Lote Norte",
        variety = OliveVariety.CRIOLLA,
        areaHectares = 2.5,
        treesPerHectare = 200,
        rowSpacingMeters = 7.0,
        treeSpacingMeters = 7.0,
        outline = emptyList(),
        lastPruningDate = null,
        isActive = true,
        revision = 1L,
    )

    private val sampleForecast = WeatherForecast(
        plotId = PlotId(plotId),
        dailyForecasts = (0..6).map { i ->
            DayForecast(
                date = LocalDate.of(2026, 10, 5).plusDays(i.toLong()),
                maxTempCelsius = 22.0 + i,
                minTempCelsius = 10.0 + i,
                precipitationProbability = 5.0 * i,
                windSpeedKmh = 12.0 + i,
                isFrostRisk = i == 0,
                syncedAt = Instant.parse("2026-10-05T12:00:00Z"),
            )
        },
        generatedAt = Instant.parse("2026-10-05T12:00:00Z"),
    )

    private lateinit var fakeWeatherRepo: FakeWeatherForecastRepository
    private lateinit var fakePlotRepo: FakePlotRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeWeatherRepo = FakeWeatherForecastRepository()
        fakePlotRepo = FakePlotRepository()
        fakePlotRepo.plots.value = listOf(plot)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        plotIdArg: String = plotId,
        plotNameArg: String = "Lote Norte",
    ): ClimateViewModel {
        return ClimateViewModel(
            savedStateHandle = SavedStateHandle(mapOf("plotId" to plotIdArg, "plotName" to plotNameArg)),
            observeWeatherForecast = ObserveWeatherForecastUseCase(fakeWeatherRepo),
            observeLastRefresh = ObserveForecastLastRefreshUseCase(fakeWeatherRepo),
            observePlot = ObservePlotUseCase(fakePlotRepo),
            refreshWeatherForecast = RefreshWeatherForecastUseCase(fakeWeatherRepo),
        )
    }

    @Test
    fun `when forecast is present, state is Content with 7 days and plot info`() = runTest {
        fakeWeatherRepo.forecastFlow.value = sampleForecast
        fakeWeatherRepo.lastRefreshFlow.value = Instant.parse("2026-10-05T12:00:00Z")

        val viewModel = createViewModel()
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }

        val state = viewModel.uiState.value
        assertTrue("Expected Content state, got $state", state is ClimateUiState.Content)
        val content = state as ClimateUiState.Content
        assertEquals("Lote Norte", content.plotName)
        assertEquals(OliveVariety.CRIOLLA, content.variety)
        assertEquals(7, content.dailyForecasts.size)
        assertEquals(0, content.selectedDayIndex)
        assertEquals(22.0, content.selectedDay!!.maxTempCelsius, 0.01)
        assertTrue(content.selectedDay!!.isFrostRisk)
        assertFalse(content.isOffline)

        collectJob.cancel()
    }

    @Test
    fun `selectDay updates selected day index and active day details`() = runTest {
        fakeWeatherRepo.forecastFlow.value = sampleForecast
        val viewModel = createViewModel()
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }

        viewModel.selectDay(2)

        val state = viewModel.uiState.value as ClimateUiState.Content
        assertEquals(2, state.selectedDayIndex)
        assertEquals(24.0, state.selectedDay!!.maxTempCelsius, 0.01)
        assertFalse(state.selectedDay!!.isFrostRisk)

        collectJob.cancel()
    }

    @Test
    fun `refresh calls repository refreshForecast`() = runTest {
        fakeWeatherRepo.forecastFlow.value = sampleForecast
        val viewModel = createViewModel()
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }

        assertEquals(1, fakeWeatherRepo.refreshCount) // from init
        viewModel.refresh()
        assertEquals(2, fakeWeatherRepo.refreshCount)

        collectJob.cancel()
    }

    @Test
    fun `offline failure with cached forecast retains content and marks isOffline true`() = runTest {
        fakeWeatherRepo.forecastFlow.value = sampleForecast
        fakeWeatherRepo.refreshResult = AppResult.Failure(AppError.Offline)

        val viewModel = createViewModel()
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }

        val state = viewModel.uiState.value
        assertTrue("Expected Content state even when offline", state is ClimateUiState.Content)
        val content = state as ClimateUiState.Content
        assertTrue(content.isOffline)
        assertEquals(7, content.dailyForecasts.size)

        collectJob.cancel()
    }

    @Test
    fun `network failure with empty cache transitions to Error state`() = runTest {
        fakeWeatherRepo.forecastFlow.value = null
        fakeWeatherRepo.refreshResult = AppResult.Failure(AppError.Offline)

        val viewModel = createViewModel()
        val collectJob = launch(testDispatcher) { viewModel.uiState.collect {} }

        val state = viewModel.uiState.value
        assertTrue("Expected Error state when cache is empty and refresh fails, got $state", state is ClimateUiState.Error)
        val errorState = state as ClimateUiState.Error
        assertEquals(AppError.Offline, errorState.error)

        collectJob.cancel()
    }
}

private class FakeWeatherForecastRepository : WeatherForecastRepository {
    val forecastFlow = MutableStateFlow<WeatherForecast?>(null)
    val lastRefreshFlow = MutableStateFlow<Instant?>(null)
    var refreshResult: AppResult<Unit> = AppResult.Success(Unit)
    var refreshCount = 0

    override fun observeForecast(plotId: String): Flow<WeatherForecast?> = forecastFlow
    override fun observeLastRefresh(plotId: String): Flow<Instant?> = lastRefreshFlow
    override suspend fun refreshForecast(plotId: String): AppResult<Unit> {
        refreshCount++
        return refreshResult
    }
}

private class FakePlotRepository : PlotRepository {
    val plots = MutableStateFlow<List<Plot>>(emptyList())

    override fun observePlots(): Flow<List<Plot>> = plots
    override fun observePlot(id: PlotId): Flow<Plot?> = plots.map { list -> list.firstOrNull { it.id == id } }
    override fun observeArchivedPlots(): Flow<List<Plot>> = MutableStateFlow(emptyList())
    override fun observeLastRefresh(): Flow<Instant?> = MutableStateFlow(null)
    override suspend fun refresh(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun refreshArchived(): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun register(newPlot: NewPlot): AppResult<Plot> = AppResult.Failure(AppError.Offline)
    override suspend fun update(id: PlotId, changes: PlotChanges): AppResult<Plot> = AppResult.Failure(AppError.Offline)
    override suspend fun archive(id: PlotId): AppResult<Unit> = AppResult.Success(Unit)
    override suspend fun restore(id: PlotId): AppResult<Plot> = AppResult.Failure(AppError.Offline)
}
