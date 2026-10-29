package pe.edu.upc.viora.core.domain

/**
 * A canonical UUID: 36 characters, lower case, in the 8-4-4-4-12 layout the backend issues and accepts.
 * Built only from a well-formed text, so any identifier typed [Uuid] is already valid.
 */
@JvmInline
value class Uuid private constructor(val value: String) {

    override fun toString(): String = value

    companion object {
        private val FORMAT = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")

        /** Whether [text] is a UUID once trimmed and lower-cased. */
        fun isValid(text: String?): Boolean = parseOrNull(text) != null

        /** The UUID for [text] (trimmed, lower-cased), or null when it is not one. */
        fun parseOrNull(text: String?): Uuid? =
            text?.trim()?.lowercase()?.takeIf { FORMAT.matches(it) }?.let(::Uuid)

        /** The UUID for [text], or an [IllegalArgumentException] naming the offending text. */
        fun parse(text: String): Uuid = parseOrNull(text) ?: throw IllegalArgumentException("Not a UUID: $text")

        /** A new random (version 4) UUID. */
        fun random(): Uuid = Uuid(java.util.UUID.randomUUID().toString())
    }
}
