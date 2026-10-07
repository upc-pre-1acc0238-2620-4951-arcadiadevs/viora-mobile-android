package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import androidx.annotation.StringRes
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.presentation.formatWhole
import pe.edu.upc.viora.core.presentation.vioraNumberFormat
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety

/** "2,5" or "117,82": one to two decimals, with the app's separators. */
fun formatHectares(hectares: Double): String = vioraNumberFormat(1, 2).format(hectares)

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
