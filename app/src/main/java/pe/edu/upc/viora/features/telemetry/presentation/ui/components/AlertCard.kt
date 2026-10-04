package pe.edu.upc.viora.features.telemetry.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.PillShape
import pe.edu.upc.viora.core.designsystem.theme.RobotoFamily
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.telemetry.domain.entity.AgroclimaticIncident
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentSeverity
import pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentType

@Composable
fun AlertCard(
    incident: AgroclimaticIncident,
    onOpenDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCritical = incident.severity == IncidentSeverity.CRITICAL
    val cardBg = if (isCritical) Terracotta100 else Harvest100
    val metricColor = if (isCritical) Terracotta700 else Harvest800
    val iconRes = when (incident.type) {
        IncidentType.HEAT_WAVE -> R.drawable.ic_thermostat
        IncidentType.HYDRIC_STRESS -> R.drawable.ic_water_drop
        else -> R.drawable.ic_sensors
    }

    val titleText = when (incident.type) {
        IncidentType.HEAT_WAVE -> stringResource(R.string.alert_title_heat_wave, incident.plotName)
        IncidentType.HYDRIC_STRESS -> stringResource(R.string.alert_title_hydric_stress, incident.plotName)
        IncidentType.FROST_WARNING -> stringResource(R.string.alert_title_frost_warning, incident.plotName)
        IncidentType.UNKNOWN -> stringResource(R.string.alert_title_generic, incident.plotName)
    }

    val descriptionText = when (incident.type) {
        IncidentType.HEAT_WAVE -> stringResource(R.string.alert_desc_heat_wave)
        IncidentType.HYDRIC_STRESS -> stringResource(R.string.alert_desc_hydric_stress)
        IncidentType.FROST_WARNING -> stringResource(R.string.alert_desc_frost_warning)
        else -> stringResource(R.string.alert_desc_generic)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(cardBg)
            .clickable { onOpenDetail(incident.id) }
            .padding(20.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Neutral0),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = if (isCritical) Terracotta500 else Harvest800,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(if (isCritical) Terracotta500 else Harvest300)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = if (isCritical) stringResource(R.string.alert_severity_critical) else stringResource(R.string.alert_severity_warning),
                        fontFamily = RobotoFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = if (isCritical) Neutral0 else Neutral900,
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(Neutral0)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.alert_time_today),
                        fontFamily = RobotoFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = Neutral700,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                val formattedMetric = if (incident.currentValue % 1.0 == 0.0) {
                    "${incident.currentValue.toInt()}${incident.unit}"
                } else {
                    "${incident.currentValue} ${incident.unit}"
                }

                Text(
                    text = formattedMetric,
                    fontFamily = NewsreaderFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = metricColor,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = titleText,
                fontFamily = NewsreaderFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = Neutral900,
                lineHeight = 26.sp,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = descriptionText,
                fontFamily = RobotoFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Neutral700,
                lineHeight = 20.sp,
            )

            Spacer(modifier = Modifier.height(18.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(PillShape)
                    .background(Green900)
                    .clickable { onOpenDetail(incident.id) }
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.alert_action_see_what_to_do),
                        fontFamily = RobotoFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Neutral0,
                    )

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Neutral0),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_forward),
                            contentDescription = null,
                            tint = Green900,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}
