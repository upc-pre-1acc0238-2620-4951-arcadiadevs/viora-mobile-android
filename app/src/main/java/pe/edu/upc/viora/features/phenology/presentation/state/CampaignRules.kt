package pe.edu.upc.viora.features.phenology.presentation.state

import java.time.Instant
import pe.edu.upc.viora.features.phenology.domain.entity.BbiClass
import pe.edu.upc.viora.features.phenology.domain.entity.BbiInterval
import pe.edu.upc.viora.features.phenology.domain.entity.BearingYear
import pe.edu.upc.viora.features.phenology.domain.entity.HarvestRecord
import pe.edu.upc.viora.features.phenology.domain.entity.HoblynBbi

/** Why a campaign year cannot be saved. */
enum class YearError { FUTURE, TOO_OLD, DUPLICATE }

/** What the year stepper says under the year when nothing is wrong. */
sealed interface YearCaption {
    data object FirstEver : YearCaption
    data class BeforeOldest(val year: Int) : YearCaption
    data class AfterLatest(val year: Int) : YearCaption
    data object Between : YearCaption
}

/**
 * The index before and after a change the producer is about to make. A null side means there are
 * not enough campaigns for an index on that side. [intervals] are the ones the change recalculates.
 */
data class IndexPreview(
    val before: Double?,
    val after: Double?,
    val intervals: List<BbiInterval> = emptyList(),
    val campaignsAfter: Int = 0,
) {
    val beforeClass: BbiClass? get() = before?.let(BbiClass::of)
    val afterClass: BbiClass? get() = after?.let(BbiClass::of)
}

/** Pure rules of the add / correct / delete sheet, no Android. */
object CampaignRules {

    const val MIN_YEAR = 2000
    private const val GROUP_DIGITS = 3

    /** Campaign years go from [MIN_YEAR] to [currentYear]; [takenYears] are already registered. */
    fun yearError(year: Int, takenYears: Set<Int>, currentYear: Int): YearError? = when {
        year > currentYear -> YearError.FUTURE
        year < MIN_YEAR -> YearError.TOO_OLD
        year in takenYears -> YearError.DUPLICATE
        else -> null
    }

    fun yearCaption(year: Int, registeredYears: Collection<Int>): YearCaption {
        if (registeredYears.isEmpty()) return YearCaption.FirstEver
        val oldest = registeredYears.min()
        val latest = registeredYears.max()
        return when {
            year < oldest -> YearCaption.BeforeOldest(oldest)
            year > latest -> YearCaption.AfterLatest(latest)
            else -> YearCaption.Between
        }
    }

    /**
     * Reads what the producer typed as kilograms: "20500", "20 500", "20.500" and "20,500" are all
     * twenty thousand five hundred (a separator followed by exactly three digits groups thousands),
     * "20500,5" and "20500.5" have a decimal. Null when it is not a number.
     */
    fun parseKilos(text: String): Double? {
        val cleaned = text.filterNot { it.isWhitespace() || it == '\u00A0' || it == '\u202F' }
        if (cleaned.isEmpty()) return null
        val separatorAt = maxOf(cleaned.lastIndexOf(','), cleaned.lastIndexOf('.'))
        val normalized = if (separatorAt < 0 || cleaned.length - separatorAt - 1 == GROUP_DIGITS) {
            cleaned.filterNot { it == ',' || it == '.' }
        } else {
            cleaned.substring(0, separatorAt).filterNot { it == ',' || it == '.' } + "." + cleaned.substring(separatorAt + 1)
        }
        return normalized.toDoubleOrNull()?.takeIf { it.isFinite() }
    }

    /** Kilos are valid when they are a number greater than zero. */
    fun isValidKilos(kilos: Double?): Boolean = kilos != null && kilos > 0.0

    fun previewAdd(records: List<HarvestRecord>, currentIndex: Double?, year: Int, kilos: Double): IndexPreview {
        val after = records + draftRecord(year, kilos)
        return IndexPreview(currentIndex, HoblynBbi.index(after), emptyList(), after.size)
    }

    fun previewCorrect(records: List<HarvestRecord>, currentIndex: Double?, year: Int, kilos: Double): IndexPreview {
        val after = records.map { if (it.campaignYear == year) it.copy(totalYieldKg = kilos) else it }
        val affected = HoblynBbi.intervals(after).filter { it.fromYear == year || it.toYear == year }
        return IndexPreview(currentIndex, HoblynBbi.index(after), affected, after.size)
    }

    fun previewDelete(records: List<HarvestRecord>, currentIndex: Double?, year: Int): IndexPreview {
        val after = records.filterNot { it.campaignYear == year }
        return IndexPreview(currentIndex, HoblynBbi.index(after), emptyList(), after.size)
    }

    private fun draftRecord(year: Int, kilos: Double) = HarvestRecord(
        id = "draft",
        plotId = "",
        campaignYear = year,
        totalYieldKg = kilos,
        greenKg = null,
        blackKg = null,
        bearing = BearingYear.UNKNOWN,
        recordedAt = Instant.EPOCH,
    )
}
