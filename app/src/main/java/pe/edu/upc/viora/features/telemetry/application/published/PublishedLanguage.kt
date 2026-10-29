package pe.edu.upc.viora.features.telemetry.application.published

/*
 * The published language of Agroclimatic telemetry: the types its use cases hand to other contexts. Other contexts import them
 * from here, never from this context's domain, so the domain can change behind this list without them noticing.
 */

typealias WeatherForecast = pe.edu.upc.viora.features.telemetry.domain.entity.WeatherForecast

typealias IncidentType = pe.edu.upc.viora.features.telemetry.domain.valueobject.IncidentType
