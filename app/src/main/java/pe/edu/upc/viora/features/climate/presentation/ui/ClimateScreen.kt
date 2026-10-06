package pe.edu.upc.viora.features.climate.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.TextStyle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.VioraTabBarDefaults
import pe.edu.upc.viora.core.designsystem.theme.Spacing
import pe.edu.upc.viora.core.designsystem.theme.Green100
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Harvest700
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta600
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.climate.domain.entity.DayForecast
import pe.edu.upc.viora.features.climate.presentation.state.ClimateUiState
import pe.edu.upc.viora.features.climate.presentation.viewmodel.ClimateViewModel
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.LotSection
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.LotSectionsSheet
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.formatCount
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.formatHectares
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.labelRes
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.CircleIconButton

@Composable
fun ClimateScreen(
    onBack: () -> Unit,
    onHarvestHistory: () -> Unit = {},
    onSensors: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ClimateViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSectionsSheet by rememberSaveable { mutableStateOf(false) }

    ClimateContent(
        state = state,
        onBack = onBack,
        onMore = { showSectionsSheet = true },
        onSelectDay = viewModel::selectDay,
        onRefresh = viewModel::refresh,
        modifier = modifier,
    )

    if (showSectionsSheet && state is ClimateUiState.Content) {
        val content = state as ClimateUiState.Content
        val summary = if (content.variety != null && content.areaHectares != null && content.estimatedTrees != null) {
            stringResource(
                R.string.plot_options_subtitle,
                stringResource(content.variety.labelRes()),
                formatHectares(content.areaHectares),
                formatCount(content.estimatedTrees),
            )
        } else {
            content.plotName
        }

        LotSectionsSheet(
            plotName = content.plotName,
            summary = summary,
            current = LotSection.CLIMATE,
            onSelect = { section ->
                showSectionsSheet = false
                when (section) {
                    LotSection.HARVEST -> onHarvestHistory()
                    LotSection.SENSORS -> onSensors()
                    else -> Unit
                }
            },
            onDismiss = { showSectionsSheet = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClimateContent(
    state: ClimateUiState,
    onBack: () -> Unit,
    onMore: () -> Unit,
    onSelectDay: (Int) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when (state) {
            ClimateUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green800)
                }
            }
            is ClimateUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(Spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.climate_error_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Neutral900,
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        text = stringResource(state.error.messageRes()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral600,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(Spacing.lg))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(Spacing.lg))
                            .background(Green800)
                            .clickable(role = Role.Button, onClick = onRefresh)
                            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                    ) {
                        Text(
                            text = stringResource(R.string.climate_retry),
                            style = MaterialTheme.typography.labelLarge,
                            color = Neutral0,
                        )
                    }
                }
            }
            is ClimateUiState.Content -> {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(bottom = VioraTabBarDefaults.ContentBottomPadding + Spacing.md),
                    ) {
                        ClimateTopBar(
                            plotName = state.plotName,
                            varietyName = state.variety?.let { stringResource(it.labelRes()) },
                            areaHa = state.areaHectares,
                            onBack = onBack,
                            onMore = onMore,
                        )

                        if (state.isOffline) {
                            OfflineNoticePill(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.xxs))
                        }

                        Spacer(Modifier.height(Spacing.xs))

                        val selectedDay = state.selectedDay
                        if (selectedDay != null) {
                            HeroForecastCard(
                                day = selectedDay,
                                isToday = state.selectedDayIndex == 0,
                                modifier = Modifier.padding(horizontal = Spacing.lg),
                            )
                        }

                        Spacer(Modifier.height(Spacing.lg))

                        Text(
                            text = stringResource(R.string.climate_forecast_section_title),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Neutral900,
                            modifier = Modifier.padding(horizontal = Spacing.lg),
                        )

                        Spacer(Modifier.height(Spacing.sm))

                        WeeklyForecastCarousel(
                            forecasts = state.dailyForecasts,
                            selectedIndex = state.selectedDayIndex,
                            onSelectDay = onSelectDay,
                        )

                        Spacer(Modifier.height(Spacing.lg))

                        ThermalTrendCard(
                            forecasts = state.dailyForecasts,
                            modifier = Modifier.padding(horizontal = Spacing.lg),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClimateTopBar(
    plotName: String,
    varietyName: String?,
    areaHa: Double?,
    onBack: () -> Unit,
    onMore: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        CircleIconButton(
            icon = R.drawable.ic_arrow_back,
            contentDescription = stringResource(R.string.action_back),
            onClick = onBack,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = plotName,
                style = MaterialTheme.typography.titleLarge.copy(fontFamily = MaterialTheme.typography.displaySmall.fontFamily),
                color = Neutral900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val subtitle = buildString {
                if (!varietyName.isNullOrBlank()) append(varietyName)
                if (areaHa != null) {
                    if (isNotEmpty()) append(" · ")
                    append(formatHectares(areaHa))
                }
            }
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral600,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        CircleIconButton(
            icon = R.drawable.ic_more_vert,
            contentDescription = stringResource(R.string.plot_menu_more),
            onClick = onMore,
        )
    }
}

@Composable
private fun OfflineNoticePill(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Spacing.md))
            .background(Neutral200)
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_cloud),
            contentDescription = null,
            tint = Neutral700,
            modifier = Modifier.size(Spacing.md),
        )
        Text(
            text = stringResource(R.string.climate_offline_badge),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Neutral700,
        )
    }
}

