package pe.edu.upc.viora.features.home.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.VioraSectionHeader
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.features.telemetry.presentation.ui.components.HomeAlertsCard

private val ScreenPadding = 24.dp

/**
 * "Hoy en tu campo" section matching Figma P10 (#194:5113).
 *
 * Divided into two columns (252dp height):
 * - Left column: "Clima · ahora" (252dp height)
 * - Right column: 2 rows (120dp each, 12dp gap):
 *   - Top row: "Alertas · resumen" ([HomeAlertsCard])
 *   - Bottom row: "Suelo · humedad" ([HomeSoilMoistureCard])
 */
@Composable
fun HomeTodayInFieldSection(
    activeAlertsCount: Long,
    onOpenAlerts: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenWeatherForecast: () -> Unit = {},
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
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * "Clima · ahora" placeholder card (Figma 194:5122).
 * Empty container with Harvest100 background to avoid collisions with weather feature development.
 */
@Composable
private fun HomeWeatherCard(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Harvest100),
    )
}

/**
 * "Suelo · humedad" placeholder card (Figma 194:5138).
 * Empty container with Green200 background to avoid collisions with soil feature development.
 */
@Composable
private fun HomeSoilMoistureCard(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Green200),
    )
}
