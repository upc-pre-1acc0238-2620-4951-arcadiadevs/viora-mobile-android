package pe.edu.upc.viora.features.plotmanagement.domain.valueobject

/** Name of a plot as the producer typed it: trimmed, 3 to 100 characters. */
@JvmInline
value class PlotName private constructor(val value: String) {

    enum class Error { TOO_SHORT, TOO_LONG }

    companion object {
        const val MIN_LENGTH = 3
        const val MAX_LENGTH = 100

        /** Why [raw] cannot be a plot name, or `null` when it is valid. Used to guide the form. */
        fun check(raw: String): Error? = when {
            raw.trim().length < MIN_LENGTH -> Error.TOO_SHORT
            raw.trim().length > MAX_LENGTH -> Error.TOO_LONG
            else -> null
        }

        /** Builds a name from [raw]; throws if [check] reports an error. */
        fun of(raw: String): PlotName {
            require(check(raw) == null) { "Invalid plot name: '$raw'" }
            return PlotName(raw.trim())
        }
    }
}
