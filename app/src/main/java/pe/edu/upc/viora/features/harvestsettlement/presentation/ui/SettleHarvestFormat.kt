package pe.edu.upc.viora.features.harvestsettlement.presentation.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Formats [date] with a localised pattern resource (`settle_pattern_*`) in the app's language,
 * e.g. "Jueves 14 de mayo". [capitalize] upper-cases the first letter for the long form.
 */
@Composable
internal fun formatSettleDate(date: LocalDate, @StringRes pattern: Int, capitalize: Boolean = false): String {
    val locale = LocalConfiguration.current.locales[0]
    val text = DateTimeFormatter.ofPattern(stringResource(pattern), locale).format(date)
    return if (capitalize) text.replaceFirstChar { it.titlecase(locale) } else text
}
