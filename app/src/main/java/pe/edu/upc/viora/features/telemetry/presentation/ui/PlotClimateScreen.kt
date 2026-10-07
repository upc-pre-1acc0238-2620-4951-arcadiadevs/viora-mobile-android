package pe.edu.upc.viora.features.telemetry.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Duration
import java.util.Locale
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.VioraSectionHeader
import pe.edu.upc.viora.core.designsystem.component.VioraTabBarDefaults
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Harvest700
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.VioraTheme
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.LotSection
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.LotSectionsSheet
import pe.edu.upc.viora.features.telemetry.domain.entity.DailyThermalSummary
import pe.edu.upc.viora.features.telemetry.domain.entity.ForecastDay
import pe.edu.upc.viora.features.telemetry.domain.entity.SoilMoistureRules
import pe.edu.upc.viora.features.telemetry.domain.valueobject.HeatLevel
import pe.edu.upc.viora.features.telemetry.domain.valueobject.SkyCondition
import pe.edu.upc.viora.features.telemetry.domain.valueobject.SoilMoistureStatus
import pe.edu.upc.viora.features.telemetry.domain.valueobject.TelemetryMetric
import pe.edu.upc.viora.features.telemetry.presentation.state.ClimatePlot
import pe.edu.upc.viora.features.telemetry.presentation.state.PlotClimateUiState
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.ClimateErrorCard
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.ClimateHeadline
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.ClimateNotice
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.ClimateTopBar
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.DayNightChart
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.SelectablePill
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.SoilGauge
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.Sparkline
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.SunGlyph
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.WeatherGlyph
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.formatClock
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.formatDegrees
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.formatElapsed
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.formatPercent
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.formatReadingStamp
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.formatWholeNumber
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.shortWeekday
import pe.edu.upc.viora.features.telemetry.presentation.viewmodel.PlotClimateViewModel

private val ScreenPadding = 24.dp
private val CardShape = RoundedCornerShape(28.dp)
private val SENSOR_CARDS_HEIGHT = 252.dp
private const val SOIL_PROBE_DEPTH_CM = 30
private const val MAX_FORECAST_DAYS = 7
private const val MAX_DAY_NIGHT_DAYS = 7

/**
 * "El clima de tu lote" (Figma P90): current weather and the 7-day forecast of the plot (US19),
 * the readings of its virtual sensors and the day/night temperature of the week (US17).
 */
