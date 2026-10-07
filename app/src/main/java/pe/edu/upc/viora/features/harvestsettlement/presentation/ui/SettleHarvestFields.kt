package pe.edu.upc.viora.features.harvestsettlement.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green700
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral500
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.Spacing
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.presentation.ThousandsVisualTransformation
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.CommercialSizeGrade
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.SettleHarvestRules
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.SettleHarvestUiState
import pe.edu.upc.viora.features.phenology.presentation.ui.formatKg
import pe.edu.upc.viora.features.phenology.presentation.ui.formatPercent
import pe.edu.upc.viora.features.phenology.presentation.ui.formatTonnesPerHectare

/** "Fecha de pesaje" (Figma P71): the chosen day with the calendar button that opens the picker. */
@Composable
internal fun WeighingDateField(dateText: String, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.settle_date_pick)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Neutral0)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = description, onClick = onClick)
            .padding(start = 18.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.settle_date_label), style = MaterialTheme.typography.labelMedium, color = Green800)
            Text(dateText, style = MaterialTheme.typography.bodyLarge, color = Neutral900)
        }
        Box(Modifier.size(44.dp).clip(CircleShape).background(Neutral100), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_calendar_month), contentDescription = description, tint = Neutral900)
        }
    }
}

/** The green and black kilos inputs and the live total with its split bar (Figma "Kilos por calidad"). */
@Composable
internal fun KilosByQualityCard(
    state: SettleHarvestUiState,
    onGreenChange: (String) -> Unit,
    onBlackChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Neutral0).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        KilosEntry(
            label = stringResource(R.string.settle_green_label),
            dot = Green700,
            value = state.greenText,
            onValueChange = onGreenChange,
            enabled = !state.isSaving,
            error = state.greenError,
            errorText = kilosErrorText(state.greenText),
        )
        KilosEntry(
            label = stringResource(R.string.settle_black_label),
            dot = Neutral900,
            value = state.blackText,
            onValueChange = onBlackChange,
            enabled = !state.isSaving,
            error = state.blackError,
            errorText = kilosErrorText(state.blackText),
        )
        TotalBlock(state)
    }
}

@Composable
private fun kilosErrorText(text: String): String {
    val kilos = SettleHarvestRules.kilos(text)
    return stringResource(if (kilos == null) R.string.settle_error_invalid else R.string.settle_error_negative)
}

@Composable
private fun KilosEntry(
    label: String,
    dot: Color,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    error: Boolean,
    errorText: String,
) {
    var focused by remember { mutableStateOf(false) }
    val line = when {
        error -> Terracotta500
        focused -> Neutral900
        else -> Neutral200
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(dot))
            Text(label, style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp), color = Neutral700)
        }
        val unit = stringResource(R.string.harvest_kilos_unit)
        val numberStyle = MaterialTheme.typography.displayMedium.copy(
            fontFamily = NewsreaderFamily,
            fontSize = 44.sp,
            lineHeight = 48.sp,
            letterSpacing = (-0.88).sp,
        )
        // Where the number (or the "0" placeholder) ends, so the unit follows it as in Figma
        // ("12 500 kg") while the whole row stays the touch target of the field.
        var numberEnd by remember { mutableFloatStateOf(0f) }
        var placeholderEnd by remember { mutableFloatStateOf(0f) }
        val unitGap = with(LocalDensity.current) { 6.dp.toPx() }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            visualTransformation = ThousandsVisualTransformation,
            textStyle = numberStyle.copy(color = if (error) Terracotta700 else Neutral900),
            cursorBrush = SolidColor(if (error) Terracotta500 else Neutral900),
            onTextLayout = { layout -> numberEnd = if (layout.lineCount > 0) layout.getLineRight(0) else 0f },
            modifier = Modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused }.semantics { contentDescription = label },
            decorationBox = { inner ->
                Box {
                    if (value.isEmpty()) {
                        Text("0", style = numberStyle, color = Neutral200, onTextLayout = { placeholderEnd = it.getLineRight(0) })
                    }
                    inner()
                    Text(
                        unit,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                        color = Neutral600,
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .offset { IntOffset((if (value.isEmpty()) placeholderEnd else numberEnd).roundToInt() + unitGap.roundToInt(), 0) }
                            .padding(bottom = 10.dp),
                    )
                }
            },
        )
        Box(Modifier.fillMaxWidth().height(1.5.dp).background(line))
        if (error) ErrorLine(errorText, Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun TotalBlock(state: SettleHarvestUiState) {
    val total = state.totalKg
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            stringResource(R.string.settle_total_label),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.88.sp),
            color = Neutral600,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (total != null && total > 0.0) stringResource(R.string.settle_total_value, formatKg(total)) else stringResource(R.string.settle_total_empty),
                style = MaterialTheme.typography.headlineLarge.copy(letterSpacing = (-0.28).sp),
                color = if (total != null && total > 0.0) Neutral900 else Neutral500,
            )
            state.tonnesPerHectare?.let {
                Text(
                    text = stringResource(R.string.settle_tonnes_chip, formatTonnesPerHectare(it)),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                    color = Green900,
                    modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(Green200).padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
        SplitBar(state.greenShare)
    }
}

