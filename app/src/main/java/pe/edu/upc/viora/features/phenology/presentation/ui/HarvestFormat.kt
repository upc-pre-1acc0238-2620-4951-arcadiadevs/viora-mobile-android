package pe.edu.upc.viora.features.phenology.presentation.ui

import androidx.annotation.StringRes
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.presentation.formatDecimal
import pe.edu.upc.viora.core.presentation.formatWhole
import pe.edu.upc.viora.core.presentation.vioraNumberFormat
import pe.edu.upc.viora.features.phenology.domain.entity.BbiClass

/** "24 000" in Spanish, "24,000" in English: kilograms with the app's separators. */
fun formatKg(kg: Double): String = vioraNumberFormat(0, 1).format(kg)

/** Tonnes per hectare of a harvest of [kg] kilograms on [areaHectares] hectares. */
fun tonnesPerHectare(kg: Double, areaHectares: Double): Double = kg / 1_000 / areaHectares

/**
 * "8,5": one decimal as in Figma. A yield under 0,1 t/ha (a few kilos on a large plot) keeps two
 * decimals ("0,02") so it is not written as an empty "0,0".
 */
fun formatTonnesPerHectare(value: Double): String =
    formatDecimal(value, if (value > 0.0 && value < SMALL_YIELD) 2 else 1)

private const val SMALL_YIELD = 0.1

/** "7,8k" for bars that are too narrow for the whole number; below a tonne the plain kilos. */
fun formatKgCompact(kg: Double): String =
    if (kg < 1_000) {
        formatWhole(kg)
    } else {
        vioraNumberFormat(0, 1).format(kg / 1_000) + "k"
    }

/** "0,51": the index with two decimals. */
fun formatIndex(value: Double): String = formatDecimal(value, 2)

/** "32 %" for a 0..1 ratio. */
fun formatPercent(ratio: Double): String = formatWhole(ratio * 100) + "\u00A0%"

/** "14 mar 2025" for the moment a record was stored, in the device's zone and language. */
fun formatRecordDate(instant: Instant, zone: ZoneId = ZoneId.systemDefault(), locale: Locale = Locale.getDefault()): String =
    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).format(instant.atZone(zone))

/** "20 sep" for the offline banner. */
fun formatShortDate(instant: Instant, zone: ZoneId = ZoneId.systemDefault(), locale: Locale = Locale.getDefault()): String =
    DateTimeFormatter.ofPattern("d MMM", locale).format(instant.atZone(zone))

@StringRes
fun BbiClass.labelRes(): Int = when (this) {
    BbiClass.LOW -> R.string.harvest_class_low
    BbiClass.MODERATE -> R.string.harvest_class_moderate
    BbiClass.SEVERE -> R.string.harvest_class_severe
}

/** "bearing severe" in the lower case the gauge writes under the index. */
@StringRes
fun BbiClass.sentenceRes(): Int = when (this) {
    BbiClass.LOW -> R.string.harvest_class_low_sentence
    BbiClass.MODERATE -> R.string.harvest_class_moderate_sentence
    BbiClass.SEVERE -> R.string.harvest_class_severe_sentence
}

@StringRes
fun BbiClass.rangeRes(): Int = when (this) {
    BbiClass.LOW -> R.string.harvest_range_low
    BbiClass.MODERATE -> R.string.harvest_range_moderate
    BbiClass.SEVERE -> R.string.harvest_range_severe
}