@Composable
fun PlotClimateScreen(
    onBack: () -> Unit,
    onOpenMetric: (plotId: String, plotName: String, metric: TelemetryMetric) -> Unit,
    onOpenSensors: (plotId: String, plotName: String) -> Unit,
    onOpenHarvestHistory: (plotId: String, plotName: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlotClimateViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showMore by rememberSaveable { mutableStateOf(false) }
    val content = state as? PlotClimateUiState.Content

    PlotClimateContent(
        state = state,
        onBack = onBack,
        onMore = { showMore = true },
        onSelectPlot = viewModel::selectPlot,
        onRefresh = viewModel::refresh,
        onOpenMetric = { metric -> content?.let { onOpenMetric(it.selectedPlotId, it.plotName, metric) } },
        modifier = modifier,
    )

    if (showMore && content != null) {
        LotSectionsSheet(
            plotName = content.plotName,
            summary = stringResource(R.string.climate_top_subtitle),
            current = LotSection.CLIMATE,
            onSelect = { section ->
                showMore = false
                when (section) {
                    LotSection.HARVEST -> onOpenHarvestHistory(content.selectedPlotId, content.plotName)
                    LotSection.SENSORS -> onOpenSensors(content.selectedPlotId, content.plotName)
                    else -> Unit
                }
            },
            onDismiss = { showMore = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlotClimateContent(
    state: PlotClimateUiState,
    onBack: () -> Unit,
    onMore: () -> Unit,
    onSelectPlot: (String) -> Unit,
    onRefresh: () -> Unit,
    onOpenMetric: (TelemetryMetric) -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = state as? PlotClimateUiState.Content
    Column(
        modifier = modifier.fillMaxSize().background(Neutral100).statusBarsPadding(),
    ) {
        ClimateTopBar(
            title = content?.plotName.orEmpty(),
            subtitle = stringResource(R.string.climate_top_subtitle),
            onBack = onBack,
            onMore = onMore,
        )
        PullToRefreshBox(
            isRefreshing = content?.isRefreshing == true,
            onRefresh = onRefresh,
            modifier = Modifier.weight(1f),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ScreenPadding),
            ) {
                Spacer(Modifier.height(16.dp))
                ClimateHeadline(
                    lead = stringResource(R.string.climate_title_lead),
                    emphasis = stringResource(R.string.climate_title_emphasis),
                )
                Spacer(Modifier.height(16.dp))
                when (state) {
                    PlotClimateUiState.Loading -> Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator(color = Green900) }
                    is PlotClimateUiState.Error -> ClimateErrorCard(error = state.error, onRetry = onRefresh)
                    is PlotClimateUiState.Content -> ClimateSections(state, onSelectPlot, onOpenMetric)
                }
                Spacer(Modifier.height(VioraTabBarDefaults.ContentBottomPadding + 24.dp))
            }
        }
    }
}

@Composable
private fun ClimateSections(
    state: PlotClimateUiState.Content,
    onSelectPlot: (String) -> Unit,
    onOpenMetric: (TelemetryMetric) -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]

    if (state.plots.size > 1) {
        PlotChips(plots = state.plots, selectedId = state.selectedPlotId, onSelect = onSelectPlot)
        Spacer(Modifier.height(16.dp))
    }
    val forecast = state.forecast
    if (state.isForecastFromCache && forecast != null) {
        ClimateNotice(
            text = stringResource(
                R.string.climate_forecast_cached,
                formatReadingStamp(forecast.syncedAt, state.zone, locale),
            ),
        )
        Spacer(Modifier.height(16.dp))
    }

    WeatherHeroCard(state, locale)

    Spacer(Modifier.height(28.dp))
    VioraSectionHeader(title = stringResource(R.string.climate_next_days))
    ForecastCard(state, locale)

    Spacer(Modifier.height(28.dp))
    VioraSectionHeader(title = stringResource(R.string.climate_sensors_title))
    SensorsRow(state, locale, onOpenMetric)

    Spacer(Modifier.height(28.dp))
    VioraSectionHeader(title = stringResource(R.string.climate_daynight_title))
    DayNightCard(state, locale)
}

@Composable
private fun PlotChips(plots: List<ClimatePlot>, selectedId: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        plots.forEach { plot ->
            SelectablePill(text = plot.name, selected = plot.id == selectedId, onClick = { onSelect(plot.id) })
        }
    }
}

