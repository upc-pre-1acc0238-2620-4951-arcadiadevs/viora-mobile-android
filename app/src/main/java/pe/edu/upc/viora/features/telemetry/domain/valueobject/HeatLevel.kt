package pe.edu.upc.viora.features.telemetry.domain.valueobject

/** How hot a forecast day is for the olive tree; 32 °C is the critical flowering threshold (US18). */
enum class HeatLevel {
    NORMAL,
    WARM,
    EXTREME,
}
