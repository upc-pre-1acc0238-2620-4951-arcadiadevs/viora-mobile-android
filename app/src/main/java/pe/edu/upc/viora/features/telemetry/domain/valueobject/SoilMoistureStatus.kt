package pe.edu.upc.viora.features.telemetry.domain.valueobject

/** How the latest soil moisture reading compares with the recharge point. */
enum class SoilMoistureStatus {
    /** Comfortably above the recharge point. */
    IN_RANGE,

    /** Still above the recharge point but close enough to plan the next irrigation. */
    WATCH,

    /** Below the recharge point: hydric stress (the alert of US18). */
    STRESS,
}
