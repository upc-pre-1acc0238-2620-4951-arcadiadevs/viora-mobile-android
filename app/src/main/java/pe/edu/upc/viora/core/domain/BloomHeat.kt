package pe.edu.upc.viora.core.domain

/**
 * The one heat threshold of the olive bloom, shared by phenology (the Home bloom card) and telemetry (the alerts):
 * a maximum from 32 °C dries the stigmas, and the producer is asked to water early (event storming EV23, PP09:
 * "> 32 °C con humedad < 20 % en floración"; US18, scenario 2). Before, Home used 30 °C and the alerts 32 °C.
 */
object BloomHeat {
    const val CELSIUS = 32.0
}
