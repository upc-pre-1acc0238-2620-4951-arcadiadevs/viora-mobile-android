package pe.edu.upc.viora.features.home.domain.valueobject

/**
 * The two tours of the Home. [FIRST_STEPS] greets an account without plots (P10 · Recorrido sin
 * lotes: the first plot and the "+"); [FULL] walks every section once the first plot exists.
 * Each one is offered once per account.
 */
enum class HomeTour { FIRST_STEPS, FULL }
