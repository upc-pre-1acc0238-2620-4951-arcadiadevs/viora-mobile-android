package pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlantationFrame
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotName
import pe.edu.upc.viora.features.plotmanagement.presentation.state.EditFailure
import pe.edu.upc.viora.features.plotmanagement.presentation.state.EditPlotUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.formatCount
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.formatHectares
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.labelRes
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.EditPlotViewModel

/** Edits the name, variety and planting frame of a registered plot (Figma P28 "Editar lote"). */
@Composable
fun EditPlotScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditPlotViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    when (state) {
        EditPlotUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        EditPlotUiState.NotFound -> EditPlotNotFound(onBack = onBack, modifier = modifier)
        is EditPlotUiState.Editing -> {
            // Saved: the detail behind this form already shows the new data (it observes the cache).
            LaunchedEffect(state.isSaved) { if (state.isSaved) onBack() }
            EditPlotForm(
                state = state,
                onNameChange = viewModel::setName,
                onToggleVariety = viewModel::toggleVarietyChoice,
                onVarietySelected = viewModel::setVariety,
                onRowSpacingChange = viewModel::setRowSpacing,
                onTreeSpacingChange = viewModel::setTreeSpacing,
                onSave = viewModel::save,
                onClose = onBack,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun EditPlotForm(
    state: EditPlotUiState.Editing,
    onNameChange: (String) -> Unit,
    onToggleVariety: () -> Unit,
    onVarietySelected: (OliveVariety) -> Unit,
    onRowSpacingChange: (String) -> Unit,
    onTreeSpacingChange: (String) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
        FormHeader(plotName = state.original.name, onClose = onClose)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val titleStyle = MaterialTheme.typography.displaySmall
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(text = stringResource(R.string.edit_title_lead), style = titleStyle)
                Text(text = stringResource(R.string.edit_title_emphasis), style = titleStyle, fontStyle = FontStyle.Italic)
            }
            NameCard(
                name = state.name,
                error = nameMessage(state),
                onNameChange = onNameChange,
            )
            VarietyCard(
                variety = state.variety,
                isChoosing = state.isChoosingVariety,
                onToggle = onToggleVariety,
                onSelected = onVarietySelected,
            )
            FrameCard(
                state = state,
                onRowSpacingChange = onRowSpacingChange,
                onTreeSpacingChange = onTreeSpacingChange,
            )
            AreaCard(areaHectares = state.original.areaHectares)
            state.failure?.let { failure -> failureMessage(failure)?.let { message -> InlineError(message) } }
        }
        PrimaryPillButton(
            text = stringResource(R.string.edit_save),
            onClick = onSave,
            trailingIcon = R.drawable.ic_check,
            enabled = state.hasChanges && state.isValid,
            isLoading = state.isSaving,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
        )
    }
}

/** The close button on the left and, centred, what is being edited. */
@Composable
private fun FormHeader(plotName: String, onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
        CircleIconButton(
            icon = R.drawable.ic_close,
            contentDescription = stringResource(R.string.action_close),
            onClick = onClose,
            modifier = Modifier.align(Alignment.CenterStart),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(R.string.edit_header_title), style = MaterialTheme.typography.titleSmall)
            Text(text = plotName, style = MaterialTheme.typography.bodySmall, color = Neutral600, maxLines = 1)
        }
    }
}

/** A white rounded card of the form, optionally outlined in red when its content is wrong. */
@Composable
private fun FieldCard(
    modifier: Modifier = Modifier,
    hasError: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Neutral0)
            .border(BorderStroke(if (hasError) 1.5.dp else 0.dp, if (hasError) Terracotta500 else Neutral0), shape)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        content()
    }
}

@Composable
private fun FieldLabel(text: String, color: Color = Neutral600) {
    Text(text = text, style = MaterialTheme.typography.bodySmall, color = color)
}

