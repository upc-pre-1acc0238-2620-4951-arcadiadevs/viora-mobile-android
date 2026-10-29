package pe.edu.upc.viora.features.phenology.presentation.ui

import androidx.annotation.StringRes
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.presentation.formatDecimal
import pe.edu.upc.viora.features.phenology.domain.entity.BbiClass

/** Tonnes per hectare of a harvest of [kg] kilograms on [areaHectares] hectares. */
fun tonnesPerHectare(kg: Double, areaHectares: Double): Double = kg / 1_000 / areaHectares

/** "0,51": the index with two decimals. */
fun formatIndex(value: Double): String = formatDecimal(value, 2)

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
