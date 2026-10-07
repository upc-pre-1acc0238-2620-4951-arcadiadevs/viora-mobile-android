package pe.edu.upc.viora.core.presentation

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Shows "12500" as "12 500" while the producer types (Figma P71), without touching the value the
 * field holds: only the whole part is grouped, digits after a "," or "." are left as typed.
 */
object ThousandsVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val wholeEnd = raw.indexOfFirst { it == ',' || it == '.' }.let { if (it < 0) raw.length else it }
        val whole = raw.substring(0, wholeEnd)
        // Only plain digit runs are grouped; anything else (a minus sign, a typo) is shown as typed.
        if (whole.isEmpty() || !whole.all(Char::isDigit) || whole.length <= GROUP) {
            return TransformedText(text, OffsetMapping.Identity)
        }
        val firstGroup = whole.length % GROUP
        val grouped = StringBuilder()
        whole.forEachIndexed { index, digit ->
            if (index > 0 && (index - firstGroup) % GROUP == 0) grouped.append(SEPARATOR)
            grouped.append(digit)
        }
        val shown = grouped.toString() + raw.substring(wholeEnd)

        // Separators added before the original offset (only inside the whole part).
        fun separatorsBefore(offset: Int): Int {
            val digits = offset.coerceAtMost(wholeEnd)
            if (digits <= firstGroup) return 0
            return (digits - firstGroup - 1) / GROUP + if (firstGroup > 0) 1 else 0
        }

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = offset + separatorsBefore(offset)

            override fun transformedToOriginal(offset: Int): Int {
                var original = 0
                while (original < raw.length && originalToTransformed(original + 1) <= offset) original++
                return original.coerceIn(0, raw.length)
            }
        }
        return TransformedText(AnnotatedString(shown), mapping)
    }

    private const val GROUP = 3
    private const val SEPARATOR = '\u00A0'
}
