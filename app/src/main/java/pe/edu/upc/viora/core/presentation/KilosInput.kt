package pe.edu.upc.viora.core.presentation

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

private const val GROUP_DIGITS = 3
