package pe.edu.upc.viora.features.home.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.features.home.presentation.state.HomeAlternation
import pe.edu.upc.viora.features.phenology.domain.entity.BearingYear
import pe.edu.upc.viora.features.phenology.domain.entity.HarvestRecord
import pe.edu.upc.viora.features.phenology.presentation.ui.BearingTag
import pe.edu.upc.viora.features.phenology.presentation.ui.barColor
import pe.edu.upc.viora.features.phenology.presentation.ui.formatTonnesPerHectare
import pe.edu.upc.viora.features.phenology.presentation.ui.tonnesPerHectare
import pe.edu.upc.viora.features.phenology.presentation.ui.voiceText

private val BarWidth = 44.dp
private val TrackHeight = 132.dp

/**
 * "Tu alternancia" of the Home (Figma P10): the harvest of the last campaigns of the plot in
 * focus, in tonnes per hectare, and the sentence Viora says about them. With fewer than three
 * campaigns there is no index yet, so the card asks for the missing ones instead. Tapping it
 * opens the alternation screen of that plot.
 */
@Composable
fun HomeAlternationCard(
    plotName: String,
    alternation: HomeAlternation,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Neutral0)
            .clickable(role = Role.Button, onClick = onOpen)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.home_alternation_title),
                style = MaterialTheme.typography.headlineSmall,
                color = Neutral900,
            )
            Text(
                text = stringResource(R.string.home_alternation_subtitle, plotName),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
                maxLines = 1,
            )
        }
        HorizontalDivider(color = Neutral200)
        when (alternation) {
            is HomeAlternation.Insufficient -> MissingCampaigns(alternation.missing)
            is HomeAlternation.Ready -> {
                CampaignBars(alternation.records, alternation.areaHectares)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                    Box(Modifier.padding(top = 6.dp).size(8.dp).clip(CircleShape).background(Harvest300))
                    Text(
                        text = voiceText(alternation.voice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Neutral900,
                    )
                }
            }
        }
    }
}

@Composable
private fun MissingCampaigns(missing: Int) {
    Text(
        text = pluralStringResource(R.plurals.home_alternation_missing, missing, missing),
        style = MaterialTheme.typography.bodyMedium,
        color = Neutral900,
    )
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.home_alternation_add),
            style = MaterialTheme.typography.titleSmall,
            color = Green800,
        )
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = Green800,
            modifier = Modifier.size(20.dp),
        )
    }
}

/** A pill per campaign, filled to its yield: green for an ON year, yellow for an OFF year. */
@Composable
private fun CampaignBars(records: List<HarvestRecord>, areaHectares: Double) {
    val perHectare = records.map { tonnesPerHectare(it.totalYieldKg, areaHectares) }
    val scaleMax = ((perHectare.maxOrNull() ?: 1.0) * 1.04).coerceAtLeast(0.001)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        records.forEachIndexed { position, record ->
            val value = perHectare[position]
            val filled = (TrackHeight * (value / scaleMax).toFloat()).coerceAtLeast(BarWidth)
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier.width(BarWidth).height(TrackHeight).clip(RoundedCornerShape(BarWidth / 2)).background(Neutral100),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .width(BarWidth)
                            .height(filled)
                            .clip(RoundedCornerShape(BarWidth / 2))
                            .background(record.bearing.barColor()),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Text(
                            text = formatTonnesPerHectare(value),
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 13.sp, lineHeight = 16.sp),
                            color = if (record.bearing == BearingYear.ON) Neutral0 else Neutral900,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 12.dp),
                        )
                    }
                }
                Text(text = record.campaignYear.toString(), style = MaterialTheme.typography.bodySmall, color = Neutral700)
                BearingTag(record.bearing)
            }
        }
    }
}
