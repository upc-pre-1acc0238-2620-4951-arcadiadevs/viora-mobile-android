package pe.edu.upc.viora.core.domain

/** Identifier of a plot (a UUID string issued by the backend), shared by every context that refers to a plot. */
@JvmInline
value class PlotId(val value: String) {
    init {
        require(value.isNotBlank()) { "PlotId must not be blank" }
    }
}
