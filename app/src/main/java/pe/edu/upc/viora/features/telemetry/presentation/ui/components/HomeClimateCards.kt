package pe.edu.upc.viora.features.telemetry.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest400
import pe.edu.upc.viora.core.designsystem.theme.Harvest700
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.RobotoFamily
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.telemetry.domain.valueobject.SkyCondition
import pe.edu.upc.viora.features.telemetry.presentation.state.HomeSoil
import pe.edu.upc.viora.features.telemetry.presentation.state.HomeWeather
import pe.edu.upc.viora.features.telemetry.presentation.viewmodel.HomeClimateViewModel

/**
 * "Clima · ahora" of the Home (Figma P10, 474:211273) for the plot in focus. Shares its
 * [HomeClimateViewModel] with [HomeSoilMoistureCard] (same screen, same instance).
 */
@Composable
fun HomeWeatherCard(
    plotId: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeClimateViewModel = hiltViewModel(),
) {
    LaunchedEffect(plotId) { plotId?.let(viewModel::showPlot) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    WeatherCardContent(state.weather, onClick, modifier)
}

/** "Suelo · humedad" of the Home (Figma P10, 474:211287): the latest reading of the 30 cm probe. */
@Composable
fun HomeSoilMoistureCard(
    plotId: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeClimateViewModel = hiltViewModel(),
) {
    LaunchedEffect(plotId) { plotId?.let(viewModel::showPlot) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SoilCardContent(state.soil, onClick, modifier)
}

@Composable
internal fun WeatherCardContent(weather: HomeWeather?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Harvest100)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        // The sun bleeds out of the top-end corner as in Figma (x 113, y -14 of a 171 dp card).
        val sky = weather?.sky ?: SkyCondition.SUNNY
        val glyphModifier = Modifier.align(Alignment.TopEnd).offset(x = 19.dp, y = (-14).dp)
        if (sky == SkyCondition.SUNNY) {
            SunGlyph(modifier = glyphModifier, glyphSize = 76.8.dp, color = Harvest400)
        } else {
            WeatherGlyph(sky = sky, modifier = glyphModifier, glyphSize = 76.8.dp, tint = Harvest700)
        }
        Column(Modifier.padding(start = 18.dp, end = 14.dp, top = 20.dp, bottom = 18.dp)) {
            Text(
                text = stringResource(if (weather == null || weather.isLiveReading) R.string.home_weather_now else R.string.home_weather_today),
                style = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
                color = Harvest800,
            )
            Spacer(Modifier.height(48.dp))
            Text(
                text = weather?.let { formatDegrees(locale, it.temperatureCelsius) } ?: "—",
                style = TextStyle(fontFamily = NewsreaderFamily, fontSize = 64.sp, lineHeight = 64.sp, letterSpacing = (-1.92).sp),
                color = Neutral900,
                modifier = Modifier.offset(x = (-4).dp),
            )
            Spacer(Modifier.height(2.dp))
            weather?.sky?.let {
                Text(
                    text = stringResource(skyLabelRes(it)),
                    style = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
                    color = Neutral900,
                )
            }
            if (weather?.maxCelsius != null && weather.minCelsius != null) {
                Text(
                    text = stringResource(R.string.climate_high_low, formatDegrees(locale, weather.maxCelsius), formatDegrees(locale, weather.minCelsius)),
                    style = TextStyle(fontFamily = RobotoFamily, fontSize = 12.sp, lineHeight = 16.sp),
                    color = Neutral700,
                )
            }
            weather?.hotDay?.let { hot ->
                HotDayChip(
                    text = stringResource(R.string.home_weather_hot_day, shortWeekday(hot.date, locale), formatDegrees(locale, hot.maxCelsius)),
                    dot = if (hot.isExtreme) Terracotta500 else Harvest400,
                    modifier = Modifier.padding(top = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun HotDayChip(text: String, dot: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Neutral0)
            .padding(start = 10.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(dot))
        Text(
            text = text,
            style = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
            color = Terracotta700,
        )
    }
}

@Composable
internal fun SoilCardContent(soil: HomeSoil?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Green200)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 18.dp, end = 18.dp, top = 14.dp),
    ) {
        Text(
            text = soil?.let { formatPercent(locale, it.percent) } ?: "—",
            style = TextStyle(fontFamily = NewsreaderFamily, fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-0.8).sp),
            color = Green900,
        )
        Text(
            text = stringResource(R.string.home_soil_moisture_label),
            style = TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
            color = Green800,
            modifier = Modifier.padding(top = 4.dp),
        )
        BoxWithConstraints(
            Modifier
                .padding(top = 14.dp)
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Neutral0),
        ) {
            if (soil != null) {
                val fill = (soil.percent / BAR_FULL_PERCENT).coerceIn(0.0, 1.0).toFloat()
                Box(Modifier.width(maxWidth * fill).height(10.dp).clip(RoundedCornerShape(5.dp)).background(Green800))
            }
        }
    }
}

/** In Figma 34 % fills 77 of the 135 dp bar: the bar spans 0–60 % of volumetric moisture. */
private const val BAR_FULL_PERCENT = 60.0

private fun skyLabelRes(sky: SkyCondition): Int = when (sky) {
    SkyCondition.SUNNY -> R.string.climate_sky_sunny
    SkyCondition.PARTLY_CLOUDY -> R.string.climate_sky_partly_cloudy
    SkyCondition.RAINY -> R.string.climate_sky_rainy
}
