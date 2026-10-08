package pe.edu.upc.viora.features.phenology.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.floor
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.EditorialHeadline
import pe.edu.upc.viora.core.designsystem.component.VioraTabBarDefaults
import pe.edu.upc.viora.core.designsystem.component.VioraVoice
import pe.edu.upc.viora.core.designsystem.theme.Green100
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green700
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.RobotoFamily
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.phenology.domain.entity.ChillProjection
import pe.edu.upc.viora.features.phenology.domain.entity.ThermalAnomaly
import pe.edu.upc.viora.features.phenology.domain.entity.WinterSeasonState
import pe.edu.upc.viora.features.phenology.presentation.state.WinterChillUiState
import pe.edu.upc.viora.features.phenology.presentation.viewmodel.WinterChillViewModel
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.LotSection
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.LotSectionsSheet
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.CircleIconButton
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.PrimaryPillButton

/**
 * Screen P80: "Frío invernal" (US22)
 * Dynamic winter chill tracking based on Erez model with 4 adaptive operational states.
 * Aligned 100% with Figma node 389:80486.
 */
@Composable
fun WinterChillScreen(
    onBack: () -> Unit,
    onHarvestHistory: () -> Unit = {},
    onSensors: () -> Unit = {},
    onClimate: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: WinterChillViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showWhySheet by rememberSaveable { mutableStateOf(false) }
    var showMore by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral100)
            .statusBarsPadding(),
    ) {
        val (plotName, varietyName) = when (val s = state) {
            is WinterChillUiState.Content -> s.plotName to s.varietyName
            is WinterChillUiState.Empty -> s.plotName to s.varietyName
            else -> "" to ""
        }

        // Top Bar
        TopBar(
            plotName = plotName,
            varietyName = varietyName,
            onBack = onBack,
            onMore = { showMore = true },
        )

        when (val s = state) {
            WinterChillUiState.Loading -> {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green900)
                }
            }
            is WinterChillUiState.Error -> {
                MessageBody(
                    message = stringResource(s.error.messageRes()),
                    onRetry = viewModel::refresh,
                    modifier = Modifier.weight(1f),
                )
            }
            is WinterChillUiState.Empty -> {
                MessageBody(
                    message = stringResource(R.string.winter_chill_empty_state),
                    onRetry = viewModel::refresh,
                    modifier = Modifier.weight(1f),
                )
            }
            is WinterChillUiState.Content -> {
                ContentBody(
                    state = s,
                    onOpenWhy = { showWhySheet = true },
                    onClimate = onClimate,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    if (showWhySheet) {
        WhyCountChillSheet(
            onDismiss = { showWhySheet = false },
        )
    }

    val sheetPlot = when (val s = state) {
        is WinterChillUiState.Content -> s.plotName to s.varietyName
        is WinterChillUiState.Empty -> s.plotName to s.varietyName
        else -> null
    }
    if (showMore && sheetPlot != null) {
        LotSectionsSheet(
            plotName = sheetPlot.first,
            summary = stringResource(
                R.string.plot_options_subtitle,
                sheetPlot.second.ifBlank { stringResource(R.string.winter_chill_no_value) },
                stringResource(R.string.winter_chill_no_value),
                stringResource(R.string.winter_chill_no_value),
            ),
            current = LotSection.WINTER_CHILL,
            onSelect = { section ->
                showMore = false
                when (section) {
                    LotSection.HARVEST -> onHarvestHistory()
                    LotSection.SENSORS -> onSensors()
                    LotSection.CLIMATE -> onClimate()
                    else -> Unit
                }
            },
            onDismiss = { showMore = false },
        )
    }
}

@Composable
private fun TopBar(
    plotName: String,
    varietyName: String,
    onBack: () -> Unit,
    onMore: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleIconButton(
            icon = R.drawable.ic_arrow_back,
            contentDescription = stringResource(R.string.action_back),
            onClick = onBack,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        ) {
            Text(
                text = plotName.ifBlank { stringResource(R.string.winter_chill_title) },
                style = MaterialTheme.typography.titleMedium,
                color = Neutral900,
                maxLines = 1,
            )
            Text(
                text = if (varietyName.isBlank()) {
                    stringResource(R.string.winter_chill_title)
                } else {
                    stringResource(R.string.winter_chill_subtitle, varietyName)
                },
                style = MaterialTheme.typography.labelSmall,
                color = Neutral600,
                maxLines = 1,
            )
        }

        CircleIconButton(
            icon = R.drawable.ic_more_vert,
            contentDescription = stringResource(R.string.plot_menu_more),
            onClick = onMore,
        )
    }
}

