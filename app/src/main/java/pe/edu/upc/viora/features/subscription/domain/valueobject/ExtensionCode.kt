package pe.edu.upc.viora.features.subscription.domain.valueobject

import pe.edu.upc.viora.core.domain.validation.PrefixedCode

/**
 * The extension code a cooperative shares with its producers: `AMPL-` followed by four letters or
 * digits (P103 "AMPL-3R8T"). Built only from a well-formed text, normalized as the backend reads it.
 */
@JvmInline
value class ExtensionCode private constructor(val value: String) {

    companion object {
        const val PREFIX = "AMPL-"
        const val LENGTH = 9
        private val FORMAT = PrefixedCode(PREFIX, bodyLength = LENGTH - PREFIX.length)

        /** Trims and uppercases [text], as the producer may type it in lower case. */
        fun normalize(text: String): String = FORMAT.normalize(text)

        fun isWellFormed(text: String): Boolean = FORMAT.isWellFormed(text)

        /** The code for [text], or null when it is not `AMPL-` plus four letters or digits. */
        fun of(text: String): ExtensionCode? = FORMAT.canonicalOrNull(text)?.let(::ExtensionCode)
    }
}
