package pe.edu.upc.viora.features.harvestsettlement.presentation.state

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import pe.edu.upc.viora.features.phenology.presentation.state.CampaignRules

/** What the "Calibre de venta" field resolves to. */
sealed interface CalibreReading {
    /** Nothing chosen or typed: the field is optional. */
    data object None : CalibreReading

    /** A grade midpoint or a typed count, in fruits per kilogram. */
    data class Value(val fruitsPerKg: Double) : CalibreReading

    /** Something was typed that is not a usable count. */
    data object Invalid : CalibreReading
}

/** Pure rules of the settle harvest form (P71), no Android. */
object SettleHarvestRules {

    const val MILL_TICKET_MAX = 30
    const val MAX_FRUITS_PER_KG = 999.0

    /** Kilos of one quality: blank counts as 0, null when the text is not a number. */
    fun kilos(text: String): Double? = if (text.isBlank()) 0.0 else CampaignRules.parseKilos(text)

    /** True when the text is not a number or is negative. */
    fun kilosError(text: String): Boolean = kilos(text).let { it == null || it < 0.0 }

    /** Green plus black kilos, or null while either is invalid. */
    fun totalKilos(greenText: String, blackText: String): Double? {
        val green = kilos(greenText) ?: return null
        val black = kilos(blackText) ?: return null
        return if (green < 0.0 || black < 0.0) null else green + black
    }

    /** The settlement needs something to settle: the total must be greater than zero. */
    fun isValidTotal(total: Double?): Boolean = total != null && total > 0.0

    /** Share of green olives in 0..1, or null when there is nothing weighed. */
    fun greenShare(greenKg: Double, blackKg: Double): Double? =
        (greenKg + blackKg).takeIf { it > 0.0 }?.let { greenKg / it }

    fun isFuture(date: LocalDate, today: LocalDate): Boolean = date.isAfter(today)

    /** The mill ticket as sent: trimmed, or null when blank. */
    fun millTicket(text: String): String? = text.trim().takeIf { it.isNotEmpty() }

    fun isMillTicketValid(text: String): Boolean = text.trim().length <= MILL_TICKET_MAX

    /** A chosen [grade] wins (its midpoint); otherwise the typed count, e.g. "105" or "105,5". */
    fun calibre(grade: CommercialSizeGrade?, typed: String): CalibreReading {
        if (grade != null) return CalibreReading.Value(grade.midpoint)
        if (typed.isBlank()) return CalibreReading.None
        val count = typed.trim().replace(',', '.').toDoubleOrNull()
        return if (count != null && count > 0.0 && count <= MAX_FRUITS_PER_KG) CalibreReading.Value(count) else CalibreReading.Invalid
    }

    /** The Material date picker works in UTC midnight milliseconds. */
    fun toPickerMillis(date: LocalDate): Long = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

    fun fromPickerMillis(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
}