@Composable
private fun ContentBody(
    state: WinterChillUiState.Content,
    onOpenWhy: () -> Unit,
    onClimate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (lead, emphasis) = when (state.seasonState) {
        WinterSeasonState.ACCUMULATING -> Pair(
            stringResource(R.string.winter_chill_headline_accumulating_lead),
            stringResource(R.string.winter_chill_headline_accumulating_emphasis),
        )
        WinterSeasonState.CHILL_HALTED -> Pair(
            stringResource(R.string.winter_chill_headline_halted_lead),
            stringResource(R.string.winter_chill_headline_halted_emphasis),
        )
        WinterSeasonState.COMPLETED -> Pair(
            stringResource(R.string.winter_chill_headline_completed_lead),
            stringResource(R.string.winter_chill_headline_completed_emphasis),
        )
        WinterSeasonState.OFF_SEASON -> Pair(
            stringResource(R.string.winter_chill_headline_off_season_lead),
            stringResource(R.string.winter_chill_headline_off_season_emphasis),
        )
    }

    // Every sentence is backed by the data: the comparison with last winter only appears when both exist.
    val difference = state.differenceWithPreviousWinter
    val voiceText = when (state.seasonState) {
        WinterSeasonState.ACCUMULATING -> when {
            difference == null -> stringResource(R.string.winter_chill_voice_accumulating)
            difference >= 0.0 -> stringResource(R.string.winter_chill_voice_accumulating_ahead)
            else -> stringResource(R.string.winter_chill_voice_accumulating_behind)
        }
        WinterSeasonState.CHILL_HALTED -> stringResource(R.string.winter_chill_voice_halted)
        WinterSeasonState.COMPLETED -> stringResource(R.string.winter_chill_voice_completed)
        WinterSeasonState.OFF_SEASON -> if (state.tracker.isCompleted) {
            stringResource(R.string.winter_chill_voice_off_season)
        } else {
            stringResource(R.string.winter_chill_voice_off_season_incomplete)
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Editorial Headline in Newsreader 44sp
        EditorialHeadline(
            lead = lead,
            emphasis = emphasis,
        )

        // Hero Section ("Frío · héroe" in slate gray 32dp)
        HeroSection(
            state = state,
            onOpenWhy = onOpenWhy,
        )

        // Voz de Viora brand component
        VioraVoice(text = voiceText)

        // Asymmetric Metric Cards (Projection tall card + Warm days & ENSO stacked cards)
        AsymmetricMetricsRow(
            state = state,
            onEnsoClick = onClimate,
        )

        // Cumulative Curve Chart (Canvas with today badge and projected point)
        ChillCurveChart(
            curvePoints = state.tracker.curvePoints,
            threshold = state.tracker.thresholdPortions,
            seasonState = state.seasonState,
            currentWarmStreakDays = state.tracker.currentWarmStreakDays,
            completionDate = state.tracker.completionDate,
            projectedCompletionDate = state.tracker.projectedCompletionDate,
            varietyName = state.varietyName,
        )

        Spacer(modifier = Modifier.height(VioraTabBarDefaults.ContentBottomPadding + 8.dp))
    }
}

@Composable
private fun HeroSection(
    state: WinterChillUiState.Content,
    onOpenWhy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val eyebrowText = if (state.seasonState == WinterSeasonState.OFF_SEASON) {
        stringResource(R.string.winter_chill_hero_eyebrow_off_season)
    } else {
        stringResource(R.string.winter_chill_hero_eyebrow)
    }

    val subtitleText = when (state.seasonState) {
        WinterSeasonState.ACCUMULATING -> stringResource(
            R.string.winter_chill_hero_sub_accumulating,
            state.portionsRemaining,
        )
        WinterSeasonState.CHILL_HALTED -> stringResource(
            R.string.winter_chill_hero_sub_halted,
            state.tracker.currentWarmStreakDays,
        )
        WinterSeasonState.COMPLETED -> stringResource(
            R.string.winter_chill_hero_sub_completed,
        )
        WinterSeasonState.OFF_SEASON -> {
            val completion = state.tracker.completionDate
            if (completion != null) {
                val locale = LocalConfiguration.current.locales[0]
                val datePattern = stringResource(R.string.winter_chill_pattern_day)
                val dateStr = DateTimeFormatter.ofPattern(datePattern, locale).format(completion)
                stringResource(R.string.winter_chill_hero_sub_off_season, dateStr)
            } else {
                stringResource(R.string.winter_chill_hero_sub_off_season_incomplete, state.thresholdPortions)
            }
        }
    }

    val timelineProgress = state.seasonProgress

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Green900)
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Row 1: Eyebrow + Pill button ¿Qué es?
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Green200),
                )
                Text(
                    text = eyebrowText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = RobotoFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                    ),
                    color = Color.White.copy(alpha = 0.85f),
                )
            }

            // ¿Qué es? pill
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f))
                    .clickable(role = Role.Button, onClick = onOpenWhy)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    text = stringResource(R.string.winter_chill_hero_what_is),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = RobotoFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                    ),
                    color = Color.White,
                )
            }
        }

        // Row 2: Big number "24 de 30"
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = state.accumulatedPortions.toString(),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = NewsreaderFamily,
                    fontSize = 92.sp,
                    lineHeight = 92.sp,
                ),
                color = Color.White,
            )
            Text(
                text = stringResource(R.string.winter_chill_hero_of_threshold, state.thresholdPortions),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = NewsreaderFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 30.sp,
                    lineHeight = 36.sp,
                ),
                color = Green200,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }

        // Row 3: Subtitle
        Text(
            text = subtitleText,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = RobotoFamily,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
            color = Color.White.copy(alpha = 0.88f),
        )

        // Row 4: 3x10 snowflake grid
        ChillSnowflakeGrid(
            accumulated = state.accumulatedPortions,
            threshold = state.thresholdPortions,
            state = state.seasonState,
        )

        // Row 5: Timeline bar
        ChillTimelineBar(progress = timelineProgress)
    }
}