/** Green / black split of the total as two rounded segments; both neutral and empty without a [greenShare]. */
@Composable
internal fun SplitBar(greenShare: Double?, modifier: Modifier = Modifier) {
    val description = greenShare?.let { stringResource(R.string.settle_split_caption, formatPercent(it), formatPercent(1 - it)) }.orEmpty()
    Row(
        modifier = modifier.fillMaxWidth().height(10.dp).semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (greenShare == null) {
            Box(Modifier.weight(0.6f).height(10.dp).clip(RoundedCornerShape(5.dp)).background(Neutral100))
            Box(Modifier.weight(0.4f).height(10.dp).clip(RoundedCornerShape(5.dp)).background(Neutral100))
        } else {
            if (greenShare > 0.0) Box(Modifier.weight(greenShare.toFloat()).height(10.dp).clip(RoundedCornerShape(5.dp)).background(Green700))
            if (greenShare < 1.0) Box(Modifier.weight((1 - greenShare).toFloat()).height(10.dp).clip(RoundedCornerShape(5.dp)).background(Neutral900))
        }
    }
}

/** A white single-line text card with its label above (Figma "Campo · Boleta"). */
@Composable
internal fun TextFieldCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Neutral0).padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Neutral600)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 22.sp, color = Neutral900),
            cursorBrush = SolidColor(Green800),
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = label },
            decorationBox = { inner ->
                Box(Modifier.padding(vertical = 4.dp)) {
                    if (value.isEmpty()) Text(placeholder, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 22.sp), color = Neutral600)
                    inner()
                }
            },
        )
    }
}

/**
 * "Calibre de venta (opcional)": a grade from the dropdown, or a typed count of fruits per kilo.
 * A chosen grade shows as text; the menu offers "Escribir otro número" to go back to typing.
 */
@Composable
internal fun CalibreCard(
    state: SettleHarvestUiState,
    onTextChange: (String) -> Unit,
    onGradeSelect: (CommercialSizeGrade?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var open by remember { mutableStateOf(false) }
    val grade = state.calibreGrade
    val openLabel = stringResource(R.string.settle_calibre_open)
    Column(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Neutral0).padding(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(stringResource(R.string.settle_calibre_label), style = MaterialTheme.typography.labelMedium, color = Neutral600)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (grade != null) {
                Text(
                    stringResource(R.string.settle_calibre_grade, grade.label),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 22.sp),
                    color = Neutral900,
                    modifier = Modifier.weight(1f).clickable(enabled = !state.isSaving, role = Role.Button, onClickLabel = openLabel) { open = true }.padding(vertical = 4.dp),
                )
            } else {
                BasicTextField(
                    value = state.calibreText,
                    onValueChange = onTextChange,
                    enabled = !state.isSaving,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 22.sp, color = if (state.calibreError) Terracotta700 else Neutral900),
                    cursorBrush = SolidColor(Green800),
                    modifier = Modifier.weight(1f).semantics { contentDescription = openLabel },
                    decorationBox = { inner ->
                        Box(Modifier.padding(vertical = 4.dp)) {
                            if (state.calibreText.isEmpty()) {
                                Text(stringResource(R.string.settle_calibre_placeholder), style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 22.sp), color = Neutral600)
                            }
                            inner()
                        }
                    },
                )
            }
            Box {
                Box(
                    modifier = Modifier
                        .size(Spacing.minTouchTarget)
                        .clip(CircleShape)
                        .clickable(enabled = !state.isSaving, role = Role.Button, onClickLabel = openLabel) { open = true },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(R.drawable.ic_expand_more), contentDescription = openLabel, tint = Neutral900)
                }
                DropdownMenu(expanded = open, onDismissRequest = { open = false }, containerColor = Neutral0) {
                    if (grade != null) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settle_calibre_other), style = MaterialTheme.typography.bodyMedium, color = Green800) },
                            onClick = {
                                open = false
                                onGradeSelect(null)
                            },
                        )
                    }
                    CommercialSizeGrade.SCALE.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.settle_calibre_grade, option.label), style = MaterialTheme.typography.bodyMedium, color = Neutral900) },
                            onClick = {
                                open = false
                                onGradeSelect(option)
                            },
                        )
                    }
                }
            }
        }
        if (state.calibreError) {
            ErrorLine(stringResource(R.string.settle_calibre_error))
        } else {
            Text(stringResource(R.string.settle_calibre_help), style = MaterialTheme.typography.labelMedium, color = Green800)
        }
    }
}

/** "Al asentarla, la campaña 2026 se cierra" (Figma "Aviso · cierre"). */
@Composable
internal fun CloseNotice(year: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clip(CircleShape).background(Green200).padding(start = 8.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(28.dp).clip(CircleShape).background(Neutral0), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_info), contentDescription = null, tint = Green800, modifier = Modifier.size(20.dp))
        }
        Text(stringResource(R.string.settle_close_notice, year), style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp), color = Green800)
    }
}

@Composable
internal fun ErrorLine(text: String, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) {
        Icon(painterResource(R.drawable.ic_warning), contentDescription = null, tint = Terracotta700, modifier = Modifier.size(16.dp).padding(top = 1.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = Terracotta700)
    }
}
