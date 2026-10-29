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

/** "34 %" for a value already in percent (0..100); the sign goes after a no-break space, as in the design. */
fun formatPercent(percent: Double, locale: Locale = Locale.getDefault()): String =
    formatWhole(percent, locale) + NO_BREAK_SPACE + "%"

/** "32 %" for a 0..1 ratio. */
fun formatPercentOf(ratio: Double, locale: Locale = Locale.getDefault()): String = formatPercent(ratio * 100, locale)

/** "2,5" or "117,82": hectares always with one or two decimals, so 2,25 ha is never shown as "2,2". */
fun formatHectares(hectares: Double, locale: Locale = Locale.getDefault()): String =
    vioraNumberFormat(1, 2, locale).format(hectares)

/** "24 000" in Spanish, "24,000" in English: kilograms, with at most one decimal. */
fun formatKg(kg: Double, locale: Locale = Locale.getDefault()): String = vioraNumberFormat(0, 1, locale).format(kg)

/** "7,8k" for bars too narrow for the whole number; below a tonne the plain kilos. */
fun formatKgCompact(kg: Double, locale: Locale = Locale.getDefault()): String =
    if (kg < 1_000) formatWhole(kg, locale) else vioraNumberFormat(0, 1, locale).format(kg / 1_000) + "k"

/**
 * "8,5" t/ha: one decimal as in Figma. A yield under 0,1 t/ha (a few kilos on a large plot) keeps two
 * decimals ("0,02") so it is not written as an empty "0,0".
 */
fun formatTonnesPerHectare(value: Double, locale: Locale = Locale.getDefault()): String =
    formatDecimal(value, if (value > 0.0 && value < SMALL_YIELD) 2 else 1, locale)

private const val SMALL_YIELD = 0.1
private const val SPANISH = "es"
private const val NO_BREAK_SPACE = '\u00A0'