@Composable
private fun NameCard(name: String, error: String?, onNameChange: (String) -> Unit) {
    Column {
        FieldCard(hasError = error != null) {
            FieldLabel(stringResource(R.string.edit_name_label), color = if (error != null) Terracotta700 else Neutral600)
            BasicTextField(
                value = name,
                onValueChange = onNameChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Neutral900),
                cursorBrush = SolidColor(Green900),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (error != null) InlineError(error)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VarietyCard(
    variety: OliveVariety,
    isChoosing: Boolean,
    onToggle: () -> Unit,
    onSelected: (OliveVariety) -> Unit,
) {
    FieldCard(onClick = onToggle) {
        FieldLabel(stringResource(R.string.edit_variety_label))
        Text(text = stringResource(variety.labelRes()), style = MaterialTheme.typography.bodyLarge, color = Neutral900)
        if (isChoosing) {
            FlowRow(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OliveVariety.entries.forEach { option ->
                    val selected = option == variety
                    Text(
                        text = stringResource(option.labelRes()),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) Neutral50 else Neutral900,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selected) Green900 else Neutral50)
                            .clickable(role = Role.RadioButton) { onSelected(option) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FrameCard(
    state: EditPlotUiState.Editing,
    onRowSpacingChange: (String) -> Unit,
    onTreeSpacingChange: (String) -> Unit,
) {
    val error = state.frameError
    val hasError = error != null
    Column {
        FieldCard(hasError = hasError) {
            FieldLabel(stringResource(R.string.edit_frame_label), color = if (hasError) Terracotta700 else Neutral600)
            Row(
                modifier = Modifier.padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DistanceTile(text = state.rowSpacingText, hasError = hasError, onChange = onRowSpacingChange)
                Text(text = "×", style = MaterialTheme.typography.bodyLarge, color = Neutral600)
                DistanceTile(text = state.treeSpacingText, hasError = hasError, onChange = onTreeSpacingChange)
            }
            DensityChange(state = state, modifier = Modifier.padding(top = 10.dp))
        }
        when (error) {
            PlantationFrame.Error.TOO_DENSE -> InlineError(
                stringResource(R.string.edit_frame_error, PlantationFrame.MAX_TREES_PER_HECTARE),
                withIcon = true,
            )
            PlantationFrame.Error.NOT_POSITIVE -> InlineError(stringResource(R.string.edit_frame_required))
            null -> Unit
        }
    }
}

/** One distance in meters: a beige tile with the figure in serif, which is typed in place. */
@Composable
private fun DistanceTile(text: String, hasError: Boolean, onChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (hasError) Terracotta100 else MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        BasicTextField(
            value = text,
            onValueChange = onChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineMedium.copy(color = if (hasError) Terracotta700 else Neutral900),
            cursorBrush = SolidColor(Green900),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.width(IntrinsicSize.Min).widthIn(min = 28.dp),
        )
        Text(
            text = stringResource(R.string.details_meters_unit),
            style = MaterialTheme.typography.bodySmall,
            color = if (hasError) Terracotta700 else Neutral600,
            modifier = Modifier.padding(bottom = 4.dp),
        )
    }
}

/** "72 → 100 trees/ha · ≈ 250 in the plot": what the frame being typed does to the density. */
@Composable
private fun DensityChange(state: EditPlotUiState.Editing, modifier: Modifier = Modifier) {
    val notViable = state.frameError != null
    val newDensity = state.rawDensity
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (notViable) Terracotta100 else Green200)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val figures = MaterialTheme.typography.headlineSmall
        Text(
            text = formatCount(state.original.treesPerHectare),
            style = figures,
            color = if (notViable) Terracotta700.copy(alpha = 0.7f) else Neutral600,
        )
        Icon(
            painter = painterResource(R.drawable.ic_arrow_forward),
            contentDescription = null,
            tint = if (notViable) Terracotta700 else Neutral900,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = newDensity?.let(::formatCount) ?: "—",
            style = figures,
            color = if (notViable) Terracotta700 else Neutral900,
        )
        Text(
            text = if (notViable) {
                stringResource(R.string.edit_density_not_viable)
            } else {
                stringResource(R.string.edit_density_caption, formatCount(state.estimatedTrees ?: 0))
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (notViable) Terracotta700 else Neutral900,
            modifier = Modifier.weight(1f),
        )
    }
}

/** The area comes from the outline, so here it can only be read (it changes by adjusting the outline). */
@Composable
private fun AreaCard(areaHectares: Double) {
    FieldCard {
        FieldLabel(stringResource(R.string.edit_area_label))
        Text(
            text = stringResource(R.string.trace_area_value, formatHectares(areaHectares)),
            style = MaterialTheme.typography.bodyLarge,
            color = Neutral600,
        )
    }
}

@Composable
private fun InlineError(text: String, withIcon: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (withIcon) {
            Icon(
                painter = painterResource(R.drawable.ic_info),
                contentDescription = null,
                tint = Terracotta700,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = Terracotta700)
    }
}

@Composable
private fun nameMessage(state: EditPlotUiState.Editing): String? = when {
    state.failure == EditFailure.NameTaken -> stringResource(R.string.save_error_name_taken)
    state.name == state.original.name -> null
    state.nameError == PlotName.Error.TOO_SHORT -> stringResource(R.string.details_name_error_short)
    state.nameError == PlotName.Error.TOO_LONG -> stringResource(R.string.details_name_error_long)
    else -> null
}

/** The message for a failed save; the taken name is explained under its field instead. */
@Composable
private fun failureMessage(failure: EditFailure): String? = when (failure) {
    EditFailure.NameTaken -> null
    EditFailure.Outdated -> stringResource(R.string.edit_error_outdated)
    is EditFailure.Rejected -> stringResource(R.string.save_error_rejected, failure.detail.orEmpty())
    is EditFailure.Other -> stringResource(failure.error.messageRes())
}

@Composable
private fun EditPlotNotFound(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().statusBarsPadding().padding(24.dp)) {
        CircleIconButton(
            icon = R.drawable.ic_close,
            contentDescription = stringResource(R.string.action_close),
            onClick = onBack,
        )
        Text(
            text = stringResource(R.string.plot_detail_not_found_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
