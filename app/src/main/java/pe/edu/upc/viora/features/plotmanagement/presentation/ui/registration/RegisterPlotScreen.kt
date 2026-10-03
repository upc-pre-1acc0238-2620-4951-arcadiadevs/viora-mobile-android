package pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.plotmanagement.presentation.state.RegisterPlotStep
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.RegisterPlotViewModel

/**
 * The plot registration wizard. It owns navigation inside the wizard (back and close) and the
 * "discard?" confirmation; [onLeave] is called when the producer leaves without saving and
 * [onSaved] with the new plot's id once it is saved.
 */
@Composable
fun RegisterPlotScreen(
    onLeave: () -> Unit,
    onSaved: (PlotId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterPlotViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    var askDiscard by rememberSaveable { mutableStateOf(false) }

    fun leave() {
        if (state.hasWorkInProgress) askDiscard = true else onLeave()
    }

    fun back() {
        if (!viewModel.goBack()) leave()
    }

    BackHandler(onBack = ::back)
    LaunchedEffect(state.savedPlotId) {
        state.savedPlotId?.let(onSaved)
    }

    val locationPermission = rememberLocationPermission()

    when (state.step) {
        RegisterPlotStep.METHOD -> MethodStep(
            permission = locationPermission,
            onStart = viewModel::startMarking,
            onBack = ::back,
            onClose = ::leave,
            modifier = modifier,
        )
        RegisterPlotStep.TRACE -> TraceStep(
            state = state,
            onAddCorner = viewModel::addCorner,
            onMoveCorner = viewModel::moveCorner,
            onUndo = viewModel::undoCorner,
            onCloseOutline = viewModel::closeOutline,
            onBack = ::back,
            onClose = ::leave,
            modifier = modifier,
        )
        RegisterPlotStep.DETAILS -> DetailsStep(
            state = state,
            onNameChange = viewModel::setName,
            onVarietySelected = viewModel::setVariety,
            onRowSpacingChange = viewModel::changeRowSpacing,
            onTreeSpacingChange = viewModel::changeTreeSpacing,
            onContinue = viewModel::continueToReview,
            onBack = ::back,
            onClose = ::leave,
            modifier = modifier,
        )
        RegisterPlotStep.REVIEW -> ReviewStep(
            state = state,
            onSave = viewModel::save,
            onBack = ::back,
            onClose = ::leave,
            modifier = modifier,
        )
    }

    if (askDiscard) {
        AlertDialog(
            onDismissRequest = { askDiscard = false },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            title = { Text(stringResource(R.string.discard_title), style = MaterialTheme.typography.headlineSmall) },
            text = { Text(stringResource(R.string.discard_body), style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { askDiscard = false }) { Text(stringResource(R.string.discard_keep)) }
            },
            dismissButton = {
                TextButton(onClick = onLeave) {
                    Text(stringResource(R.string.discard_confirm), color = Terracotta700)
                }
            },
            tonalElevation = 0.dp,
        )
    }
}
