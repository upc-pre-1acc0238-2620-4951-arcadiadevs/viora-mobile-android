package pe.edu.upc.viora.features.telemetry.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.os.ConfigurationCompat
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.PillShape
import pe.edu.upc.viora.core.designsystem.theme.RobotoFamily
import pe.edu.upc.viora.features.telemetry.domain.entity.AlertsSummary
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentStatus
import pe.edu.upc.viora.features.telemetry.presentation.state.AlertsFilter
import pe.edu.upc.viora.features.telemetry.presentation.state.AlertsUiState
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.AlertCard
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.AlertFilterCapsules
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.NormalizedAlertRow
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import pe.edu.upc.viora.features.telemetry.presentation.viewmodel.AlertsViewModel

@Composable
fun AlertsCenterScreen(
    onBack: () -> Unit,
    onOpenDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlertsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral50)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        when (val state = uiState) {
            is AlertsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Green900)
                }
            }

            is AlertsUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.alerts_error_title),
                        fontFamily = NewsreaderFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Neutral900,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.alerts_error_subtitle),
                        fontFamily = RobotoFamily,
                        fontSize = 14.sp,
                        color = Neutral600,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(Green900)
                            .clickable { viewModel.refresh() }
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.alerts_action_retry),
                            fontFamily = RobotoFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Neutral0,
                        )
                    }
                }
            }

            is AlertsUiState.Empty -> {
                AlertsCenterContent(
                    summary = state.summary,
                    activeFilter = state.activeFilter,
                    incidents = emptyList(),
                    affectedPlotsSummary = "",
                    affectedPlotNames = emptyList(),
                    latestTriggeredAt = null,
                    onBack = onBack,
                    onOpenDetail = onOpenDetail,
                    onFilterSelected = { viewModel.setFilter(it) },
                )
            }

            is AlertsUiState.Content -> {
                AlertsCenterContent(
                    summary = state.summary,
                    activeFilter = state.activeFilter,
                    incidents = state.incidents,
                    affectedPlotsSummary = state.affectedPlotsSummary,
                    affectedPlotNames = state.affectedPlotNames,
                    latestTriggeredAt = state.latestTriggeredAt,
                    onBack = onBack,
                    onOpenDetail = onOpenDetail,
                    onFilterSelected = { viewModel.setFilter(it) },
                )
            }
        }
    }
}

@Composable
private fun AlertsCenterContent(
    summary: AlertsSummary,
    activeFilter: AlertsFilter,
    incidents: List<pe.edu.upc.viora.features.telemetry.domain.entity.AgroclimaticIncident>,
    affectedPlotsSummary: String,
    affectedPlotNames: List<String> = emptyList(),
    latestTriggeredAt: String? = null,
    onBack: () -> Unit,
    onOpenDetail: (String) -> Unit,
    onFilterSelected: (AlertsFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    val activeIncidents = incidents.filter {
        it.status != IncidentStatus.NORMALIZED
    }
    val normalizedIncidents = incidents.filter {
        it.status == IncidentStatus.NORMALIZED
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp),
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Top Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Neutral0)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.alerts_nav_back),
                    tint = Neutral900,
                    modifier = Modifier.size(24.dp),
                )
            }

            Text(
                text = stringResource(R.string.alerts_title),
                fontFamily = RobotoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Neutral900,
            )

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Neutral0)
                    .clickable { /* Preferences */ },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = stringResource(R.string.alerts_nav_settings),
                    tint = Neutral900,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Hero Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = summary.activeCount.toString(),
                fontFamily = NewsreaderFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 112.sp,
                color = Neutral900,
                lineHeight = 104.sp,
                textAlign = TextAlign.Center,
            )

            Text(
                text = stringResource(R.string.alerts_hero_active_label),
                fontFamily = NewsreaderFamily,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Normal,
                fontSize = 26.sp,
                color = Neutral900,
                lineHeight = 30.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(6.dp))

            val conjunction = stringResource(R.string.alerts_plots_conjunction)
            val affectedPlotsText = when {
                affectedPlotNames.size > 1 -> affectedPlotNames.dropLast(1).joinToString(", ") + conjunction + affectedPlotNames.last()
                affectedPlotNames.size == 1 -> affectedPlotNames.first()
                affectedPlotsSummary.isNotBlank() -> affectedPlotsSummary
                else -> ""
            }

            val configuration = LocalConfiguration.current
            val locale = ConfigurationCompat.getLocales(configuration)[0] ?: Locale.getDefault()
            val formattedTime = latestTriggeredAt?.let { formatAlertTriggerTime(it, locale) }

            val contextualSubtitle = when {
                affectedPlotsText.isNotBlank() && !formattedTime.isNullOrBlank() -> {
                    stringResource(R.string.alerts_hero_affected_plots_with_time, affectedPlotsText, formattedTime)
                }
                affectedPlotsText.isNotBlank() -> {
                    stringResource(R.string.alerts_hero_affected_plots, affectedPlotsText)
                }
                else -> {
                    stringResource(R.string.alerts_hero_no_affected_plots)
                }
            }

            Text(
                text = contextualSubtitle,
                fontFamily = RobotoFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = Neutral600,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Summary / Filter Capsules
        AlertFilterCapsules(
            summary = summary,
            activeFilter = activeFilter,
            onFilterSelected = onFilterSelected,
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Active Alerts Section
        if (activeIncidents.isNotEmpty()) {
            Text(
                text = stringResource(R.string.alerts_section_today),
                fontFamily = RobotoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Neutral600,
                letterSpacing = 1.sp,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                activeIncidents.forEach { incident ->
                    AlertCard(
                        incident = incident,
                        onOpenDetail = onOpenDetail,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        // Normalized History Section
        if (normalizedIncidents.isNotEmpty()) {
            Text(
                text = stringResource(R.string.alerts_section_normalized_recent),
                fontFamily = RobotoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Neutral600,
                letterSpacing = 1.sp,
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                normalizedIncidents.forEach { incident ->
                    NormalizedAlertRow(
                        incident = incident,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        if (activeIncidents.isEmpty() && normalizedIncidents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PillShape)
                    .background(Neutral0)
                    .padding(vertical = 28.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.alerts_empty_filtered),
                    fontFamily = RobotoFamily,
                    fontSize = 14.sp,
                    color = Neutral600,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

private fun formatAlertTriggerTime(triggerIso: String, locale: Locale): String? {
    return runCatching {
        val instant = Instant.parse(triggerIso)
        val isSpanish = locale.language.equals("es", ignoreCase = true)
        val formatter = DateTimeFormatter.ofPattern("h:mm a", locale)
        var formatted = instant.atZone(ZoneId.systemDefault()).format(formatter)
        if (isSpanish) {
            formatted = formatted
                .replace("AM", "a. m.")
                .replace("PM", "p. m.")
                .replace("am", "a. m.")
                .replace("pm", "p. m.")
        }
        formatted
    }.getOrNull()
}

