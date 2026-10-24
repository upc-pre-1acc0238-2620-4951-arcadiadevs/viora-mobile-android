package pe.edu.upc.viora.features.home

import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.edu.upc.viora.features.croploadregulation.blockedPlan
import pe.edu.upc.viora.features.croploadregulation.domain.entity.CampaignPlan
import pe.edu.upc.viora.features.croploadregulation.issuedPlan
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.HarvestEntryUiState
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.PlotToSettle
import pe.edu.upc.viora.features.home.presentation.state.HomePhase
import pe.edu.upc.viora.features.home.presentation.state.HomePhaseRules
import pe.edu.upc.viora.features.home.presentation.state.HomePhaseUiState
import pe.edu.upc.viora.features.home.presentation.state.PhaseAction
import pe.edu.upc.viora.features.home.presentation.state.PhaseFigure
import pe.edu.upc.viora.features.home.presentation.state.PhaseMessage
import pe.edu.upc.viora.features.home.presentation.state.PhaseTiming
import pe.edu.upc.viora.features.phenology.domain.entity.ChillProjection
import pe.edu.upc.viora.features.phenology.domain.entity.ChillTracker
import pe.edu.upc.viora.features.phenology.domain.entity.ThermalAnomaly
import pe.edu.upc.viora.features.phenology.domain.entity.WinterSeasonState
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.telemetry.domain.entity.ForecastDay
import pe.edu.upc.viora.features.telemetry.domain.entity.WeatherForecast

/** Figma P10 · Fase: which phase the Home opens on and what each card says, from the plot's own data. */
class HomePhaseRulesTest {

    private val plot = Plot(
        id = PlotId("plot-1"), name = "La Yarada 02", variety = OliveVariety.SEVILLANA, areaHectares = 2.5, treesPerHectare = 100,
        rowSpacingMeters = 10.0, treeSpacingMeters = 10.0,
        outline = listOf(GeoPoint(-18.05, -70.25), GeoPoint(-18.05, -70.24), GeoPoint(-18.04, -70.24)),
        lastPruningDate = null, isActive = true, revision = 0,
    )

    private fun state(
        today: LocalDate,
        plan: CampaignPlan? = null,
        chill: ChillTracker? = null,
        forecast: WeatherForecast? = null,
        harvest: HarvestEntryUiState? = HarvestEntryUiState(today.year, emptyList(), 2),
    ): HomePhaseUiState = HomePhaseRules.state(today, plot, plan, chill, forecast, harvest)

    private fun HomePhaseUiState.card(phase: HomePhase) = cards[phase.ordinal]

    private fun HomePhaseUiState.currentPhase() = cards[current].phase

    private fun pending(today: LocalDate, count: Int) = HarvestEntryUiState(
        today.year,
        List(count) { PlotToSettle("p$it", "Lote $it", OliveVariety.SEVILLANA, 1.0) },
        2,
    )

    private fun chill(accumulated: Double, completed: LocalDate? = null, state: WinterSeasonState = WinterSeasonState.ACCUMULATING) = ChillTracker(
        plotId = "plot-1", seasonYear = 2026, accumulatedPortions = accumulated, thresholdPortions = 30.0, seasonState = state,
        evaluatedThrough = null, completionDate = completed, projection = ChillProjection.NOT_APPLICABLE, projectedCompletionDate = null,
        daysAbove24Celsius = 0, currentWarmStreakDays = 0, longestWarmStreakDays = 0, thermalAnomaly = ThermalAnomaly.NONE,
        previousSeason = null, curvePoints = emptyList(), syncedAt = Instant.EPOCH,
    )

    @Test
    fun `in november the thinning window of the plan counts down with its percentage`() {
        val today = LocalDate.of(2026, 11, 18)
        val state = state(today, plan = CampaignPlan("plot-1", issuedPlan(), null))

        assertEquals(HomePhase.THINNING, state.currentPhase())
        assertEquals(HomePhase.THINNING, state.headlinePhase)
        val card = state.card(HomePhase.THINNING)
        assertEquals(PhaseFigure.Days(12), card.figure)
        assertEquals(PhaseMessage.ThinPercentage(30, "La Yarada 02"), card.message)
        assertEquals(LocalDate.of(2026, 11, 30), card.endsOn)
        assertEquals(PhaseAction.SEE_THINNING_PLAN, card.action)
    }

    @Test
    fun `a plot without a plan uses the usual window and says the sampling decides how much`() {
        val today = LocalDate.of(2026, 11, 10)
        val card = state(today).card(HomePhase.THINNING)

        assertEquals(PhaseTiming.NOW, card.timing)
        assertEquals(PhaseMessage.ThinWithoutPlan("La Yarada 02"), card.message)
        assertEquals(LocalDate.of(2026, 11, 3), card.startsOn)
    }

    @Test
    fun `the recorded bloom moves the window by the profile offsets`() {
        val bloom = LocalDate.of(2026, 10, 1)
        val plan = blockedPlan().copy(fullBloomOn = bloom, windowOpensDaysAfterBloom = 14, windowClosesDaysAfterBloom = 49)
        val state = state(LocalDate.of(2026, 10, 20), plan = CampaignPlan("plot-1", plan, null))

        assertEquals(HomePhase.THINNING, state.currentPhase())
        assertEquals(LocalDate.of(2026, 10, 15), state.card(HomePhase.THINNING).startsOn)
        assertEquals(LocalDate.of(2026, 11, 19), state.card(HomePhase.THINNING).endsOn)
        assertEquals(PhaseTiming.DONE, state.card(HomePhase.FRUIT_SET).timing)
    }

