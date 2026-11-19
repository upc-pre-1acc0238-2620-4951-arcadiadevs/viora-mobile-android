package pe.edu.upc.viora.features.assistant.presentation.state

import pe.edu.upc.viora.features.assistant.domain.entity.AnswerFact

/**
 * The collapsed line of the summary of the day: "12 días de ventana · 34° máx. jueves · 2 alertas activas".
 *
 * Each card reads as its value followed by its label. A textual value can already end with the first
 * words of its label (the server sends "Sin ventana" over "ventana de aclareo"), so the shared words
 * are written once: "Sin ventana de aclareo", never "Sin ventana ventana de aclareo".
 */
fun summaryLine(facts: List<AnswerFact>): String = facts.joinToString(" · ") { factPhrase(it.value, it.label) }

private fun factPhrase(value: String, label: String): String {
    val valueWords = value.trim().split(WHITESPACE).filter { it.isNotEmpty() }
    val labelWords = label.trim().split(WHITESPACE).filter { it.isNotEmpty() }
    val overlap = (minOf(valueWords.size, labelWords.size) downTo 1).firstOrNull { size ->
        valueWords.takeLast(size).zip(labelWords.take(size)).all { (a, b) -> a.equals(b, ignoreCase = true) }
    } ?: 0
    return (valueWords + labelWords.drop(overlap)).joinToString(" ")
}

private val WHITESPACE = Regex("\\s+")
