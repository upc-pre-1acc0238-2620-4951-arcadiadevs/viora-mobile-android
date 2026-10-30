package pe.edu.upc.viora.core.domain.validation

/**
 * The shape of the codes the app asks the producer to type: a fixed [prefix] followed by [bodyLength]
 * letters or digits (`COOP-7K2Q` for a cooperative, `AMPL-4D9X` for a plan extension). The producer may type
 * it in lower case or with spaces, so the text is normalized before it is judged.
 */
class PrefixedCode(val prefix: String, val bodyLength: Int) {
    private val format = Regex("^" + Regex.escape(prefix) + "[A-Z0-9]{" + bodyLength + "}$")

    /** Characters of a complete code, prefix included. */
    val length: Int get() = prefix.length + bodyLength

    /** [text] without whitespace and in upper case, as the backend stores it. */
    fun normalize(text: String): String = text.filterNot { it.isWhitespace() }.uppercase()

    fun isWellFormed(text: String): Boolean = format.matches(normalize(text))

    /** The normalized code, or null when [text] is not a complete code of this shape. */
    fun canonicalOrNull(text: String): String? = normalize(text).takeIf { format.matches(it) }
}