    @Test
    fun `during fruit set the card counts the trees still to sample`() {
        val bloom = LocalDate.of(2026, 10, 5)
        val plan = blockedPlan(sampled = 3).copy(fullBloomOn = bloom, windowOpensDaysAfterBloom = 29, windowClosesDaysAfterBloom = 56)
        val state = state(LocalDate.of(2026, 10, 29), plan = CampaignPlan("plot-1", plan, null))

        assertEquals(HomePhase.FRUIT_SET, state.currentPhase())
        val card = state.card(HomePhase.FRUIT_SET)
        assertEquals(PhaseFigure.Trees(2), card.figure)
        assertEquals(PhaseMessage.TreesMissing("La Yarada 02"), card.message)
        assertEquals(true, card.hasSamplingStarted)
        assertEquals(PhaseAction.SAMPLE, card.action)
    }

    @Test
    fun `in bloom the hottest coming day asks to water early`() {
        val today = LocalDate.of(2026, 9, 30)
        val forecast = WeatherForecast(
            "plot-1",
            listOf(
                ForecastDay(today, 26.0, 12.0, 0, 5.0),
                ForecastDay(today.plusDays(2), 33.6, 14.0, 0, 5.0),
                ForecastDay(today.minusDays(1), 40.0, 14.0, 0, 5.0),
            ),
            Instant.EPOCH,
        )
        val card = state(today, forecast = forecast).card(HomePhase.BLOOM)

        assertEquals(PhaseTiming.NOW, card.timing)
        assertEquals(PhaseFigure.Temperature(34, today.plusDays(2)), card.figure)
        assertEquals(PhaseMessage.BloomHeat("La Yarada 02"), card.message)
        assertEquals(PhaseAction.RECORD_BLOOM, card.action)
    }

    @Test
    fun `a sampled plot without its full bloom asks for it instead of trusting the usual window`() {
        val today = LocalDate.of(2026, 10, 24)
        val card = state(today, plan = CampaignPlan("plot-1", blockedPlan(sampled = 5), null)).card(HomePhase.FRUIT_SET)

        assertEquals(PhaseTiming.NOW, card.timing)
        assertEquals(PhaseMessage.BloomMissing, card.message)
        assertEquals(PhaseAction.RECORD_BLOOM, card.action)
    }

    @Test
    fun `with the full bloom recorded the bloom pill goes back to the forecast`() {
        val plan = blockedPlan().copy(fullBloomOn = LocalDate.of(2026, 10, 10))
        val card = state(LocalDate.of(2026, 10, 1), plan = CampaignPlan("plot-1", plan, null)).card(HomePhase.BLOOM)

        assertEquals(PhaseAction.SEE_FORECAST, card.action)
    }

    @Test
    fun `in winter the chill portions fill the capsule`() {
        val state = state(LocalDate.of(2026, 7, 21), chill = chill(24.4))

        assertEquals(HomePhase.DORMANCY, state.currentPhase())
        val card = state.card(HomePhase.DORMANCY)
        assertEquals(PhaseFigure.ChillPortions(24, 30), card.figure)
        assertEquals(PhaseMessage.ChillMissing(6), card.message)
        assertEquals(24.4f / 30f, card.progress, 1e-4f)
    }

    @Test
    fun `in june a plot still to register keeps the harvest first`() {
        val today = LocalDate.of(2026, 6, 10)
        assertEquals(HomePhase.HARVEST, state(today, harvest = pending(today, 2)).currentPhase())
        assertEquals(HomePhase.DORMANCY, state(today).currentPhase())
    }

    @Test
    fun `out of season the harvest card is not presented as going on`() {
        val today = LocalDate.of(2026, 10, 23)
        val state = state(today)

        assertEquals(PhaseTiming.DONE, state.card(HomePhase.HARVEST).timing)
        assertEquals(PhaseMessage.HarvestRegistered(2026), state.card(HomePhase.HARVEST).message)
        assertEquals(PhaseAction.SEE_ALTERNATION, state.card(HomePhase.HARVEST).action)
    }

    @Test
    fun `a harvest still to register after june is pending, not over`() {
        val today = LocalDate.of(2026, 10, 23)
        val card = state(today, harvest = pending(today, 3)).card(HomePhase.HARVEST)

        assertEquals(PhaseTiming.LATE, card.timing)
        assertEquals(PhaseFigure.Plots(3), card.figure)
        assertEquals(PhaseAction.REGISTER_HARVEST, card.action)
    }

    @Test
    fun `between the thinning and the harvest the carousel opens on the coming harvest`() {
        val today = LocalDate.of(2027, 2, 1)
        val state = state(today)

        assertEquals(HomePhase.HARVEST, state.currentPhase())
        assertNull(state.headlinePhase)
        assertEquals(PhaseFigure.Days(78), state.card(HomePhase.HARVEST).figure)
        assertEquals(PhaseMessage.StartsIn(HomePhase.HARVEST), state.card(HomePhase.HARVEST).message)
    }
}