@Composable
private fun HeroForecastCard(
    day: DayForecast,
    isToday: Boolean,
    modifier: Modifier = Modifier,
) {
    val cardBackground = if (day.isFrostRisk) Terracotta100 else Harvest100

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(cardBackground)
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        // Date & Today tag
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val locale = LocalConfiguration.current.locales[0]
            val dayOfWeek = day.date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
            val formattedDate = "$dayOfWeek, ${day.date.dayOfMonth} ${day.date.month.getDisplayName(TextStyle.SHORT, locale)}"

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Neutral900,
            )

            if (isToday) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Spacing.sm))
                        .background(Green800)
                        .padding(horizontal = Spacing.xs, vertical = Spacing.xxs),
                ) {
                    Text(
                        text = stringResource(R.string.climate_today),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Neutral0,
                    )
                }
            }
        }

        // Main Temperature Display
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = stringResource(R.string.climate_temp_degrees_value, day.maxTempCelsius.toInt()),
                        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                        color = Neutral900,
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Text(
                        text = stringResource(R.string.climate_temp_min_celsius_format, day.minTempCelsius.toInt()),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Neutral600,
                        modifier = Modifier.padding(bottom = Spacing.xs),
                    )
                }
                val amplitude = day.maxTempCelsius - day.minTempCelsius
                Text(
                    text = stringResource(R.string.climate_thermal_amplitude, amplitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral700,
                )
            }

            Box(
                modifier = Modifier
                    .size(Spacing.xxxl)
                    .clip(CircleShape)
                    .background(Neutral0),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(if (day.precipitationProbability > 40.0) R.drawable.ic_water_drop else R.drawable.ic_cloud),
                    contentDescription = null,
                    tint = if (day.isFrostRisk) Terracotta600 else Harvest700,
                    modifier = Modifier.size(Spacing.xl),
                )
            }
        }

        // Risk / Alert Banner
        if (day.isFrostRisk) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Spacing.md))
                    .background(Neutral0)
                    .padding(Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_warning),
                    contentDescription = null,
                    tint = Terracotta600,
                    modifier = Modifier.size(Spacing.lg),
                )
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                    Text(
                        text = stringResource(R.string.climate_alert_frost_title),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Terracotta700,
                    )
                    Text(
                        text = stringResource(R.string.climate_alert_frost_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = Neutral700,
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Spacing.md))
                    .background(Neutral0)
                    .padding(Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = Green800,
                    modifier = Modifier.size(Spacing.lg),
                )
                Text(
                    text = stringResource(R.string.climate_condition_optimal_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral700,
                )
            }
        }

        // 4-Metric Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            MetricTile(
                icon = R.drawable.ic_thermostat,
                label = stringResource(R.string.climate_metric_temp_max),
                value = stringResource(R.string.climate_temp_celsius_value, day.maxTempCelsius),
                modifier = Modifier.weight(1f),
            )
            MetricTile(
                icon = R.drawable.ic_thermostat,
                label = stringResource(R.string.climate_metric_temp_min),
                value = stringResource(R.string.climate_temp_celsius_value, day.minTempCelsius),
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            MetricTile(
                icon = R.drawable.ic_water_drop,
                label = stringResource(R.string.climate_metric_precipitation),
                value = stringResource(R.string.climate_percentage_value, day.precipitationProbability),
                modifier = Modifier.weight(1f),
            )
            MetricTile(
                icon = R.drawable.ic_air,
                label = stringResource(R.string.climate_metric_wind),
                value = stringResource(R.string.climate_wind_speed_value, day.windSpeedKmh),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MetricTile(
    icon: Int,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(Spacing.md))
            .background(Neutral0)
            .padding(horizontal = Spacing.sm, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Box(
            modifier = Modifier
                .size(Spacing.xl)
                .clip(CircleShape)
                .background(Green100),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = Green900,
                modifier = Modifier.size(Spacing.md),
            )
        }
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Neutral600,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Neutral900,
            )
        }
    }
}

