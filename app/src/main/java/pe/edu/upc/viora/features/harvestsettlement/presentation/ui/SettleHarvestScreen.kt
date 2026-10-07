package pe.edu.upc.viora.features.harvestsettlement.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.CommercialSizeGrade
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.SettleDialog
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.SettleHarvestRules
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.SettleHarvestUiState
import pe.edu.upc.viora.features.harvestsettlement.presentation.viewmodel.SettleHarvestViewModel
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.CircleIconButton
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.PrimaryPillButton

/**
 * Settle harvest (Figma P71 form, P72 confirm and already-settled dialogs). [onSettled], [onQueued]
 * and [onViewReceipt] receive the plot id and campaign year; the receipt screens (P73) live behind them.
 */
@Composable
fun SettleHarvestScreen(
    onClose: () -> Unit,
    onSettled: (plotId: String, year: Int) -> Unit,
    onQueued: (plotId: String, year: Int) -> Unit,
    onViewReceipt: (plotId: String, year: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettleHarvestViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettleHarvestContent(
        state = state,
        onClose = onClose,
        onWeighedOnChange = viewModel::onWeighedOnChange,
        onGreenChange = viewModel::onGreenChange,
        onBlackChange = viewModel::onBlackChange,
        onMillTicketChange = viewModel::onMillTicketChange,
        onCalibreTextChange = viewModel::onCalibreTextChange,
        onCalibreGradeSelect = viewModel::onCalibreGradeSelect,
        onRequestConfirm = viewModel::requestConfirm,
        onConfirm = { viewModel.confirm(onSettled, onQueued) },
        onDismissDialog = viewModel::dismissDialog,
        onViewReceipt = { onViewReceipt(state.plotId, state.campaignYear) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettleHarvestContent(
    state: SettleHarvestUiState,
    onClose: () -> Unit,
    onWeighedOnChange: (LocalDate) -> Unit,
    onGreenChange: (String) -> Unit,
    onBlackChange: (String) -> Unit,
    onMillTicketChange: (String) -> Unit,
    onCalibreTextChange: (String) -> Unit,
    onCalibreGradeSelect: (CommercialSizeGrade?) -> Unit,
    onRequestConfirm: () -> Unit,
    onConfirm: () -> Unit,
    onDismissDialog: () -> Unit,
    onViewReceipt: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pickingDate by rememberSaveable { mutableStateOf(false) }
    // Back or the close button ask before throwing away kilos the producer already typed.
    var askDiscard by rememberSaveable { mutableStateOf(false) }
    val hasTyped = listOf(state.greenText, state.blackText, state.millTicket, state.calibreText).any { it.isNotBlank() }
    val close = { if (hasTyped && !state.isSaving) askDiscard = true else onClose() }
    BackHandler(enabled = hasTyped && !state.isSaving) { askDiscard = true }
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FormHeader(state, close)
            val headline = MaterialTheme.typography.displayLarge.copy(fontSize = 44.sp, lineHeight = 46.sp, letterSpacing = (-1.1).sp)
            Column(Modifier.padding(top = 20.dp, bottom = 8.dp)) {
                Text(stringResource(R.string.settle_form_headline_lead), style = headline, color = Neutral900)
                Text(stringResource(R.string.settle_form_headline_emphasis), style = headline, fontStyle = FontStyle.Italic, color = Neutral900)
            }
            WeighingDateField(
                dateText = formatSettleDate(state.weighedOn, R.string.settle_pattern_long, capitalize = true),
                enabled = !state.isSaving,
                onClick = { pickingDate = true },
            )
            KilosByQualityCard(state, onGreenChange, onBlackChange)
            TextFieldCard(
                label = stringResource(R.string.settle_mill_label),
                value = state.millTicket,
                onValueChange = onMillTicketChange,
                placeholder = stringResource(R.string.settle_mill_placeholder),
                enabled = !state.isSaving,
            )
            CalibreCard(state, onCalibreTextChange, onCalibreGradeSelect)
            CloseNotice(state.campaignYear)
            state.error?.let {
                Text(stringResource(it.messageRes()), style = MaterialTheme.typography.bodySmall, color = Terracotta700)
            }
            PrimaryPillButton(
                text = stringResource(R.string.settle_submit),
                onClick = onRequestConfirm,
                modifier = Modifier.padding(top = 4.dp),
                enabled = state.canSave,
                trailingIcon = R.drawable.ic_check,
            )
        }
    }
    if (pickingDate) {
        WeighingDatePicker(
            selected = state.weighedOn,
            max = state.maxDate,
            onPick = {
                onWeighedOnChange(it)
                pickingDate = false
            },
            onDismiss = { pickingDate = false },
        )
    }
    when (val dialog = state.dialog) {
        SettleDialog.Confirm -> ConfirmSettleDialog(
            year = state.campaignYear,
            plotName = state.plotName,
            totalKg = state.totalKg ?: 0.0,
            tonnesPerHectare = state.tonnesPerHectare,
            greenShare = state.greenShare,
            isSaving = state.isSaving,
            onConfirm = onConfirm,
            onReview = onDismissDialog,
        )
        is SettleDialog.AlreadySettled -> AlreadySettledDialog(
            year = state.campaignYear,
            plotName = state.plotName,
            existing = dialog.existing,
            onViewReceipt = onViewReceipt,
            // The campaign is closed: there is nothing left to edit on this form.
            onDismiss = {
                onDismissDialog()
                onClose()
            },
        )
        null -> Unit
    }
    if (askDiscard) {
        AlertDialog(
            onDismissRequest = { askDiscard = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            title = { Text(stringResource(R.string.settle_discard_title), style = MaterialTheme.typography.headlineSmall) },
            text = { Text(stringResource(R.string.settle_discard_body), style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = { askDiscard = false }) { Text(stringResource(R.string.discard_keep)) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        askDiscard = false
                        onClose()
                    },
                ) { Text(stringResource(R.string.discard_confirm), color = Terracotta700) }
            },
            tonalElevation = 0.dp,
        )
    }
}

@Composable
private fun FormHeader(state: SettleHarvestUiState, onClose: () -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        CircleIconButton(
            icon = R.drawable.ic_close,
            contentDescription = stringResource(R.string.action_close),
            onClick = onClose,
        )
        Column(Modifier.fillMaxWidth().padding(horizontal = 56.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.settle_form_title), style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, lineHeight = 20.sp), color = Neutral900)
            Text(
                text = if (state.plotName.isBlank()) {
                    stringResource(R.string.home_campaign, state.campaignYear)
                } else {
                    stringResource(R.string.settle_form_subtitle, state.plotName, state.campaignYear)
                },
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeighingDatePicker(selected: LocalDate, max: LocalDate, onPick: (LocalDate) -> Unit, onDismiss: () -> Unit) {
    val maxMillis = remember(max) { SettleHarvestRules.toPickerMillis(max) }
    val selectable = remember(max) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= maxMillis
            override fun isSelectableYear(year: Int): Boolean = year <= max.year
        }
    }
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = SettleHarvestRules.toPickerMillis(selected),
        selectableDates = selectable,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { pickerState.selectedDateMillis?.let { onPick(SettleHarvestRules.fromPickerMillis(it)) } ?: onDismiss() },
            ) { Text(stringResource(R.string.settle_date_ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.settle_date_cancel)) } },
    ) {
        DatePicker(state = pickerState)
    }
}