// ─── Hero: the weather now ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WeatherHeroCard(state: PlotClimateUiState.Content, locale: Locale) {
    val latestTemperature = state.series.latest(TelemetryMetric.TEMPERATURE)
    val latestHumidity = state.series.latest(TelemetryMetric.AIR_HUMIDITY)
    val today = state.todayForecast

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Harvest100)
            .padding(20.dp),
    ) {
        val sky = today?.sky ?: SkyCondition.SUNNY
        if (sky == SkyCondition.SUNNY) {
            SunGlyph(modifier = Modifier.align(Alignment.TopEnd), glyphSize = 84.dp)
        } else {
            WeatherGlyph(modifier = Modifier.align(Alignment.TopEnd), sky = sky, glyphSize = 84.dp, tint = Harvest700)
        }
        Column {
            Text(
                text = latestTemperature?.let {
                    stringResource(R.string.climate_now_ago, formatElapsed(it.observedAt, state.now))
                } ?: stringResource(R.string.climate_now_none),
                style = MaterialTheme.typography.labelMedium,
                color = Neutral700,
            )
            Text(
                text = latestTemperature?.let { formatDegrees(locale, it.value) } ?: "—",
                style = TextStyle(
                    fontFamily = NewsreaderFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 88.sp,
                    lineHeight = 92.sp,
                ),
                color = Neutral900,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (today != null) {
                Text(
                    text = stringResource(skyLabel(today.sky)),
                    style = MaterialTheme.typography.titleMedium,
                    color = Neutral900,
                )
                Text(
                    text = stringResource(
                        R.string.climate_high_low,
                        formatDegrees(locale, today.maxTemperatureCelsius),
                        formatDegrees(locale, today.minTemperatureCelsius),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral700,
                )
            }
            FlowRow(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                latestHumidity?.let {
                    HeroChip(stringResource(R.string.climate_chip_air, formatPercent(locale, it.value)))
                }
                today?.let {
                    HeroChip(stringResource(R.string.climate_chip_wind, formatWholeNumber(locale, it.windSpeedKmh)))
                    HeroChip(
                        stringResource(
                            R.string.climate_chip_rain,
                            formatPercent(locale, it.precipitationProbabilityPercent.toDouble()),
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroChip(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = Neutral900,
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(Neutral0)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

private fun skyLabel(sky: SkyCondition): Int = when (sky) {
    SkyCondition.SUNNY -> R.string.climate_sky_sunny
    SkyCondition.PARTLY_CLOUDY -> R.string.climate_sky_partly_cloudy
    SkyCondition.RAINY -> R.string.climate_sky_rainy
}

// ─── Next 7 days ────────────────────────────────────────────────────────────────────────────

@Composable
private fun ForecastCard(state: PlotClimateUiState.Content, locale: Locale) {
    val forecast = state.forecast
    Column(
        modifier = Modifier.fillMaxWidth().clip(CardShape).background(Neutral0).padding(12.dp),
    ) {
        if (forecast == null) {
            Text(
                text = stringResource(R.string.climate_forecast_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral600,
                modifier = Modifier.padding(8.dp),
            )
            return@Column
        }
        val currentTemperature = state.series.latest(TelemetryMetric.TEMPERATURE)?.value
        forecast.days.take(MAX_FORECAST_DAYS).forEach { day ->
            val isToday = day.date == state.today
            ForecastRow(
                day = day,
                isToday = isToday,
                weekMin = forecast.weekMinCelsius ?: day.minTemperatureCelsius,
                weekMax = forecast.weekMaxCelsius ?: day.maxTemperatureCelsius,
                marker = if (isToday) currentTemperature else null,
                locale = locale,
            )
        }
        Text(
            text = stringResource(
                R.string.climate_forecast_updated,
                formatClock(forecast.syncedAt, state.zone, locale),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = Neutral600,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
        )
    }
}

@Composable
private fun ForecastRow(
    day: ForecastDay,
    isToday: Boolean,
    weekMin: Double,
    weekMax: Double,
    marker: Double?,
    locale: Locale,
) {
    val extreme = day.heat == HeatLevel.EXTREME
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (extreme) Terracotta100 else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.width(64.dp)) {
            Text(
                text = if (isToday) {
                    stringResource(R.string.climate_today)
                } else {
                    "${shortWeekday(day.date, locale)} ${day.date.dayOfMonth}"
                },
                style = MaterialTheme.typography.titleSmall,
                color = Neutral900,
            )
            Text(
                text = stringResource(R.string.climate_wind_kmh, formatWholeNumber(locale, day.windSpeedKmh)),
                style = MaterialTheme.typography.labelSmall,
                color = Neutral600,
            )
        }
        Row(Modifier.width(64.dp), verticalAlignment = Alignment.CenterVertically) {
            WeatherGlyph(sky = day.sky, glyphSize = 24.dp)
            if (day.precipitationProbabilityPercent > 0) {
                Text(
                    text = formatPercent(locale, day.precipitationProbabilityPercent.toDouble()),
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral600,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
        Text(
            text = formatDegrees(locale, day.minTemperatureCelsius),
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral600,
            textAlign = TextAlign.End,
            modifier = Modifier.width(36.dp),
        )
        TemperatureBar(
            min = day.minTemperatureCelsius,
            max = day.maxTemperatureCelsius,
            weekMin = weekMin,
            weekMax = weekMax,
            color = heatColor(day.heat),
            marker = marker,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
        )
        Text(
            text = formatDegrees(locale, day.maxTemperatureCelsius),
            style = MaterialTheme.typography.titleSmall,
            color = Neutral900,
            modifier = Modifier.width(36.dp),
        )
    }
}

private fun heatColor(heat: HeatLevel): Color = when (heat) {
    HeatLevel.NORMAL -> Green800
    HeatLevel.WARM -> Harvest300
    HeatLevel.EXTREME -> Terracotta500
}

/** The day's range drawn inside the week's range; the white dot is the temperature right now. */
@Composable
private fun TemperatureBar(
    min: Double,
    max: Double,
    weekMin: Double,
    weekMax: Double,
    color: Color,
    marker: Double?,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier.height(12.dp)) {
        val span = (weekMax - weekMin).takeIf { it > 0.0 } ?: 1.0
        fun xOf(value: Double) = ((value - weekMin) / span).toFloat().coerceIn(0f, 1f) * size.width
        val barHeight = 6.dp.toPx()
        val top = (size.height - barHeight) / 2
        val corner = CornerRadius(barHeight / 2)
        drawRoundRect(Neutral100, Offset(0f, top), Size(size.width, barHeight), corner)
        val start = xOf(min)
        val end = maxOf(xOf(max), start + barHeight)
        drawRoundRect(color, Offset(start, top), Size(end - start, barHeight), corner)
        if (marker != null && marker in min..max) {
            val center = Offset(xOf(marker), size.height / 2)
            drawCircle(Neutral0, 5.dp.toPx(), center)
            drawCircle(Neutral900, 5.dp.toPx(), center, style = Stroke(1.5.dp.toPx()))
        }
    }
}

// ─── Plot sensors ───────────────────────────────────────────────────────────────────────────

@Composable
private fun SensorsRow(
    state: PlotClimateUiState.Content,
    locale: Locale,
    onOpenMetric: (TelemetryMetric) -> Unit,
) {
    val recentWindow = state.now.minus(Duration.ofHours(RECENT_HOURS))
    fun recent(metric: TelemetryMetric) = state.series.points(metric).filter { it.observedAt >= recentWindow }

    Row(
        modifier = Modifier.fillMaxWidth().height(SENSOR_CARDS_HEIGHT),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SoilCard(
            state = state,
            locale = locale,
            onClick = { onOpenMetric(TelemetryMetric.SOIL_MOISTURE) },
            modifier = Modifier.weight(1f).fillMaxHeight(),
        )
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            MetricCard(
                label = stringResource(R.string.climate_temperature),
                value = state.series.latest(TelemetryMetric.TEMPERATURE)?.let { formatDegrees(locale, it.value, decimals = 1) },
                trend = recent(TelemetryMetric.TEMPERATURE).map { it.value },
                onClick = { onOpenMetric(TelemetryMetric.TEMPERATURE) },
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                label = stringResource(R.string.climate_air_humidity),
                value = state.series.latest(TelemetryMetric.AIR_HUMIDITY)?.let { formatPercent(locale, it.value) },
                trend = recent(TelemetryMetric.AIR_HUMIDITY).map { it.value },
                onClick = { onOpenMetric(TelemetryMetric.AIR_HUMIDITY) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private const val RECENT_HOURS = 24L

@Composable
private fun SoilCard(
    state: PlotClimateUiState.Content,
    locale: Locale,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val latest = state.series.latest(TelemetryMetric.SOIL_MOISTURE)
    val status = state.series.soilStatus()
    val container = when (status) {
        SoilMoistureStatus.STRESS -> Terracotta100
        SoilMoistureStatus.WATCH -> Harvest100
        else -> Green200
    }
    Row(
        modifier = modifier
            .clip(CardShape)
            .background(container)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(16.dp),
    ) {
        Column(Modifier.weight(1f).fillMaxHeight()) {
            Text(
                text = stringResource(R.string.climate_soil_title),
                style = MaterialTheme.typography.labelMedium,
                color = Neutral700,
            )
            if (latest == null || status == null) {
                Text(
                    text = stringResource(R.string.climate_soil_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral700,
                    modifier = Modifier.padding(top = 8.dp),
                )
                return@Column
            }
            Text(
                text = formatPercent(locale, latest.value),
                style = TextStyle(fontFamily = NewsreaderFamily, fontSize = 44.sp, lineHeight = 48.sp),
                color = Neutral900,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = stringResource(R.string.climate_soil_probe, SOIL_PROBE_DEPTH_CM, stringResource(soilStatusLabel(status))),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral700,
            )
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Neutral0)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(Terracotta500))
                Text(
                    text = stringResource(
                        R.string.climate_soil_recharge,
                        formatPercent(locale, SoilMoistureRules.RECHARGE_POINT_PERCENT),
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral900,
                )
            }
        }
        if (latest != null) {
            SoilGauge(
                percent = latest.value,
                rechargePercent = SoilMoistureRules.RECHARGE_POINT_PERCENT,
                trackColor = Neutral0.copy(alpha = 0.6f),
                modifier = Modifier.width(44.dp).fillMaxHeight(),
            )
        }
    }
}

private fun soilStatusLabel(status: SoilMoistureStatus): Int = when (status) {
    SoilMoistureStatus.IN_RANGE -> R.string.climate_soil_status_in_range
    SoilMoistureStatus.WATCH -> R.string.climate_soil_status_watch
    SoilMoistureStatus.STRESS -> R.string.climate_soil_status_stress
}

@Composable
private fun MetricCard(
    label: String,
    value: String?,
    trend: List<Double>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(Neutral0)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = value ?: "—",
            style = TextStyle(fontFamily = NewsreaderFamily, fontSize = 30.sp, lineHeight = 34.sp),
            color = Neutral900,
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Neutral600)
        Sparkline(
            values = trend,
            color = Green800,
            modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 6.dp),
        )
    }
}

// ─── Day and night ──────────────────────────────────────────────────────────────────────────

@Composable
private fun DayNightCard(state: PlotClimateUiState.Content, locale: Locale) {
    val summary = remember(state.series, state.zone) { state.series.thermalSummary(state.zone) }
    val days: List<DailyThermalSummary> = remember(state.series, state.zone) {
        state.series.dailyThermalSummaries(state.zone).takeLast(MAX_DAY_NIGHT_DAYS)
    }
    Column(
        modifier = Modifier.fillMaxWidth().clip(CardShape).background(Neutral0).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (summary == null) {
            Text(
                text = stringResource(R.string.climate_daynight_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral600,
            )
            return@Column
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                DayNightFigure(R.string.climate_day, formatDegrees(locale, summary.dayAverageCelsius, 1), Harvest300)
                DayNightFigure(R.string.climate_night, formatDegrees(locale, summary.nightAverageCelsius, 1), Green900)
            }
            Column(
                modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Green200).padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = formatDegrees(locale, summary.oscillationCelsius, 1),
                    style = TextStyle(fontFamily = NewsreaderFamily, fontSize = 22.sp, lineHeight = 26.sp),
                    color = Neutral900,
                )
                Text(
                    text = stringResource(R.string.climate_oscillation),
                    style = MaterialTheme.typography.labelSmall,
                    color = Neutral700,
                )
            }
        }
        DayNightChart(days = days, locale = locale)
        Text(
            text = stringResource(R.string.climate_daynight_caption),
            style = MaterialTheme.typography.bodySmall,
            color = Neutral600,
        )
    }
}

@Composable
private fun DayNightFigure(label: Int, value: String, dot: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
            Text(text = stringResource(label), style = MaterialTheme.typography.labelMedium, color = Neutral600)
        }
        Text(
            text = value,
            style = TextStyle(fontFamily = NewsreaderFamily, fontSize = 30.sp, lineHeight = 34.sp),
            color = Neutral900,
        )
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────────────────────

private fun previewState() = PlotClimateUiState.Content(
    plots = listOf(ClimatePlot("plot-1", "La Yarada 02"), ClimatePlot("plot-2", "Lote Norte")),
    selectedPlotId = "plot-1",
    plotName = "La Yarada 02",
    today = PreviewToday,
    now = PreviewNow,
    zone = PreviewZone,
    forecast = previewForecast(),
    series = previewSeries(),
    isRefreshing = false,
    refreshError = null,
)

@Preview(showBackground = true, backgroundColor = 0xFFF3F0EA, heightDp = 2000)
@Composable
private fun PlotClimatePreview() {
    VioraTheme {
        PlotClimateContent(
            state = previewState(),
            onBack = {},
            onMore = {},
            onSelectPlot = {},
            onRefresh = {},
            onOpenMetric = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F0EA, heightDp = 800)
@Composable
private fun PlotClimateLoadingPreview() {
    VioraTheme {
        PlotClimateContent(
            state = PlotClimateUiState.Loading,
            onBack = {},
            onMore = {},
            onSelectPlot = {},
            onRefresh = {},
            onOpenMetric = {},
        )
    }
}
