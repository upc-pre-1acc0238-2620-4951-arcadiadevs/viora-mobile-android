package pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration

import androidx.activity.compose.BackHandler
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import pe.edu.upc.viora.core.designsystem.component.VioraConfirmDialog
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.domain.PlotId
import pe.edu.upc.viora.features.plotmanagement.presentation.state.MarkingMethod
import pe.edu.upc.viora.features.plotmanagement.presentation.state.RegisterPlotStep
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.GpsTrackingViewModel
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
    onExpandPlan: () -> Unit = {},
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
        RegisterPlotStep.TRACE -> if (state.method == MarkingMethod.GPS_WALK) {
            val gps: GpsTrackingViewModel = hiltViewModel()
            GpsStep(
                state = state,
                reading = gps.reading.collectAsStateWithLifecycle().value,
                onMarkCorner = viewModel::addCorner,
                onUndo = viewModel::undoCorner,
                onCloseOutline = viewModel::closeOutline,
                onUseMap = viewModel::switchToMapTrace,
                onBack = ::back,
                onClose = ::leave,
                modifier = modifier,
            )
        } else {
            TraceStep(
                state = state,
                onAddCorner = viewModel::addCorner,
                onMoveCorner = viewModel::moveCorner,
                onUndo = viewModel::undoCorner,
                onCloseOutline = viewModel::closeOutline,
                onBack = ::back,
                onClose = ::leave,
                modifier = modifier,
            )
        }
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
            onAdjustOutline = viewModel::adjustOutline,
            onExpandPlan = onExpandPlan,
        )
    }

    if (askDiscard) {
        // Figma "Capa · ¿Descartar este lote? / abierta".
        VioraConfirmDialog(
            icon = R.drawable.ic_warning,
            iconBackground = Terracotta100,
            iconTint = Terracotta700,
            title = stringResource(R.string.discard_title),
            body = stringResource(R.string.discard_body),
            primaryLabel = stringResource(R.string.discard_keep),
            onPrimary = { askDiscard = false },
            secondaryLabel = stringResource(R.string.discard_confirm),
            onSecondary = onLeave,
            onDismiss = { askDiscard = false },
            secondaryColor = Terracotta700,
        )
    }
}
