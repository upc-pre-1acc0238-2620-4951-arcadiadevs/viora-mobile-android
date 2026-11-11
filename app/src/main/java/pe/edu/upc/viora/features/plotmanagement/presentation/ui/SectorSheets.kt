package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.CircleIconButton
import pe.edu.upc.viora.core.designsystem.component.PrimaryPillButton
import pe.edu.upc.viora.core.designsystem.component.VioraBottomSheet
import pe.edu.upc.viora.core.designsystem.component.VioraFieldLabel
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral500
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.features.plotmanagement.application.usecase.AssignPlotSectorUseCase
import pe.edu.upc.viora.features.plotmanagement.domain.entity.SectorChoice
import pe.edu.upc.viora.features.plotmanagement.domain.entity.SectorZone

// P24 · Elegir sector and P24 · Escribir mi sector: the two sheets a member uses to say where a plot is, from the
// registration (P24) and from P06b.

/** The two-line title of the sector sheets: «Elige tu *sector*», «Escribe tu *sector*». */
@Composable
private fun SheetTitle(lead: String, emphasis: String, modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.displaySmall
    Column(modifier) {
        Text(text = lead, style = style, color = Neutral900)
        Text(text = emphasis, style = style, fontStyle = FontStyle.Italic, color = Neutral900)
    }
}

/**
 * P24 · Elegir sector: the license's list with the chosen one marked, and «Mi sector no está» last, which opens
 * [WriteSectorSheet] through [onWriteOwn].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChooseSectorSheet(
    sectors: List<SectorZone>,
    chosen: SectorChoice?,
    onChoose: (SectorZone) -> Unit,
    onWriteOwn: () -> Unit,
    onDismiss: () -> Unit,
) {
    VioraBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SheetTitle(stringResource(R.string.sector_choose_title_lead), stringResource(R.string.sector_choose_title_emphasis))
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Neutral0).padding(horizontal = 16.dp)) {
                sectors.forEach { sector ->
                    ChoiceRow(
                        title = sector.name,
                        selected = (chosen as? SectorChoice.Listed)?.sector?.id == sector.id,
                        onClick = { onChoose(sector) },
                    )
                    HorizontalDivider(color = Neutral200)
                }
                ChoiceRow(
                    title = stringResource(R.string.sector_not_listed),
                    hint = stringResource(R.string.sector_not_listed_hint),
                    selected = chosen is SectorChoice.Written,
                    onClick = onWriteOwn,
                )
            }
            Text(
                text = stringResource(R.string.sector_choose_footer),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
            )
        }
    }
}

@Composable
private fun ChoiceRow(title: String, selected: Boolean, onClick: () -> Unit, hint: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { this.selected = selected }
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = Neutral900)
            if (hint != null) Text(text = hint, style = MaterialTheme.typography.bodySmall, color = Neutral600)
        }
        if (selected) {
            Box(Modifier.size(28.dp).clip(CircleShape).background(Green800), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = Neutral0, modifier = Modifier.size(18.dp))
            }
        } else {
            Box(Modifier.size(24.dp).clip(CircleShape).border(1.5.dp, Neutral300, CircleShape))
        }
    }
}

/**
 * P24 · Escribir mi sector: the member writes the sector the list lacks. The plot is saved anyway and stays «Por
 * confirmar» until [managerName] (their manager) reviews it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteSectorSheet(
    initial: String,
    managerName: String?,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initial) }
    val valid = AssignPlotSectorUseCase.isValidName(name)
    VioraBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(top = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SheetTitle(stringResource(R.string.sector_write_title_lead), stringResource(R.string.sector_write_title_emphasis))
                    Text(stringResource(R.string.sector_write_subtitle), style = MaterialTheme.typography.bodyMedium, color = Neutral600)
                }
                CircleIconButton(
                    icon = R.drawable.ic_close,
                    contentDescription = stringResource(R.string.sector_write_close),
                    onClick = onDismiss,
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Neutral0).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                val label = stringResource(R.string.sector_write_label)
                VioraFieldLabel(label.uppercase())
                BasicTextField(
                    value = name,
                    onValueChange = { if (it.length <= SectorChoice.MAX_NAME_LENGTH) name = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 22.sp, color = Neutral900),
                    cursorBrush = SolidColor(Green800),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { if (valid) onSave(name.trim()) }),
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
                    decorationBox = { inner ->
                        Box(Modifier.fillMaxWidth().clip(CircleShape).background(Neutral100).padding(horizontal = 18.dp, vertical = 14.dp)) {
                            if (name.isEmpty()) {
                                Text(
                                    stringResource(R.string.sector_write_placeholder),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 22.sp),
                                    color = Neutral500,
                                )
                            }
                            inner()
                        }
                    },
                )
            }
            Text(
                text = managerName?.let { stringResource(R.string.sector_write_note, it) }
                    ?: stringResource(R.string.sector_write_note_no_manager),
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral700,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Harvest100).padding(horizontal = 16.dp, vertical = 12.dp),
            )
            PrimaryPillButton(
                text = stringResource(R.string.sector_write_save),
                onClick = { onSave(name.trim()) },
                trailingIcon = R.drawable.ic_check,
                enabled = valid,
            )
        }
    }
}

/**
 * The sector sheets as one flow: the list first, and «Mi sector no está» over it; closing the written one goes back to
 * the list. [onChosen] gets the answer, and the caller closes the flow.
 */
@Composable
fun SectorPicker(
    sectors: List<SectorZone>,
    chosen: SectorChoice?,
    managerName: String?,
    onChosen: (SectorChoice) -> Unit,
    onDismiss: () -> Unit,
) {
    var writing by rememberSaveable { mutableStateOf(false) }
    if (writing) {
        WriteSectorSheet(
            initial = (chosen as? SectorChoice.Written)?.name.orEmpty(),
            managerName = managerName,
            onSave = { onChosen(SectorChoice.Written(it)) },
            onDismiss = { writing = false },
        )
    } else {
        ChooseSectorSheet(
            sectors = sectors,
            chosen = chosen,
            onChoose = { onChosen(SectorChoice.Listed(it)) },
            onWriteOwn = { writing = true },
            onDismiss = onDismiss,
        )
    }
}

/** What a chosen sector reads like on a field or a row: «Los Palos», or «Cerro Blanco · Por confirmar». */
@Composable
fun SectorChoice.label(): String = when (this) {
    is SectorChoice.Listed -> sector.name
    is SectorChoice.Written -> stringResource(R.string.sector_pending_value, name)
}
