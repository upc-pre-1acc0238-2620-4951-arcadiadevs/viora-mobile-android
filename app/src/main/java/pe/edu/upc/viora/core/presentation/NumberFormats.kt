package pe.edu.upc.viora.core.presentation

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

/**
 * The one way the app writes numbers. In Spanish it follows the Figma convention on every screen
 * ("0,51", "27,4°", "12 500 kg"): a decimal comma and a no-break space between thousands, whatever
 * the phone's region says (es-PE and es-US would otherwise write "0.51" and "12,500"). Any other
 * language keeps its own separators.
 */
fun vioraNumberFormat(
    minFractionDigits: Int = 0,
    maxFractionDigits: Int = minFractionDigits,
    locale: Locale = Locale.getDefault(),
): NumberFormat {
    val format = if (locale.language == SPANISH) {
        val symbols = DecimalFormatSymbols(locale).apply {
            decimalSeparator = ','
            groupingSeparator = NO_BREAK_SPACE
        }
        DecimalFormat("#,##0.###", symbols)
    } else {
        NumberFormat.getNumberInstance(locale)
    }
    return format.apply {
        isGroupingUsed = true
        minimumFractionDigits = minFractionDigits
        maximumFractionDigits = maxFractionDigits
    }
}

/** [value] with exactly [decimals] decimals, e.g. `formatDecimal(0.6, 2)` = "0,60". */
fun formatDecimal(value: Double, decimals: Int, locale: Locale = Locale.getDefault()): String =
    vioraNumberFormat(decimals, decimals, locale).format(value)

/** A whole number with thousands separated: "12 500". */
fun formatWhole(value: Number, locale: Locale = Locale.getDefault()): String =
    vioraNumberFormat(0, 0, locale).format(value)

private const val SPANISH = "es"
private const val NO_BREAK_SPACE = '\u00A0'
