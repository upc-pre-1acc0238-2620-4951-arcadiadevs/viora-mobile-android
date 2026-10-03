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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlantationFrame
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotName
import pe.edu.upc.viora.features.plotmanagement.presentation.state.RegisterPlotStep
import pe.edu.upc.viora.features.plotmanagement.presentation.state.RegisterPlotUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.state.SaveFailure
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.formatCount
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.formatHectares
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.formatMeters
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.labelRes

/** Step 2: name, variety and planting frame. Density and net area update as the frame changes. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailsStep(
    state: RegisterPlotUiState,
    onNameChange: (String) -> Unit,
    onVarietySelected: (OliveVariety) -> Unit,
    onRowSpacingChange: (Int) -> Unit,
    onTreeSpacingChange: (Int) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
        StepHeader(step = RegisterPlotStep.DETAILS.number, onBack = onBack, onClose = onClose, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val titleStyle = MaterialTheme.typography.displaySmall
            Column {
                Text(text = stringResource(R.string.details_title_lead), style = titleStyle)
                Text(text = stringResource(R.string.details_title_emphasis), style = titleStyle, fontStyle = FontStyle.Italic)
            }
            OutlinePreview(
                corners = state.corners,
                caption = stringResource(
                    R.string.details_summary,
                    formatHectares(state.areaHectares),
                    pluralStringResource(R.plurals.corners_count, state.corners.size, state.corners.size),
                ),
                captionAlignment = Alignment.BottomEnd,
                height = 168.dp,
            )
            NameField(state = state, onNameChange = onNameChange)

            SectionLabel(stringResource(R.string.details_variety))
            VarietyChips(selected = state.variety, onSelected = onVarietySelected)
            if (state.showDetailErrors && state.variety == null) {
                FieldError(stringResource(R.string.details_variety_error))
            }

            SectionLabel(stringResource(R.string.details_frame))
            val tooDense = state.frameError == PlantationFrame.Error.TOO_DENSE
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SpacingCard(
                    label = stringResource(R.string.details_between_rows),
                    meters = state.rowSpacingMeters,
                    hasError = tooDense,
                    onChange = onRowSpacingChange,
                    modifier = Modifier.weight(1f),
                )
                SpacingCard(
                    label = stringResource(R.string.details_between_trees),
                    meters = state.treeSpacingMeters,
                    hasError = tooDense,
                    onChange = onTreeSpacingChange,
                    modifier = Modifier.weight(1f),
                )
            }
            if (tooDense) {
                FieldError(
                    stringResource(
                        R.string.details_frame_too_dense,
                        formatMeters(state.rowSpacingMeters),
                        formatMeters(state.treeSpacingMeters),
                        formatCount(state.treesPerHectare),
                        PlantationFrame.MAX_TREES_PER_HECTARE,
                    ),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.height(IntrinsicSize.Min)) {
                StatTile(
                    value = if (tooDense) "—" else formatCount(state.treesPerHectare),
                    caption = stringResource(R.string.details_density_caption),
                    background = Harvest100,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                StatTile(
                    value = stringResource(R.string.trace_area_value, formatHectares(state.areaHectares)),
                    caption = stringResource(R.string.details_area_caption),
                    background = Green200,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
            Box(Modifier.size(4.dp))
        }
        PrimaryPillButton(
            text = stringResource(R.string.details_review),
            onClick = onContinue,
            trailingIcon = R.drawable.ic_arrow_forward,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
        )
    }
}

@Composable
private fun NameField(state: RegisterPlotUiState, onNameChange: (String) -> Unit) {
    val nameTaken = state.saveFailure == SaveFailure.NameTaken
    val error: String? = when {
        nameTaken -> stringResource(R.string.save_error_name_taken)
        state.showDetailErrors && state.nameError == PlotName.Error.TOO_SHORT -> stringResource(R.string.details_name_error_short)
        state.nameError == PlotName.Error.TOO_LONG -> stringResource(R.string.details_name_error_long)
        else -> null
    }
    Column {
        OutlinedTextField(
            value = state.name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.details_name_label)) },
            singleLine = true,
            isError = error != null,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Neutral0,
                unfocusedContainerColor = Neutral0,
                errorContainerColor = Neutral0,
                focusedBorderColor = Green900,
                unfocusedBorderColor = Neutral300,
                errorBorderColor = Terracotta500,
                focusedLabelColor = Green900,
                errorLabelColor = Terracotta700,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        if (error != null) FieldError(error)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun VarietyChips(selected: OliveVariety?, onSelected: (OliveVariety) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OliveVariety.entries.forEach { variety ->
            val isSelected = variety == selected
            Text(
                text = stringResource(variety.labelRes()),
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) Neutral50 else Neutral900,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) Green900 else Neutral0)
                    .clickable(role = Role.RadioButton, onClick = { onSelected(variety) })
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun SpacingCard(
    label: String,
    meters: Double,
    hasError: Boolean,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Neutral0)
            .border(
                BorderStroke(if (hasError) 2.dp else 0.dp, if (hasError) Terracotta500 else Color.Transparent),
                RoundedCornerShape(24.dp),
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Neutral600)
        Text(
            text = stringResource(R.string.details_meters_value, formatMeters(meters)),
            style = MaterialTheme.typography.headlineMedium,
            color = if (hasError) Terracotta700 else Neutral900,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
            StepperButton(isPlus = false, contentDescription = stringResource(R.string.details_decrease)) { onChange(-1) }
            StepperButton(isPlus = true, contentDescription = stringResource(R.string.details_increase)) { onChange(1) }
        }
    }
}

/** The round minus/plus button; the minus is a plain bar exactly as the design draws it. */
@Composable
private fun StepperButton(isPlus: Boolean, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isPlus) {
            Icon(painter = painterResource(R.drawable.ic_add), contentDescription = contentDescription, tint = Neutral900)
        } else {
            Box(
                modifier = Modifier
                    .size(width = 14.dp, height = 2.2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(Neutral900),
            )
        }
    }
}

@Composable
private fun FieldError(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = Terracotta700,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
    )
}
