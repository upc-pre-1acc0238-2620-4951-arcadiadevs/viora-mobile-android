package pe.edu.upc.viora.features.plotmanagement.domain.valueobject

/** Identifier of a plot (a UUID string issued by the backend). */
@JvmInline
value class PlotId(val value: String) {
    init {
        require(value.isNotBlank()) { "PlotId must not be blank" }
    }
}
