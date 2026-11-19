package pe.edu.upc.viora.features.phenology.infrastructure.local

import java.time.DateTimeException
import java.time.MonthDay
import java.util.Locale
import pe.edu.upc.viora.features.phenology.domain.OlivePhase
import pe.edu.upc.viora.features.phenology.domain.PhaseAdjustments

/**
 * The corrections of every plot in one preference: plots separated by `;`, each as `plotId|PHASE=MM-dd,PHASE=MM-dd`.
 * A piece that does not read is skipped, so a damaged preference loses a correction instead of the whole calendar.
 */
internal object PhaseAdjustmentsCodec {

    fun encode(byPlot: Map<String, PhaseAdjustments>): String = byPlot
        .filterValues { !it.isEmpty }
        .entries.joinToString(PLOT_SEPARATOR) { (plotId, adjustments) ->
            plotId + ID_SEPARATOR + adjustments.starts.entries.joinToString(PHASE_SEPARATOR) { (phase, on) ->
                String.format(Locale.ROOT, "%s=%02d-%02d", phase.name, on.monthValue, on.dayOfMonth)
            }
        }

    fun decode(raw: String?): Map<String, PhaseAdjustments> {
        if (raw.isNullOrBlank()) return emptyMap()
        val byPlot = mutableMapOf<String, PhaseAdjustments>()
        for (piece in raw.split(PLOT_SEPARATOR)) {
            val plotId = piece.substringBefore(ID_SEPARATOR, missingDelimiterValue = "")
            if (plotId.isEmpty()) continue
            var adjustments = PhaseAdjustments.NONE
            for (entry in piece.substringAfter(ID_SEPARATOR).split(PHASE_SEPARATOR)) {
                val phase = OlivePhase.entries.firstOrNull { it.name == entry.substringBefore('=') } ?: continue
                val on = parse(entry.substringAfter('=', missingDelimiterValue = "")) ?: continue
                adjustments = adjustments.adjusted(phase, on)
            }
            if (!adjustments.isEmpty) byPlot[plotId] = adjustments
        }
        return byPlot
    }

    private fun parse(text: String): MonthDay? {
        val parts = text.split('-')
        if (parts.size != 2) return null
        val month = parts[0].toIntOrNull() ?: return null
        val day = parts[1].toIntOrNull() ?: return null
        return try {
            MonthDay.of(month, day)
        } catch (_: DateTimeException) {
            null
        }
    }

    private const val PLOT_SEPARATOR = ";"
    private const val ID_SEPARATOR = "|"
    private const val PHASE_SEPARATOR = ","
}
