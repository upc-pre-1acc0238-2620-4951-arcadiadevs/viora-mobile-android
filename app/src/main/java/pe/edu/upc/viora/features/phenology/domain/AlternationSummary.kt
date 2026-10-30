package pe.edu.upc.viora.features.phenology.domain

import java.time.Instant
import pe.edu.upc.viora.features.phenology.domain.entity.BbiClass
import pe.edu.upc.viora.features.phenology.domain.entity.HarvestRecord
import pe.edu.upc.viora.features.phenology.domain.entity.HoblynBbi

/**
 * The alternate bearing of a plot as every screen reads it: its campaigns, the index once there are enough of
 * them and, before that, how many are still missing. The server's index is what counts; the local formula only
 * stands in while it is missing.
 */
data class AlternationSummary(
    val records: List<HarvestRecord>,
    val index: Double?,
    val missingCampaigns: Int,
    /** When the campaigns were last downloaded; null while they were never. */
    val lastRefresh: Instant? = null,
) {
    val bbiClass: BbiClass? get() = index?.let(BbiClass::of)

    companion object {
        /** Null while nothing is cached and nothing was ever downloaded: better say nothing than "3 missing". */
        fun of(records: List<HarvestRecord>, serverIndex: Double?, lastRefresh: Instant?): AlternationSummary? {
            if (records.isEmpty() && lastRefresh == null) return null
            val enough = records.size >= HoblynBbi.MIN_CAMPAIGNS
            return AlternationSummary(
                records = records,
                index = if (enough) serverIndex ?: HoblynBbi.index(records) else null,
                missingCampaigns = (HoblynBbi.MIN_CAMPAIGNS - records.size).coerceAtLeast(0),
                lastRefresh = lastRefresh,
            )
        }
    }
}
