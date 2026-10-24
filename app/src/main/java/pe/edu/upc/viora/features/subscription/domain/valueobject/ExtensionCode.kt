package pe.edu.upc.viora.features.subscription.domain.valueobject

/**
 * The extension code a cooperative shares with its producers: `AMPL-` followed by four letters or
 * digits (P103 "AMPL-3R8T"). Built only from a well-formed text, normalized as the backend reads it.
 */
@JvmInline
value class ExtensionCode private constructor(val value: String) {

    companion object {
        const val PREFIX = "AMPL-"
        const val LENGTH = 9
        private val FORMAT = Regex("^AMPL-[A-Z0-9]{4}$")

        /** Trims and uppercases [text], as the producer may type it in lower case. */
        fun normalize(text: String): String = text.filterNot { it.isWhitespace() }.uppercase()

        fun isWellFormed(text: String): Boolean = FORMAT.matches(normalize(text))

        /** The code for [text], or null when it is not `AMPL-` plus four letters or digits. */
        fun of(text: String): ExtensionCode? = normalize(text).takeIf { FORMAT.matches(it) }?.let(::ExtensionCode)
    }
}
