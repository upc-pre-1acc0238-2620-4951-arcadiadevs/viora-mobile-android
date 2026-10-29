package pe.edu.upc.viora.features.croploadregulation.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.CircleIconButton
import pe.edu.upc.viora.core.designsystem.component.PrimaryPillButton
import pe.edu.upc.viora.core.designsystem.component.VioraDatePicker
import pe.edu.upc.viora.core.designsystem.component.VioraLoadingState
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta600
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.designsystem.theme.newsreader
import pe.edu.upc.viora.core.designsystem.theme.roboto
import pe.edu.upc.viora.core.designsystem.theme.robotoMedium
import pe.edu.upc.viora.core.presentation.formatWhole
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.core.presentation.pickerMillisToLocalDate
import pe.edu.upc.viora.core.presentation.toPickerMillis
import pe.edu.upc.viora.features.croploadregulation.presentation.state.RegisterThinningUiState
import pe.edu.upc.viora.features.croploadregulation.presentation.state.WindowCheck
import pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel.RegisterThinningViewModel
import pe.edu.upc.viora.features.harvestsettlement.presentation.ui.TextFieldCard
import pe.edu.upc.viora.features.harvestsettlement.presentation.ui.WeighingDateField

/**
 * P62 · Registrar aclareo (US27): the day, the share of fruit removed (preloaded with the prescription) and a note.
 * A day after the window is accepted with a warning; [onSaved] opens P63 with what the thinning left.
 */
@Composable
fun RegisterThinningScreen(
    onClose: () -> Unit,
    onSaved: (prescriptionId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterThinningViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RegisterThinningContent(
        state = state,
        onClose = onClose,
        onDateChange = viewModel::onDateChange,
        onStep = viewModel::stepPercentage,
        onNoteChange = viewModel::onNoteChange,
        onSave = { viewModel.save(onSaved) },
        modifier = modifier,
    )
}

@Composable
internal fun RegisterThinningContent(
    state: RegisterThinningUiState,
    onClose: () -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onStep: (Int) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pickingDate by rememberSaveable { mutableStateOf(false) }
    // Leaving asks first once the producer changed the preloaded share or wrote a note.
    var askDiscard by rememberSaveable { mutableStateOf(false) }
    val askBeforeLeaving = state.hasChanges && !state.isSaving
    val close = { if (askBeforeLeaving) askDiscard = true else onClose() }
    BackHandler(enabled = askBeforeLeaving) { askDiscard = true }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral100)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .imePadding(),
    ) {
        Header(plotName = state.plotName, onClose = close)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Headline()
            if (state.isLoading) {
                VioraLoadingState(modifier = Modifier.fillMaxWidth().padding(top = 40.dp))
            } else {
                WeighingDateField(
                    dateText = thinningDateText(state.executedOn, state.today),
                    enabled = !state.isSaving && state.prescriptionId != null,
                    onClick = { pickingDate = true },
                    label = stringResource(R.string.thinning_register_date_label),
                )
                PercentageCard(state = state, onStep = onStep)
                TextFieldCard(
                    label = stringResource(R.string.thinning_register_note_label),
                    value = state.note,
                    onValueChange = onNoteChange,
                    placeholder = stringResource(R.string.thinning_register_note_placeholder),
                    enabled = !state.isSaving && state.prescriptionId != null,
                )
                WindowNotice(state)
                state.error?.let {
                    Text(stringResource(it.messageRes()), style = roboto(13.sp, 18.sp), color = Terracotta700)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        PrimaryPillButton(
            text = stringResource(R.string.thinning_register_save),
            onClick = onSave,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
            trailingIcon = R.drawable.ic_check,
            enabled = state.canSave || state.isSaving,
            isLoading = state.isSaving,
        )
    }

    if (pickingDate) {
        VioraDatePicker(
            selected = state.executedOn,
            min = state.earliest,
            max = state.today,
            onPick = {
                onDateChange(it)
                pickingDate = false
            },
            onDismiss = { pickingDate = false },
        )
    }
    if (askDiscard) {
        AlertDialog(
            onDismissRequest = { askDiscard = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            title = { Text(stringResource(R.string.thinning_register_discard_title), style = MaterialTheme.typography.headlineSmall) },
            text = { Text(stringResource(R.string.thinning_register_discard_body), style = MaterialTheme.typography.bodyMedium) },
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
private fun Header(plotName: String, onClose: () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 8.dp), contentAlignment = Alignment.CenterStart) {
        CircleIconButton(icon = R.drawable.ic_close, contentDescription = stringResource(R.string.action_close), onClick = onClose)
        Column(Modifier.fillMaxWidth().padding(horizontal = 56.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.thinning_register), style = robotoMedium(15.sp, 20.sp), color = Neutral900, maxLines = 1)
            if (plotName.isNotBlank()) Text(plotName, style = roboto(12.sp, 16.sp), color = Neutral600, maxLines = 1)
        }
    }
}

/** "¿Cuánto *quitaste?*" at 44 sp, with 24 dp above as in Figma. */
@Composable
private fun Headline() {
    val style = newsreader(44.sp, 46.sp).copy(letterSpacing = (-1.1).sp)
    Column(Modifier.padding(top = 24.dp, bottom = 2.dp)) {
        Text(stringResource(R.string.thinning_register_headline_lead), style = style, color = Neutral900)
        Text(stringResource(R.string.thinning_register_headline_emphasis), style = style.copy(fontStyle = FontStyle.Italic), color = Neutral900)
    }
}

/** "Hoy, martes 18 de noviembre" for today, otherwise "Martes 9 de diciembre". */
@Composable
private fun thinningDateText(date: LocalDate, today: LocalDate): String {
    val locale = LocalConfiguration.current.locales[0]
    val text = DateTimeFormatter.ofPattern(stringResource(R.string.settle_pattern_long), locale).format(date)
    return if (date == today) {
        stringResource(R.string.thinning_register_date_today, text)
    } else {
        text.replaceFirstChar { it.titlecase(locale) }
    }
}

/** "Frutos que quitaste": the share with its steppers, and a 0–100 % bar marking the prescription. */
@Composable
private fun PercentageCard(state: RegisterThinningUiState, onStep: (Int) -> Unit) {
    val enabled = !state.isSaving && state.prescriptionId != null
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Neutral0)
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 16.dp),
    ) {
        Text(stringResource(R.string.thinning_register_removed_label), style = robotoMedium(14.sp, 20.sp), color = Neutral900)
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.thinning_percent, formatWhole(state.percentage)),
                style = newsreader(64.sp, 64.sp).copy(letterSpacing = (-1.6).sp),
                color = Neutral900,
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )
            PercentStepButton(
                description = stringResource(R.string.thinning_register_less, RegisterThinningUiState.PERCENT_STEP),
                dark = false,
                enabled = enabled && state.percentage > 0,
                onClick = { onStep(-1) },
            )
            Spacer(Modifier.width(12.dp))
            PercentStepButton(
                description = stringResource(R.string.thinning_register_more, RegisterThinningUiState.PERCENT_STEP),
                dark = true,
                enabled = enabled && state.percentage < 100,
                onClick = { onStep(+1) },
            )
        }
        RemovalBar(percentage = state.percentage, prescribed = state.prescribedPercentage, modifier = Modifier.padding(top = 19.dp))
        Text(
            state.prescribedPercentage?.let {
                stringResource(R.string.thinning_register_prescribed, stringResource(R.string.thinning_percent, formatWhole(it)))
            } ?: stringResource(R.string.thinning_register_no_plan),
            style = roboto(12.sp, 16.sp),
            color = Neutral600,
            modifier = Modifier.padding(top = 7.dp),
        )
    }
}

