package pe.edu.upc.viora.features.croploadregulation.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.CircleIconButton
import pe.edu.upc.viora.core.designsystem.component.VioraBottomSheet
import pe.edu.upc.viora.core.designsystem.component.VioraLoadingState
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.features.croploadregulation.presentation.state.PlanRow
import pe.edu.upc.viora.features.croploadregulation.presentation.state.PlanRowStatus
import pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel.CampaignPlanViewModel

/**
 * "Aclareo realizado" of the "+" menu: the plots of this campaign with the state of their plan, as
 * in P60. A plot whose plan asks for thinning (open or closed window) goes straight to P62; any
 * other one opens its P61, which says what is missing (sampling, full bloom...) or shows the result.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThinningPlotPickerSheet(
    onRegister: (plotId: String, plotName: String, campaignYear: Int) -> Unit,
    onOpenPlan: (plotId: String, plotName: String, campaignYear: Int) -> Unit,
    onDismiss: () -> Unit,
    viewModel: CampaignPlanViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    VioraBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val title = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp, lineHeight = 38.sp)
                    Column {
                        Text(stringResource(R.string.thinning_picker_title_lead), style = title, color = Neutral900)
                        Text(stringResource(R.string.thinning_picker_title_emphasis), style = title, fontStyle = FontStyle.Italic, color = Neutral900)
                    }
                    Text(
                        text = stringResource(R.string.thinning_picker_subtitle, state.campaignYear),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                        color = Neutral600,
                    )
                }
                CircleIconButton(icon = R.drawable.ic_close, contentDescription = stringResource(R.string.action_close), onClick = onDismiss)
            }
            when {
                state.isLoading && state.rows.isEmpty() -> VioraLoadingState(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), color = MaterialTheme.colorScheme.primary)
                state.rows.isEmpty() -> Text(
                    stringResource(R.string.thinning_picker_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
                else -> Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.rows.forEach { row ->
                        PlanPlotRow(row = row, state = state, onClick = {
                            if (row.opensRegistration()) {
                                onRegister(row.plot.id.value, row.plot.name, state.campaignYear)
                            } else {
                                onOpenPlan(row.plot.id.value, row.plot.name, state.campaignYear)
                            }
                        })
                    }
                }
            }
            Text(stringResource(R.string.thinning_picker_note), style = MaterialTheme.typography.bodySmall, color = Neutral600)
        }
    }
}

/** P62 accepts the thinning of an issued plan, on time or late; any other state is explained by P61. */
internal fun PlanRow.opensRegistration(): Boolean =
    status == PlanRowStatus.RECOMMENDED || status == PlanRowStatus.WINDOW_CLOSED