@Composable
private fun WeeklyForecastCarousel(
    forecasts: List<DayForecast>,
    selectedIndex: Int,
    onSelectDay: (Int) -> Unit,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        itemsIndexed(forecasts) { index, day ->
            val isSelected = index == selectedIndex
            DayCarouselCard(
                day = day,
                isSelected = isSelected,
                onClick = { onSelectDay(index) },
            )
        }
    }
}

@Composable
private fun DayCarouselCard(
    day: DayForecast,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Green800 else Neutral0,
        animationSpec = tween(200),
        label = "bgColor",
    )
    val textColor = if (isSelected) Neutral0 else Neutral900
    val subTextColor = if (isSelected) Green100 else Neutral600

    val locale = LocalConfiguration.current.locales[0]
    val dayName = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

    Column(
        modifier = Modifier
            .width(68.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(
                width = if (day.isFrostRisk && !isSelected) 1.5.dp else 1.dp,
                color = if (day.isFrostRisk && !isSelected) Terracotta600 else Neutral200,
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = Spacing.sm, horizontal = Spacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = textColor,
        )
        Text(
            text = "${day.date.dayOfMonth}",
            style = MaterialTheme.typography.bodySmall,
            color = subTextColor,
        )

        Icon(
            painter = painterResource(if (day.precipitationProbability > 40.0) R.drawable.ic_water_drop else R.drawable.ic_cloud),
            contentDescription = null,
            tint = if (isSelected) Harvest300 else if (day.isFrostRisk) Terracotta600 else Neutral700,
            modifier = Modifier.size(Spacing.lg),
        )

        Text(
            text = stringResource(R.string.climate_temp_degrees_value, day.maxTempCelsius.toInt()),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = textColor,
        )
        Text(
            text = stringResource(R.string.climate_temp_degrees_value, day.minTempCelsius.toInt()),
            style = MaterialTheme.typography.labelSmall,
            color = subTextColor,
        )
    }
}

@Composable
private fun ThermalTrendCard(
    forecasts: List<DayForecast>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Neutral0)
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_thermostat),
                contentDescription = null,
                tint = Green800,
                modifier = Modifier.size(Spacing.lg),
            )
            Text(
                text = stringResource(R.string.climate_thermal_trend_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Neutral900,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            val globalMin = forecasts.minOfOrNull { it.minTempCelsius } ?: 10.0
            val globalMax = forecasts.maxOfOrNull { it.maxTempCelsius } ?: 35.0
            val span = (globalMax - globalMin).coerceAtLeast(1.0)

            val locale = LocalConfiguration.current.locales[0]
            forecasts.forEach { day ->
                val dayLabel = day.date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
                val leftFraction = ((day.minTempCelsius - globalMin) / span).toFloat().coerceIn(0f, 1f)
                val rightFraction = ((day.maxTempCelsius - globalMin) / span).toFloat().coerceIn(0f, 1f)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    Text(
                        text = "$dayLabel ${day.date.dayOfMonth}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Neutral700,
                        modifier = Modifier.width(52.dp),
                    )

                    Text(
                        text = stringResource(R.string.climate_temp_degrees_value, day.minTempCelsius.toInt()),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Neutral600,
                        modifier = Modifier.width(26.dp),
                        textAlign = TextAlign.End,
                    )

                    // Range Bar
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(Spacing.xs)
                            .clip(RoundedCornerShape(Spacing.xxs))
                            .background(Neutral100),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(rightFraction)
                                .height(Spacing.xs)
                                .clip(RoundedCornerShape(Spacing.xxs))
                                .background(if (day.isFrostRisk) Terracotta600 else Green800),
                        )
                    }

                    Text(
                        text = stringResource(R.string.climate_temp_degrees_value, day.maxTempCelsius.toInt()),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Neutral900,
                        modifier = Modifier.width(26.dp),
                    )
                }
            }
        }
    }
}
