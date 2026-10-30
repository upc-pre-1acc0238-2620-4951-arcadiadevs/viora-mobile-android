package pe.edu.upc.viora.core.designsystem.component

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import java.time.LocalDate
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.presentation.pickerMillisToLocalDate
import pe.edu.upc.viora.core.presentation.toPickerMillis

/**
 * The Material date picker in a dialog, limited to the days from [min] (none when null) to [max].
 * The picker works in UTC-midnight milliseconds; this converts both ways so callers only see dates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VioraDatePicker(
    selected: LocalDate,
    max: LocalDate,
    onPick: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    min: LocalDate? = null,
) {
    val minMillis = remember(min) { min?.toPickerMillis() ?: Long.MIN_VALUE }
    val maxMillis = remember(max) { max.toPickerMillis() }
    val selectable = remember(min, max) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis in minMillis..maxMillis
            override fun isSelectableYear(year: Int): Boolean = year <= max.year && (min == null || year >= min.year)
        }
    }
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = selected.toPickerMillis(), selectableDates = selectable)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { pickerState.selectedDateMillis?.let { onPick(pickerMillisToLocalDate(it)) } ?: onDismiss() },
            ) { Text(stringResource(R.string.settle_date_ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.settle_date_cancel)) } },
    ) {
        DatePicker(state = pickerState)
    }
}
