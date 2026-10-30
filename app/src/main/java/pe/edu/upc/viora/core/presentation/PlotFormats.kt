package pe.edu.upc.viora.core.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.domain.OliveVariety

/** "7" or "6,5": spacings move in half metres, so one decimal at most. */
fun formatMeters(meters: Double): String = vioraNumberFormat(0, 1).format(meters)

/** "1 429": whole numbers with the language's thousands separator. */
fun formatCount(value: Int): String = formatWhole(value)

/** Localised name of an olive variety. */
@StringRes
fun OliveVariety.labelRes(): Int = when (this) {
    OliveVariety.CRIOLLA -> R.string.variety_criolla
    OliveVariety.SEVILLANA -> R.string.variety_sevillana
    OliveVariety.MANZANILLA -> R.string.variety_manzanilla
    OliveVariety.ARBEQUINA -> R.string.variety_arbequina
}

/** "Sevillana · 2,5 ha · 180 árboles", localised. */
@Composable
fun plotSummary(variety: OliveVariety, areaHectares: Double, estimatedTrees: Int): String {
    val varietyText = stringResource(variety.labelRes())
    val area = formatHectares(areaHectares)
    val trees = pluralStringResource(R.plurals.plots_trees_count, estimatedTrees, formatCount(estimatedTrees))
    return stringResource(R.string.plots_summary, varietyText, area, trees)
}
