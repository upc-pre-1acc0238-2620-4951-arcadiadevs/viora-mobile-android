package pe.edu.upc.viora.features.harvestsettlement.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.viora.features.harvestsettlement.presentation.viewmodel.HarvestEntryViewModel

/**
 * What `harvestsettlement` contributes to the Home `sections` slot: the "AHORA · COSECHA" card
 * and, once opened, the plot picker. It draws nothing while no plot is left to register in the
 * current campaign. [onSettle] receives the chosen plot id and the campaign year.
 */
@Composable
fun HarvestEntrySection(
    onSettle: (plotId: String, campaignYear: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HarvestEntryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var picking by rememberSaveable { mutableStateOf(false) }
    if (!state.showCard) return
    HarvestEntryCard(
        pendingCount = state.pendingCount,
        registeredCount = state.registeredCount,
        totalCount = state.totalPlots,
        onRegister = { picking = true },
        modifier = modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp),
    )
    if (picking) {
        HarvestPlotPickerSheet(
            state = state,
            onSelect = {
                picking = false
                onSettle(it.plotId, state.campaignYear)
            },
            onDismiss = { picking = false },
        )
    }
}
