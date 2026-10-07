package pe.edu.upc.viora.features.home.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.VioraSectionHeader
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.HomeAlertsCard
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.HomeSoilMoistureCard
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.HomeWeatherCard

private val ScreenPadding = 24.dp

/**
 * "Hoy en tu campo" section matching Figma P10 (Prototipo 474:211273 / 474:211282 / 474:211287)
 * for the plot in focus ([plotId]).
 *
 * Divided into two columns (252dp height):
 * - Left column: "Clima · ahora" (252dp height)
 * - Right column: 2 rows (120dp each, 12dp gap):
 *   - Top row: "Alertas · resumen" ([HomeAlertsCard])
 *   - Bottom row: "Suelo · humedad" ([HomeSoilMoistureCard])
 */
@Composable
fun HomeTodayInFieldSection(
    plotId: String?,
    activeAlertsCount: Long,
    onOpenAlerts: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenWeatherForecast: () -> Unit = {},
    onOpenSoilMoisture: () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        VioraSectionHeader(
            title = stringResource(R.string.home_today_in_field_title),
            actionLabel = stringResource(R.string.home_today_in_field_action),
            onAction = onOpenWeatherForecast,
            modifier = Modifier.padding(horizontal = ScreenPadding),
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(252.dp)
                .padding(horizontal = ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Left Column: Clima · ahora (Figma 194:5122)
            HomeWeatherCard(
                plotId = plotId,
                onClick = onOpenWeatherForecast,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )

            // Right Column: 2 rows (Alerts on top, Soil on bottom)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Top row: Alertas · resumen (Figma 194:5132)
                HomeAlertsCard(
                    activeCount = activeAlertsCount,
                    onClick = onOpenAlerts,
                    modifier = Modifier.weight(1f),
                )

                // Bottom row: Suelo · humedad (Figma 194:5138)
                HomeSoilMoistureCard(
                    plotId = plotId,
                    onClick = onOpenSoilMoisture,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
