package pe.edu.upc.viora.features.subscription.domain.valueobject

/**
 * The invitation code a cooperative gives its producers: `COOP-` followed by four letters or digits
 * (P05 "COOP-7K2Q"). Built only from a well-formed text, normalized as the backend stores it.
 */
@JvmInline
value class InvitationCode private constructor(val value: String) {

    companion object {
        const val PREFIX = "COOP-"
        const val LENGTH = 9
        private val FORMAT = Regex("^COOP-[A-Z0-9]{4}$")

        /** Trims and uppercases [text], as the producer may type it in lower case. */
        fun normalize(text: String): String = text.filterNot { it.isWhitespace() }.uppercase()

        fun isWellFormed(text: String): Boolean = FORMAT.matches(normalize(text))

        /** The code for [text], or null when it is not `COOP-` plus four letters or digits. */
        fun of(text: String): InvitationCode? = normalize(text).takeIf { FORMAT.matches(it) }?.let(::InvitationCode)
    }
}
