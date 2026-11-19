package pe.edu.upc.viora.features.plotmanagement.presentation.state

/** Keep the map available and never start a GPS walk without both prerequisites. */
fun availableMethod(chosen: MarkingMethod?, permission: Boolean, serviceEnabled: Boolean): MarkingMethod {
    val gpsAvailable = permission && serviceEnabled
    return chosen?.takeIf { it != MarkingMethod.GPS_WALK || gpsAvailable }
        ?: if (gpsAvailable) MarkingMethod.GPS_WALK else MarkingMethod.MAP_TRACE
}
