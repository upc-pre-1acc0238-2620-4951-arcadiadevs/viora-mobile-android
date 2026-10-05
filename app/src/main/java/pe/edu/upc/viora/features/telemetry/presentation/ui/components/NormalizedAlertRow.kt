package pe.edu.upc.viora.features.telemetry.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green100
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.PillShape
import pe.edu.upc.viora.core.designsystem.theme.RobotoFamily
import pe.edu.upc.viora.features.telemetry.domain.entity.AgroclimaticIncident
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentType

/**
 * Informative row representing a normalized (resolved) agroclimatic alert.
 * Shows task_alt check icon, condition title and resolution date without navigation.
 */
@Composable
fun NormalizedAlertRow(
    incident: AgroclimaticIncident,
    modifier: Modifier = Modifier,
) {
    val title = when {
        incident.type == IncidentType.HYDRIC_STRESS && incident.plotName.isNotBlank() ->
            stringResource(R.string.alert_normalized_title_hydric, incident.plotName)
        incident.type == IncidentType.HEAT_WAVE && incident.plotName.isNotBlank() ->
            stringResource(R.string.alert_normalized_title_thermal, incident.plotName)
        incident.plotName.isNotBlank() ->
            stringResource(R.string.alert_normalized_title_with_plot, incident.plotName)
        else ->
            stringResource(R.string.alert_normalized_title_generic)
    }

    val resolutionDateText = formatResolutionDate(incident)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(PillShape)
            .background(Neutral0)
            .padding(start = 12.dp, end = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Green100),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_task_alt),
                contentDescription = null,
                tint = Green800,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            fontFamily = RobotoFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Neutral900,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = resolutionDateText,
            fontFamily = RobotoFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            color = Neutral600,
            maxLines = 1,
        )
    }
}

@Composable
private fun formatResolutionDate(incident: AgroclimaticIncident): String {
    val dateString = incident.triggeredAt
    if (dateString.isBlank()) return ""

    val zone = ZoneId.systemDefault()
    val eventDate = runCatching {
        Instant.parse(dateString).atZone(zone).toLocalDate()
    }.recoverCatching {
        LocalDate.parse(dateString)
    }.getOrNull() ?: return dateString

    val today = LocalDate.now(zone)
    val yesterday = today.minusDays(1)
    val locale = LocalConfiguration.current.locales[0]

    return when (eventDate) {
        today -> stringResource(R.string.alert_date_today)
        yesterday -> stringResource(R.string.alert_date_yesterday)
        else -> {
            val formatter = DateTimeFormatter.ofPattern("d MMM", locale)
            eventDate.format(formatter).lowercase(locale)
        }
    }
}
