package pe.edu.upc.viora.features.harvestsettlement.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import java.time.Instant
import java.time.format.DateTimeFormatter
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.presentation.LocalAppZone
import pe.edu.upc.viora.core.presentation.formatDecimal
import pe.edu.upc.viora.core.presentation.formatPercent
import pe.edu.upc.viora.core.presentation.formatWhole
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.labelRes

/**
 * "c7e1 09fa 3b42 d8e6" per line: the SHA-256 in groups of four, four groups a line, so the whole code fits the card
 * and can be read aloud or compared by eye.
 */
internal fun formatHash(hash: String): String =
    hash.chunked(4).chunked(4).joinToString("\n") { it.joinToString(" ") }

/** "+43 %": how far above the target the load was. */
internal fun formatOverTarget(percent: Double): String = (if (percent > 0) "+" else "") + formatPercent(percent)

/** "14 may 2026, 10:40": when the dossier was sealed, in the phone's zone. */
@Composable
internal fun formatSealedAt(instant: Instant): String {
    val locale = LocalConfiguration.current.locales[0]
    return DateTimeFormatter.ofPattern(stringResource(R.string.dossier_pattern_sealed), locale)
        .format(instant.atZone(LocalAppZone.current)).replace(".", "")
}

/** "1,2 MB" or "480 KB". */
internal fun formatFileSize(bytes: Long): String =
    if (bytes >= MEGABYTE) {
        formatDecimal(bytes / MEGABYTE.toDouble(), 1) + " MB"
    } else {
        formatWhole((bytes + KILOBYTE - 1) / KILOBYTE) + " KB"
    }

/** "Sevillana" for a variety the app knows; any other name as the backend wrote it. */
@Composable
internal fun varietyLabel(variety: String): String =
    OliveVariety.entries.firstOrNull { it.name == variety }?.let { stringResource(it.labelRes()) }
        ?: variety.lowercase().replaceFirstChar { it.titlecase() }

private const val KILOBYTE = 1024L
private const val MEGABYTE = 1024L * 1024L