/** 60 dp round stepper: "−" on cream, "+" on forest green (Figma "Menos" / "Más"). */
@Composable
private fun PercentStepButton(description: String, dark: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(if (dark) Green800 else Neutral100)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        if (dark) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = null, tint = Neutral0, modifier = Modifier.size(24.dp))
        } else {
            Box(Modifier.width(18.dp).height(2.6.dp).clip(RoundedCornerShape(1.3.dp)).background(Neutral900))
        }
    }
}

/** The removed share in yellow over a cream track, with a dark 3 × 22 dp mark at the prescribed share. */
@Composable
private fun RemovalBar(percentage: Int, prescribed: Int?, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxWidth().height(22.dp)) {
        val track = Modifier.align(Alignment.CenterStart).height(12.dp).clip(RoundedCornerShape(6.dp))
        Box(track.fillMaxWidth().background(Neutral100))
        if (percentage > 0) Box(track.width(maxWidth * (percentage / 100f)).background(Harvest300))
        prescribed?.let {
            Box(
                Modifier.offset(x = (maxWidth * (it / 100f) - 1.5.dp).coerceIn(0.dp, maxWidth - 3.dp))
                    .width(3.dp).height(22.dp).clip(RoundedCornerShape(1.5.dp)).background(Green900),
            )
        }
    }
}

/** The chip "Dentro de la ventana · quedan 12 días" or the late warning under the note. */
@Composable
private fun WindowNotice(state: RegisterThinningUiState) {
    val locale = LocalConfiguration.current.locales[0]
    val pattern = DateTimeFormatter.ofPattern(stringResource(R.string.thinning_pattern_short), locale)
    fun LocalDate.short(): String = pattern.format(this).replace(".", "")
    when (val check = state.windowCheck) {
        // Full width like the late warning: both answers take the same slot, so moving the date does not reshape the form.
        is WindowCheck.Within -> Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp).clip(RoundedCornerShape(20.dp)).background(Green200)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(28.dp).clip(CircleShape).background(Neutral0), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_task_alt), contentDescription = null, tint = Neutral900, modifier = Modifier.size(24.dp))
            }
            Text(
                check.daysLeft?.let { pluralStringResource(R.plurals.thinning_register_within, it, it) }
                    ?: stringResource(R.string.thinning_register_within_past),
                style = robotoMedium(13.sp, 18.sp),
                color = Green800,
            )
        }
        is WindowCheck.Late -> Warning(stringResource(R.string.thinning_register_late, check.closedOn.short()))
        is WindowCheck.NotOpenYet -> Warning(stringResource(R.string.thinning_register_not_open, check.opensOn.short()))
        null -> Unit
    }
}

@Composable
private fun Warning(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp).clip(RoundedCornerShape(20.dp)).background(Terracotta100)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(painterResource(R.drawable.ic_warning), contentDescription = null, tint = Terracotta600, modifier = Modifier.size(20.dp))
        Text(text, style = roboto(13.sp, 18.sp), color = Terracotta700, modifier = Modifier.weight(1f))
    }
}

