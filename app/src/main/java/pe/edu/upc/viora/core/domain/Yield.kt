package pe.edu.upc.viora.core.domain

/** Tonnes per hectare of a harvest of [kg] kilograms on [areaHectares] hectares. */
fun tonnesPerHectare(kg: Double, areaHectares: Double): Double = kg / 1_000 / areaHectares
