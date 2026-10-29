package pe.edu.upc.viora.features.profiles.domain.valueobject

/** Identifier of a producer profile issued by the backend; like [pe.edu.upc.viora.core.domain.PlotId], never blank. */
@JvmInline
value class ProfileId(val value: String) {
    init {
        require(value.isNotBlank()) { "ProfileId must not be blank" }
    }
}
