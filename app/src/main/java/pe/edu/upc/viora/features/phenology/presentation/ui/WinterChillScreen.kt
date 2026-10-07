package pe.edu.upc.viora.features.phenology.presentation.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Harvest400
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.phenology.domain.entity.EnsoRiskLevel
import pe.edu.upc.viora.features.phenology.domain.entity.WinterSeasonState
import pe.edu.upc.viora.features.phenology.presentation.state.WinterChillUiState
import pe.edu.upc.viora.features.phenology.presentation.viewmodel.WinterChillViewModel
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.CircleIconButton
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.PrimaryPillButton

/**
 * Screen P80: "Frío invernal" (US22)
 * Dynamic winter chill tracking based on Erez model with 4 adaptive operational states.
 */
@Composable
fun WinterChillScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WinterChillViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showWhySheet by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral50)
            .statusBarsPadding(),
    ) {
        val content = state as? WinterChillUiState.Content

        // Top Bar
        TopBar(
            plotName = content?.plotName.orEmpty(),
            varietyName = content?.varietyName.orEmpty(),
            onBack = onBack,
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
}

@Composable
private fun TopBar(
    plotName: String,
    varietyName: String,
    onBack: () -> Unit,
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
                text = stringResource(R.string.winter_chill_subtitle, varietyName.ifBlank { "Sevillana" }),
                style = MaterialTheme.typography.labelSmall,
                color = Neutral600,
                maxLines = 1,
            )
        }

        // Balance space for symmetry
        Spacer(modifier = Modifier.size(44.dp))
    }
}

@Composable
private fun ContentBody(
    state: WinterChillUiState.Content,
    onOpenWhy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Hero Section
        HeroSection(state = state)

        // 30-Snowflake Grid
        ChillSnowflakeGrid(
            accumulated = state.accumulatedPortions,
            threshold = state.thresholdPortions,
            state = state.seasonState,
        )

        // Timeline Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Neutral0)
                .border(1.dp, Neutral200, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_calendar_month),
                contentDescription = null,
                tint = Neutral600,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = stringResource(R.string.winter_chill_timeline),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral700,
            )
        }

        // Metrics Summary Cards
        MetricsCardsRow(state = state)

        // Cumulative Curve Chart (Canvas)
        ChillCurveChart(
            curvePoints = state.curvePoints,
            threshold = state.thresholdPortions.toDouble(),
            currentPortions = state.accumulatedPortions.toDouble(),
        )

        // Educational Bottom Sheet Trigger Button
        WhyChillButton(onClick = onOpenWhy)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HeroSection(state: WinterChillUiState.Content) {
    val statusText = when (state.seasonState) {
        WinterSeasonState.ACCUMULATING -> stringResource(R.string.winter_chill_status_accumulating)
        WinterSeasonState.CHILL_HALTED -> stringResource(R.string.winter_chill_status_halted)
        WinterSeasonState.COMPLETED -> stringResource(R.string.winter_chill_status_completed)
        WinterSeasonState.OFF_SEASON -> stringResource(R.string.winter_chill_status_off_season)
    }

    val (statusBg, statusFg) = when (state.seasonState) {
        WinterSeasonState.CHILL_HALTED -> Pair(Terracotta100, Terracotta700)
        WinterSeasonState.COMPLETED -> Pair(Harvest100, Green800)
        WinterSeasonState.OFF_SEASON -> Pair(Neutral200, Neutral700)
        else -> Pair(Harvest100, Harvest800)
    }

    val caption = when (state.seasonState) {
        WinterSeasonState.ACCUMULATING -> stringResource(R.string.winter_chill_portions_caption_accumulating, state.portionsRemaining)
        WinterSeasonState.CHILL_HALTED -> stringResource(R.string.winter_chill_portions_caption_halted)
        WinterSeasonState.COMPLETED -> stringResource(R.string.winter_chill_portions_caption_completed)
        WinterSeasonState.OFF_SEASON -> stringResource(R.string.winter_chill_portions_caption_off_season)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Neutral0)
            .border(1.dp, Neutral200, RoundedCornerShape(24.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Status Pill
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(statusBg)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = statusFg,
                )
            }

            // Olive Dormancy / Bloom Icon
            val illRes = if (state.isCompleted) R.drawable.ill_phase_bloom else R.drawable.ill_phase_dormancy
            Image(
                painter = painterResource(illRes),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
            )
        }

        // Big Numbers: e.g. "24 de 30"
        Text(
            text = stringResource(R.string.winter_chill_portions_hero, state.accumulatedPortions, state.thresholdPortions),
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 38.sp, lineHeight = 42.sp),
            color = Neutral900,
        )

        // Editorial Explanatory Caption
        Text(
            text = caption,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
            color = Neutral700,
        )
    }
}

@Composable
private fun MetricsCardsRow(state: WinterChillUiState.Content) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Card 1: El Niño Costero
        val isEnsoActive = state.ensoRisk != EnsoRiskLevel.NEUTRAL
        MetricCard(
            title = stringResource(R.string.winter_chill_enso_title),
            value = if (isEnsoActive) stringResource(R.string.winter_chill_enso_active) else stringResource(R.string.winter_chill_enso_neutral),
            indicatorColor = if (isEnsoActive) Terracotta500 else Green800,
            modifier = Modifier.weight(1f),
        )

        // Card 2: Calor en invierno (días > 24 °C)
        MetricCard(
            title = stringResource(R.string.winter_chill_warm_days_title),
            value = stringResource(R.string.winter_chill_warm_days_value, state.daysAbove24Celsius),
            indicatorColor = if (state.daysAbove24Celsius > 2) Terracotta500 else Neutral600,
            modifier = Modifier.weight(1f),
        )

        // Card 3: Proyección
        val projDateStr = state.projectedCompletionDate?.let { formatDateShort(it) } ?: "—"
        val prevDateStr = state.previousWinterCompletionDate?.let { formatDateShort(it) }
            ?: stringResource(R.string.winter_chill_prev_winter_default_date)
        MetricCard(
            title = stringResource(R.string.winter_chill_projection_title),
            value = projDateStr,
            subtitle = stringResource(R.string.winter_chill_projection_vs_last, prevDateStr),
            indicatorColor = Green800,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String? = null,
    indicatorColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Neutral0)
            .border(1.dp, Neutral200, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(indicatorColor),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = Neutral600,
                maxLines = 1,
            )
        }

        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
            color = Neutral900,
            maxLines = 1,
        )

        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Neutral600,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun WhyChillButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Neutral0)
            .border(1.dp, Neutral200, RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Harvest100),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_info),
                    contentDescription = null,
                    tint = Harvest800,
                    modifier = Modifier.size(18.dp),
                )
            }
            Text(
                text = stringResource(R.string.winter_chill_why_button),
                style = MaterialTheme.typography.titleMedium,
                color = Neutral900,
            )
        }

        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = Neutral600,
            modifier = Modifier.size(20.dp),
        )
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

