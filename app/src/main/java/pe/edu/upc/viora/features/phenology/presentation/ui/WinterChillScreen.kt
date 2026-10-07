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
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.EditorialHeadline
import pe.edu.upc.viora.core.designsystem.component.VioraVoice
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.RobotoFamily
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.phenology.domain.entity.EnsoRiskLevel
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
        val content = state as? WinterChillUiState.Content

        // Top Bar
        TopBar(
            plotName = content?.plotName.orEmpty(),
            varietyName = content?.varietyName.orEmpty(),
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
                ErrorBody(error = s, onRetry = viewModel::refresh, modifier = Modifier.weight(1f))
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

    val content = state as? WinterChillUiState.Content
    if (showMore && content != null) {
        LotSectionsSheet(
            plotName = content.plotName,
            summary = stringResource(
                R.string.plot_options_subtitle,
                content.varietyName.ifBlank { stringResource(R.string.variety_sevillana) },
                "—",
                "—",
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
                text = stringResource(R.string.winter_chill_subtitle, varietyName.ifBlank { stringResource(R.string.variety_sevillana) }),
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

    val voiceText = when (state.seasonState) {
        WinterSeasonState.ACCUMULATING -> stringResource(R.string.winter_chill_voice_accumulating)
        WinterSeasonState.CHILL_HALTED -> stringResource(R.string.winter_chill_voice_halted)
        WinterSeasonState.COMPLETED -> stringResource(R.string.winter_chill_voice_completed)
        WinterSeasonState.OFF_SEASON -> stringResource(R.string.winter_chill_voice_off_season)
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
            curvePoints = state.curvePoints,
            threshold = state.thresholdPortions.toDouble(),
            currentPortions = state.accumulatedPortions.toDouble(),
            seasonState = state.seasonState,
            daysAbove24Celsius = state.daysAbove24Celsius,
            projectedCompletionDate = state.projectedCompletionDate,
            varietyName = state.varietyName.ifBlank { stringResource(R.string.variety_sevillana) },
        )

        Spacer(modifier = Modifier.height(16.dp))
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
            state.daysAbove24Celsius,
        )
        WinterSeasonState.COMPLETED -> stringResource(
            R.string.winter_chill_hero_sub_completed,
        )
        WinterSeasonState.OFF_SEASON -> {
            val datePattern = stringResource(R.string.winter_chill_pattern_day)
            val date = state.projectedCompletionDate ?: LocalDate.of(LocalDate.now().year, 8, 18)
            val dateStr = DateTimeFormatter.ofPattern(datePattern, Locale.getDefault()).format(date)
            stringResource(R.string.winter_chill_hero_sub_off_season, dateStr)
        }
    }

    val timelineProgress = when (state.seasonState) {
        WinterSeasonState.COMPLETED, WinterSeasonState.OFF_SEASON -> 1f
        WinterSeasonState.CHILL_HALTED -> 0.65f
        WinterSeasonState.ACCUMULATING -> 0.65f
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF727272))
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
                        .background(Color.White.copy(alpha = 0.8f)),
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
                color = Color.White.copy(alpha = 0.85f),
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
                    .background(Color(0xFFE6E6E6)),
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
                        .background(Color(0xFFF9F6F1))
                        .border(2.dp, Color(0xFFE6E6E6), CircleShape),
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
    val projDate = state.projectedCompletionDate ?: LocalDate.of(LocalDate.now().year, 8, 4)
    val dayStr = projDate.dayOfMonth.toString()
    val monthStr = formatMonthShort(projDate)

    val projHeader = if (state.isCompleted || state.seasonState == WinterSeasonState.OFF_SEASON) {
        stringResource(R.string.winter_chill_card_completed_header)
    } else {
        stringResource(R.string.winter_chill_card_proj_header)
    }

    val daysBeforeBudbreak = ChronoUnit.DAYS.between(
        projDate,
        LocalDate.of(projDate.year, 8, 31),
    ).coerceAtLeast(0).toInt()

    val projSub = when (state.seasonState) {
        WinterSeasonState.COMPLETED -> stringResource(R.string.winter_chill_card_completed_sub, 14)
        WinterSeasonState.OFF_SEASON -> stringResource(R.string.winter_chill_card_off_season_sub, daysBeforeBudbreak)
        WinterSeasonState.CHILL_HALTED -> stringResource(R.string.winter_chill_card_risk_sub)
        WinterSeasonState.ACCUMULATING -> stringResource(
            R.string.winter_chill_card_proj_sub,
            state.thresholdPortions,
            daysBeforeBudbreak,
        )
    }

    val bottomChipText = if (state.seasonState == WinterSeasonState.OFF_SEASON) {
        stringResource(R.string.winter_chill_card_total_portions, state.accumulatedPortions)
    } else {
        val fallbackPrevDate = if (state.seasonState == WinterSeasonState.CHILL_HALTED) {
            LocalDate.of(LocalDate.now().year - 1, 8, 4)
        } else {
            LocalDate.of(LocalDate.now().year - 1, 8, 18)
        }
        val prevDateStr = formatDateShort(state.previousWinterCompletionDate ?: fallbackPrevDate)
        stringResource(R.string.winter_chill_card_past_winter_date, prevDateStr)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Left Column: Tall projection card (252dp)
        val leftCardBg = if (state.isCompleted) Color(0xFFBFBFBF) else Color(0xFFC7C7CC)
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
                        color = Neutral900,
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar_month),
                        contentDescription = null,
                        tint = Neutral900,
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
                        color = Color(0xFF727272),
                    )
                    Text(
                        text = monthStr,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = NewsreaderFamily,
                            fontStyle = FontStyle.Italic,
                            fontSize = 30.sp,
                            lineHeight = 34.sp,
                        ),
                        color = Color(0xFF727272),
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
                    color = Neutral900,
                )
            }

            // Bottom chip
            Box(
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
                            .background(Neutral700),
                    )
                    Text(
                        text = bottomChipText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = RobotoFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                        ),
                        color = Neutral900,
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
                        text = state.daysAbove24Celsius.toString(),
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

            // Card 2: El Niño Costero
            val isEnsoActive = state.ensoRisk != EnsoRiskLevel.NEUTRAL
            val ensoTitle = if (isEnsoActive) {
                stringResource(R.string.winter_chill_card_enso_active)
            } else {
                stringResource(R.string.winter_chill_card_enso_neutral)
            }
            val ensoSub = when {
                state.seasonState == WinterSeasonState.OFF_SEASON -> stringResource(R.string.winter_chill_card_enso_sub_off_season)
                isEnsoActive -> stringResource(R.string.winter_chill_card_enso_sub_active)
                else -> stringResource(R.string.winter_chill_card_enso_sub_neutral)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFFAEAEB2))
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
                        text = stringResource(R.string.winter_chill_card_enso_title),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = RobotoFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                        ),
                        color = Neutral900,
                    )
                    Text(
                        text = ensoSub,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = RobotoFamily,
                            fontSize = 11.sp,
                        ),
                        color = Neutral600,
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorBody(
    error: WinterChillUiState.Error,
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
            text = stringResource(error.error.messageRes()),
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

private fun formatDateShort(date: LocalDate, locale: Locale = Locale.getDefault()): String =
    DateTimeFormatter.ofPattern("d MMM", locale).format(date).replace(".", "")

private fun formatMonthShort(date: LocalDate, locale: Locale = Locale.getDefault()): String =
    DateTimeFormatter.ofPattern("MMM", locale).format(date).replace(".", "")
