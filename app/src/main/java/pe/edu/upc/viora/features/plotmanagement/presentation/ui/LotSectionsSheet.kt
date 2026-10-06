package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.CircleIconButton

/** The sections a plot has besides its detail; the "more options" sheet jumps between them. */
enum class LotSection(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val hint: Int,
) {
    HARVEST(R.drawable.ic_history, R.string.lot_section_harvest, R.string.lot_section_harvest_hint),
    THINNING(R.drawable.ic_calendar_month, R.string.lot_section_thinning, R.string.lot_section_thinning_hint),
    CLIMATE(R.drawable.ic_cloud, R.string.lot_section_climate, R.string.lot_section_climate_hint),
    SENSORS(R.drawable.ic_sensors, R.string.lot_section_sensors, R.string.lot_section_sensors_hint),
    DOSSIER(R.drawable.ic_inventory, R.string.lot_section_dossier, R.string.lot_section_dossier_hint),
    ;

    companion object {
        /** The sections whose screens exist in the app; the others are listed as "coming soon". */
        val BUILT: Set<LotSection> = setOf(HARVEST, SENSORS, CLIMATE)
    }
}

/**
 * "More options" of a plot (Figma "Capa · Más opciones"): the plot's name and summary over a card
 * with its sections. The section the producer is already in ([current]) is left out. A section
 * that is not in [available] is listed greyed out as "coming soon" instead of a dead row.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LotSectionsSheet(
    plotName: String,
    summary: String,
    current: LotSection,
    onSelect: (LotSection) -> Unit,
    onDismiss: () -> Unit,
    available: Set<LotSection> = LotSection.BUILT,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { Box(Modifier.padding(top = 12.dp).size(width = 40.dp, height = 4.dp).clip(CircleShape).background(Neutral300)) },
    ) {
        Column(
            modifier = Modifier.navigationBarsPadding().padding(horizontal = 24.dp).padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                SheetPlotName(plotName, modifier = Modifier.weight(1f).padding(end = 56.dp))
                CircleIconButton(
                    icon = R.drawable.ic_close,
                    contentDescription = stringResource(R.string.action_close),
                    onClick = onDismiss,
                )
            }
            Text(text = summary, style = MaterialTheme.typography.bodyMedium, color = Neutral600)
            val sections = LotSection.entries.filter { it != current }
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Neutral0).padding(horizontal = 16.dp),
            ) {
                sections.forEachIndexed { position, section ->
                    val enabled = section in available
                    OptionRow(
                        icon = section.icon,
                        title = stringResource(section.title),
                        hint = stringResource(if (enabled) section.hint else R.string.lot_section_soon),
                        onClick = { onSelect(section) },
                        enabled = enabled,
                    )
                    if (position < sections.lastIndex) OptionDivider()
                }
            }
        }
    }
}

/** "La Yarada" over an italic "02": the last word of the name goes on its own line. */
@Composable
private fun SheetPlotName(name: String, modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.displaySmall
    val lead = name.substringBeforeLast(' ', missingDelimiterValue = "")
    val tail = name.substringAfterLast(' ')
    Column(modifier) {
        if (lead.isEmpty()) {
            Text(text = name, style = style)
        } else {
            Text(text = lead, style = style)
            Text(text = tail, style = style, fontStyle = FontStyle.Italic)
        }
    }
}
