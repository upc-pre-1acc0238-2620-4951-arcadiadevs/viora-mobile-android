package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import androidx.annotation.StringRes
import java.text.NumberFormat
import pe.edu.upc.viora.R
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety

/** "2,5" or "117,82": one to two decimals, in the device's language. */
fun formatHectares(hectares: Double): String =
    NumberFormat.getNumberInstance().apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 2
    }.format(hectares)

/** "7" or "6,5": spacings move in half metres, so one decimal at most. */
fun formatMeters(meters: Double): String =
    NumberFormat.getNumberInstance().apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 1
    }.format(meters)

/** "1 429": whole numbers with the language's thousands separator. */
fun formatCount(value: Int): String = NumberFormat.getIntegerInstance().format(value)

/** Localised name of an olive variety. */
@StringRes
fun OliveVariety.labelRes(): Int = when (this) {
    OliveVariety.CRIOLLA -> R.string.variety_criolla
    OliveVariety.SEVILLANA -> R.string.variety_sevillana
    OliveVariety.MANZANILLA -> R.string.variety_manzanilla
    OliveVariety.ARBEQUINA -> R.string.variety_arbequina
}
