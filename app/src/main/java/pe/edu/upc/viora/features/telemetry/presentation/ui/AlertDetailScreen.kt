package pe.edu.upc.viora.features.telemetry.presentation.ui

import android.content.Intent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.PillShape
import pe.edu.upc.viora.core.designsystem.theme.RobotoFamily
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta600
import pe.edu.upc.viora.features.telemetry.domain.entity.IncidentDetail
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentSeverity
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentType
import pe.edu.upc.viora.features.telemetry.presentation.state.AlertDetailUiState
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.MitigationChecklistCard
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.SnoozeBottomSheet
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.WeeklyTrendChart
import pe.edu.upc.viora.features.telemetry.presentation.viewmodel.AlertDetailViewModel

@Composable
fun AlertDetailScreen(
    onBack: () -> Unit,
    onOpenPlot: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlertDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showSnoozeSheet by remember { mutableStateOf(false) }

    val feedbackRollback = stringResource(R.string.alert_feedback_rollback)
    val feedbackSnoozed = stringResource(R.string.alert_feedback_snoozed)
    val feedbackSnoozeError = stringResource(R.string.alert_feedback_snooze_error)

    LaunchedEffect(uiState) {
        val messageKey = (uiState as? AlertDetailUiState.Content)?.userFeedbackMessage
        if (messageKey != null) {
            val message = when (messageKey) {
                "rollback" -> feedbackRollback
                "snoozed" -> feedbackSnoozed
                "snooze_error" -> feedbackSnoozeError
                else -> messageKey
            }
            snackbarHostState.showSnackbar(message)
            viewModel.clearFeedbackMessage()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral50)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        when (val state = uiState) {
            is AlertDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Green900)
                }
            }

            is AlertDetailUiState.Error -> {
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
                            .clickable { viewModel.loadDetail() }
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

            is AlertDetailUiState.Content -> {
                val detail = state.detail
                val scrollState = rememberScrollState()
                val isCritical = detail.severity == IncidentSeverity.CRITICAL
                val timeRangeColor = if (isCritical) Terracotta600 else Harvest800

                val titleText = when (detail.type) {
                    IncidentType.HEAT_WAVE -> stringResource(R.string.alert_detail_title_heat_wave)
                    IncidentType.HYDRIC_STRESS -> stringResource(R.string.alert_detail_title_hydric_stress)
                    IncidentType.FROST_WARNING -> stringResource(R.string.alert_detail_title_frost_warning)
                    IncidentType.UNKNOWN -> stringResource(R.string.alert_detail_title_generic)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 20.dp),
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

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

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Neutral0)
                                .clickable {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Alerta Viora: $titleText en ${detail.plotName}. Valor: ${detail.currentValue} ${detail.unit}.",
                                        )
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, null)
                                    context.startActivity(shareIntent)
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_share),
                                contentDescription = stringResource(R.string.alert_detail_share),
                                tint = Neutral900,
                                modifier = Modifier.size(22.dp),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val datePattern = stringResource(R.string.alert_detail_date_pattern)
                    val timeWindowText = formatAlertDetailDateTime(
                        dateFormatted = detail.dateFormatted,
                        triggeredAt = detail.triggeredAt,
                        timeWindow = detail.timeWindow,
                        weeklyTrend = detail.weeklyTrend,
                        datePattern = datePattern,
                        locale = Locale.getDefault(),
                    )

                    // Header centered (Figma T15)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (timeWindowText.isNotBlank()) {
                            Text(
                                text = timeWindowText,
                                fontFamily = RobotoFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = timeRangeColor,
                                textAlign = TextAlign.Center,
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        Text(
                            text = titleText,
                            fontFamily = NewsreaderFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 40.sp,
                            lineHeight = 44.sp,
                            color = Neutral900,
                            textAlign = TextAlign.Center,
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(if (isCritical) Terracotta500 else Harvest300)
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = if (isCritical) stringResource(R.string.alert_severity_critical) else stringResource(R.string.alert_severity_warning),
                                    fontFamily = RobotoFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (isCritical) Neutral50 else Neutral900,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.wrapContentSize(Alignment.Center),
                                )
                            }

                            if (detail.plotName.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(PillShape)
                                        .background(Neutral0)
                                        .padding(horizontal = 14.dp, vertical = 7.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = detail.plotName,
                                        fontFamily = RobotoFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = Neutral900,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.wrapContentSize(Alignment.Center),
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(Neutral0)
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = stringResource(R.string.alert_detail_rule_chip),
                                    fontFamily = RobotoFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = Neutral900,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.wrapContentSize(Alignment.Center),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    WeeklyTrendChart(
                        weeklyTrend = detail.weeklyTrend,
                        thresholdValue = detail.thresholdValue,
                        unit = detail.unit,
                        severity = detail.severity,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (detail.mitigationSteps.isNotEmpty()) {
                        MitigationChecklistCard(
                            steps = detail.mitigationSteps,
                            onToggleStep = { stepId -> viewModel.completeStep(stepId) },
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    val traceabilityNote = detail.dataSource?.takeIf { it.isNotBlank() }
                        ?: if (detail.plotName.isNotBlank()) {
                            stringResource(R.string.alert_detail_traceability_note_plot, detail.plotName)
                        } else {
                            stringResource(R.string.alert_detail_traceability_note)
                        }

                    Text(
                        text = traceabilityNote,
                        fontFamily = RobotoFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Neutral600,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Neutral0)
                                .clickable { showSnoozeSheet = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_snooze),
                                contentDescription = stringResource(R.string.alert_detail_snooze_action),
                                tint = Neutral900,
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        val buttonText = if (detail.plotName.isNotBlank()) {
                            stringResource(R.string.alert_detail_view_plot, detail.plotName)
                        } else {
                            stringResource(R.string.alert_detail_view_plot_generic)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(PillShape)
                                .background(Green900)
                                .clickable { onOpenPlot(detail.plotId) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = buttonText,
                                fontFamily = RobotoFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Neutral0,
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )

        if (showSnoozeSheet) {
            SnoozeBottomSheet(
                onDismiss = { showSnoozeSheet = false },
                onConfirmSnooze = { hours -> viewModel.postpone(hours) },
            )
        }
    }
}

private fun formatAlertDetailDateTime(
    dateFormatted: String,
    triggeredAt: String,
    timeWindow: String,
    weeklyTrend: List<pe.edu.upc.viora.features.telemetry.domain.entity.WeeklyTrendPoint>,
    datePattern: String,
    locale: Locale = Locale.getDefault(),
): String {
    val rawDateCandidate = dateFormatted.ifBlank { triggeredAt }.ifBlank {
        weeklyTrend.lastOrNull()?.timestamp.orEmpty()
    }

    val parsedDate = if (rawDateCandidate.isNotBlank()) {
        runCatching {
            LocalDate.parse(rawDateCandidate)
        }.recoverCatching {
            Instant.parse(rawDateCandidate).atZone(ZoneId.systemDefault()).toLocalDate()
        }.recoverCatching {
            LocalDateTime.parse(rawDateCandidate).toLocalDate()
        }.getOrNull()
    } else null

    val resolvedDate = parsedDate ?: LocalDate.now(ZoneId.systemDefault())

    val formattedDateText = DateTimeFormatter.ofPattern(datePattern, locale)
        .format(resolvedDate)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

    val formattedTimeWindow = formatDetailTimeWindow(timeWindow, triggeredAt, locale)

    return if (formattedTimeWindow.isNotBlank()) {
        "$formattedDateText · $formattedTimeWindow"
    } else {
        formattedDateText
    }
}

private fun formatDetailTimeWindow(
    rawTimeWindow: String,
    triggeredAt: String,
    locale: Locale,
): String {
    val trimmed = rawTimeWindow.trim()
    if (trimmed.isNotBlank()) {
        val rangeRegex = Regex("""(\d{1,2}):(\d{2})\s*-\s*(\d{1,2}):(\d{2})""")
        val match = rangeRegex.find(trimmed)
        if (match != null) {
            val (h1Str, m1Str, h2Str, m2Str) = match.destructured
            val startHour = h1Str.toIntOrNull() ?: 0
            val startMin = m1Str.toIntOrNull() ?: 0
            val endHour = h2Str.toIntOrNull() ?: 0
            val endMin = m2Str.toIntOrNull() ?: 0

            val startTime = LocalTime.of(startHour, startMin)
            val endTime = LocalTime.of(endHour, endMin)

            val isSpanish = locale.language.equals("es", ignoreCase = true)
            val timeFormatter = DateTimeFormatter.ofPattern("h a", locale)

            val startFormatted = formatAmPm(startTime.format(timeFormatter), isSpanish)
            val endFormatted = formatAmPm(endTime.format(timeFormatter), isSpanish)

            val connector = if (isSpanish) " a " else " to "
            return "$startFormatted$connector$endFormatted"
        }
        return trimmed
    }

    if (triggeredAt.isNotBlank()) {
        val instant = runCatching { Instant.parse(triggeredAt) }.getOrNull()
        if (instant != null) {
            val localTime = instant.atZone(ZoneId.systemDefault()).toLocalTime()
            val isSpanish = locale.language.equals("es", ignoreCase = true)
            val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", locale)
            return formatAmPm(localTime.format(timeFormatter), isSpanish)
        }
    }

    return ""
}

private fun formatAmPm(formattedTime: String, isSpanish: Boolean): String {
    if (!isSpanish) return formattedTime
    return formattedTime
        .replace("AM", "a. m.")
        .replace("PM", "p. m.")
        .replace("am", "a. m.")
        .replace("pm", "p. m.")
}