@Composable
private fun ChillTimelineBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            // Track background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
            )

            // Active track fill
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress.coerceIn(0.05f, 1f))
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Harvest300),
            )

            // Circle thumb
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress.coerceIn(0.05f, 1f)),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Neutral50)
                        .border(2.dp, Harvest300, CircleShape),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.winter_chill_timeline_start),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = RobotoFamily,
                    fontSize = 11.sp,
                ),
                color = Color.White.copy(alpha = 0.75f),
            )
            Text(
                text = stringResource(R.string.winter_chill_timeline_end),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = RobotoFamily,
                    fontSize = 11.sp,
                ),
                color = Color.White.copy(alpha = 0.75f),
            )
        }
    }
}

@Composable
private fun AsymmetricMetricsRow(
    state: WinterChillUiState.Content,
    onEnsoClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tracker = state.tracker
    val locale = LocalConfiguration.current.locales[0]
    val completion = tracker.completionDate
    val projected = tracker.projectedCompletionDate.takeIf { tracker.projection == ChillProjection.PROJECTED }

    // The big date is the real completion day, or the projected one; never a placeholder date.
    val cardDate = completion ?: projected
    val dayStr = cardDate?.dayOfMonth?.toString() ?: stringResource(R.string.winter_chill_no_value)
    val monthStr = cardDate?.let { formatMonthShort(it, locale) }.orEmpty()

    val projHeader = when {
        completion != null -> stringResource(R.string.winter_chill_card_completed_header)
        state.seasonState == WinterSeasonState.OFF_SEASON -> stringResource(R.string.winter_chill_card_closed_header)
        else -> stringResource(R.string.winter_chill_card_proj_header)
    }

    val projSub = when {
        completion != null -> {
            val daysAhead = state.daysAheadOfPreviousWinter
            when {
                daysAhead != null && daysAhead > 0 -> stringResource(R.string.winter_chill_card_completed_sub, daysAhead.toInt())
                daysAhead != null && daysAhead < 0 -> stringResource(R.string.winter_chill_card_completed_sub_later, -daysAhead.toInt())
                daysAhead != null -> stringResource(R.string.winter_chill_card_completed_sub_same)
                tracker.previousSeason != null -> stringResource(R.string.winter_chill_card_completed_sub_first)
                else -> stringResource(R.string.winter_chill_card_off_season_sub, daysBeforeBudbreak(completion))
            }
        }
        state.seasonState == WinterSeasonState.OFF_SEASON ->
            stringResource(R.string.winter_chill_card_closed_sub, state.thresholdPortions)
        projected != null -> stringResource(
            R.string.winter_chill_card_proj_sub,
            state.thresholdPortions,
            daysBeforeBudbreak(projected),
        )
        tracker.projection == ChillProjection.NOT_REACHABLE_IN_SEASON ->
            stringResource(R.string.winter_chill_card_not_reachable_sub, state.thresholdPortions)
        else -> stringResource(R.string.winter_chill_card_insufficient_sub)
    }

    val previous = tracker.previousSeason
    val previousCompletion = previous?.completionDate
    val bottomChipText = when {
        state.seasonState == WinterSeasonState.OFF_SEASON ->
            stringResource(R.string.winter_chill_card_total_portions, state.accumulatedPortions)
        previousCompletion != null ->
            stringResource(R.string.winter_chill_card_past_winter_date, formatDateShort(previousCompletion, locale))
        previous != null ->
            stringResource(R.string.winter_chill_card_past_winter_portions, floor(previous.accumulatedPortions).toInt())
        else -> null
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Left Column: Tall projection card (252dp)
        val leftCardBg = if (tracker.isCompleted) Green100 else Green200
        Column(
            modifier = Modifier
                .weight(1f)
                .height(252.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(leftCardBg)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Top header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = projHeader,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = RobotoFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                        ),
                        color = Green800,
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar_month),
                        contentDescription = null,
                        tint = Green800,
                        modifier = Modifier.size(16.dp),
                    )
                }

                // Day + Month
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = dayStr,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = NewsreaderFamily,
                            fontSize = 80.sp,
                            lineHeight = 82.sp,
                        ),
                        color = Green900,
                    )
                    Text(
                        text = monthStr,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = NewsreaderFamily,
                            fontStyle = FontStyle.Italic,
                            fontSize = 30.sp,
                            lineHeight = 34.sp,
                        ),
                        color = Green900,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                }

                // Subtitle explanation
                Text(
                    text = projSub,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = RobotoFamily,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    ),
                    color = Green800,
                )
            }

            // Bottom chip (hidden when the previous winter could not be read)
            if (bottomChipText != null) Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Neutral0)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(Green700),
                    )
                    Text(
                        text = bottomChipText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = RobotoFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                        ),
                        color = Green900,
                    )
                }
            }
        }

        // Right Column: Stacked 2 cards
        Column(
            modifier = Modifier
                .weight(1f)
                .height(252.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Card 1: Warm days (> 24 °C)
            // While halted the card counts the current run of warm days; otherwise all the warm days of the season.
            val warmValue = if (state.seasonState == WinterSeasonState.CHILL_HALTED) {
                tracker.currentWarmStreakDays
            } else {
                tracker.daysAbove24Celsius
            }
            val warmSub = when (state.seasonState) {
                WinterSeasonState.OFF_SEASON -> stringResource(R.string.winter_chill_card_warm_sub_off_season)
                WinterSeasonState.CHILL_HALTED -> stringResource(R.string.winter_chill_card_warm_sub_halted)
                else -> stringResource(R.string.winter_chill_card_warm_sub_accumulating)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Neutral0)
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = warmValue.toString(),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = NewsreaderFamily,
                            fontSize = 48.sp,
                            lineHeight = 50.sp,
                        ),
                        color = Neutral900,
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_thermostat),
                        contentDescription = null,
                        tint = Neutral700,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.winter_chill_card_warm_title),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = RobotoFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                        ),
                        color = Neutral900,
                    )
                    Text(
                        text = warmSub,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = RobotoFamily,
                            fontSize = 11.sp,
                        ),
                        color = Neutral600,
                    )
                }
            }

            // Card 2: warm winter (US23). A local thermal rule from the plot's temperatures, not the El Niño index.
            val ensoTitle = when (tracker.thermalAnomaly) {
                ThermalAnomaly.ACTIVE -> stringResource(R.string.winter_chill_card_warm_spell_active)
                ThermalAnomaly.RECORDED -> stringResource(R.string.winter_chill_card_warm_spell_recorded)
                ThermalAnomaly.NONE -> stringResource(R.string.winter_chill_card_warm_spell_none)
            }
            val ensoSub = when (tracker.thermalAnomaly) {
                ThermalAnomaly.ACTIVE -> stringResource(R.string.winter_chill_card_warm_spell_sub_active)
                ThermalAnomaly.RECORDED -> stringResource(
                    R.string.winter_chill_card_warm_spell_sub_recorded,
                    tracker.longestWarmStreakDays,
                )
                ThermalAnomaly.NONE -> stringResource(R.string.winter_chill_card_warm_spell_sub_none)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Harvest100)
                    .clickable(role = Role.Button, onClick = onEnsoClick)
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = ensoTitle,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = NewsreaderFamily,
                            fontSize = 28.sp,
                            lineHeight = 30.sp,
                        ),
                        color = Neutral900,
                    )
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Neutral0),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_forward),
                            contentDescription = null,
                            tint = Neutral900,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = stringResource(R.string.winter_chill_card_warm_spell_title),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = RobotoFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                        ),
                        color = Harvest800,
                    )
                    Text(
                        text = ensoSub,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = RobotoFamily,
                            fontSize = 11.sp,
                        ),
                        color = Harvest800,
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBody(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Neutral700,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        PrimaryPillButton(
            text = stringResource(R.string.harvest_retry),
            onClick = onRetry,
        )
    }
}

/** Days from [date] to August 31, the budbreak that closes the count. */
private fun daysBeforeBudbreak(date: LocalDate): Int =
    ChronoUnit.DAYS.between(date, LocalDate.of(date.year, 8, 31)).coerceAtLeast(0).toInt()

private fun formatDateShort(date: LocalDate, locale: Locale): String =
    DateTimeFormatter.ofPattern("d MMM", locale).format(date).replace(".", "")

private fun formatMonthShort(date: LocalDate, locale: Locale): String =
    DateTimeFormatter.ofPattern("MMM", locale).format(date).replace(".", "")
