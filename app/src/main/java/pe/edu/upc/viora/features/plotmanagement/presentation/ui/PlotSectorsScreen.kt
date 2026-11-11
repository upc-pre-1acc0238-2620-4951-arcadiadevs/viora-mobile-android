package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.OutlinedPillButton
import pe.edu.upc.viora.core.designsystem.component.PillButton
import pe.edu.upc.viora.core.designsystem.component.VioraVoice
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.presentation.formatHectares
import pe.edu.upc.viora.core.presentation.labelRes
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.entity.SectorChoice
import pe.edu.upc.viora.features.plotmanagement.presentation.state.PlotSectorsUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.PlotSectorsViewModel

/** P06b · Sector de tus lotes. [onDone] runs once the member saved or put it off. */
@Composable
fun PlotSectorsScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: PlotSectorsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(state.done) { if (state.done) onDone() }
    PlotSectorsContent(
        state = state,
        onBack = onBack,
        onChoose = viewModel::choose,
        onSave = viewModel::save,
        onLater = viewModel::later,
    )
}

@Composable
fun PlotSectorsContent(
    state: PlotSectorsUiState,
    onBack: () -> Unit,
    onChoose: (plotId: String, SectorChoice) -> Unit,
    onSave: () -> Unit,
    onLater: () -> Unit,
) {
    var choosingFor by rememberSaveable { mutableStateOf<String?>(null) }
    choosingFor?.let { plotId ->
        SectorPicker(
            sectors = state.sectors,
            chosen = state.choices[plotId],
            managerName = state.managerName,
            onChosen = {
                onChoose(plotId, it)
                choosingFor = null
            },
            onDismiss = { choosingFor = null },
        )
    }
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        Box(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .size(48.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.action_back), tint = Neutral900)
        }
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val titleStyle = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp, lineHeight = 40.sp)
            Column(Modifier.padding(top = 12.dp, bottom = 6.dp)) {
                Text(stringResource(R.string.plot_sectors_title_lead), style = titleStyle, color = Neutral900)
                Text(stringResource(R.string.plot_sectors_title_emphasis), style = titleStyle, fontStyle = FontStyle.Italic, color = Neutral900)
            }
            VioraVoice(text = stringResource(R.string.plot_sectors_voice), modifier = Modifier.padding(bottom = 10.dp))
            when {
                state.loading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green800)
                }
                state.plots.isEmpty() -> Text(
                    stringResource(R.string.plot_sectors_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral700,
                )
                else -> state.plots.forEach { plot ->
                    PlotSectorRow(plot = plot, choice = state.choices[plot.id.value], onClick = { choosingFor = plot.id.value })
                }
            }
            if (state.saveFailed) {
                Text(stringResource(R.string.plot_sectors_save_failed), style = MaterialTheme.typography.bodyMedium, color = Terracotta700)
            }
        }
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PillButton(text = stringResource(R.string.plot_sectors_save), onClick = onSave, enabled = state.canSave, loading = state.saving)
            OutlinedPillButton(text = stringResource(R.string.plot_sectors_later), onClick = onLater)
        }
    }
}

/** One plot: its mark, name, variety and hectares, and «Elegir sector» or the sector chosen. */
@Composable
private fun PlotSectorRow(plot: Plot, choice: SectorChoice?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Neutral0)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 10.dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(44.dp).clip(CircleShape).background(Green200), contentAlignment = Alignment.Center) {
            Text(plotMark(plot.name), style = MaterialTheme.typography.titleMedium.copy(fontFamily = SerifFamily, fontSize = 15.sp), color = Green900)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(plot.name, style = MaterialTheme.typography.titleSmall, color = Neutral900)
            Text(
                stringResource(R.string.plot_sectors_row_subtitle, stringResource(plot.variety.labelRes()), formatHectares(plot.areaHectares)),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
            )
            if (choice == null) {
                SectorTag(stringResource(R.string.plot_sectors_choose), R.drawable.ic_scale, Terracotta100, Terracotta700)
            } else {
                SectorTag(choice.label(), R.drawable.ic_check, Green200, Green900)
            }
        }
        Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = Neutral700, modifier = Modifier.size(24.dp))
    }
}

@Composable
private fun SectorTag(text: String, icon: Int, background: Color, content: Color) {
    Row(
        modifier = Modifier.clip(CircleShape).background(background).padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = content, modifier = Modifier.size(14.dp))
        Text(text, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = content)
    }
}

/** The serif of the editorial titles, for the plot mark. */
private val SerifFamily: FontFamily
    @Composable get() = MaterialTheme.typography.displaySmall.fontFamily ?: FontFamily.Serif

/** «02» for «La Yarada 02», «LN» for «Lote Norte»: the number a plot ends with, or the initials of two words. */
internal fun plotMark(name: String): String {
    val words = name.trim().split(" ").filter { it.isNotEmpty() }
    val last = words.lastOrNull() ?: return ""
    if (last.any { it.isDigit() } && last.length <= 3) return last
    return words.take(2).joinToString("") { it.first().uppercase() }
}
