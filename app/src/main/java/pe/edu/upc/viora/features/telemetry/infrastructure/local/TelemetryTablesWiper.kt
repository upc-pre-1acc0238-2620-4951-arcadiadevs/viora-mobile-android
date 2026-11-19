package pe.edu.upc.viora.features.telemetry.infrastructure.local

import javax.inject.Inject
import pe.edu.upc.viora.core.database.AccountDataWiper
import pe.edu.upc.viora.features.telemetry.domain.notification.CriticalAlertSettings

/** The tables of this context, its sensor nodes, readings, forecast and incidents: emptied when the account leaves the phone. */
class TelemetryTablesWiper @Inject constructor(
    private val sensorNodeDao: SensorNodeDao,
    private val telemetryReadingDao: TelemetryReadingDao,
    private val forecastDayDao: ForecastDayDao,
    private val incidentDao: IncidentDao,
    private val criticalAlertSettings: CriticalAlertSettings,
) : AccountDataWiper {
    override suspend fun wipe() {
        sensorNodeDao.wipeAll()
        telemetryReadingDao.wipeAll()
        forecastDayDao.wipeAll()
        incidentDao.wipeAll()
        criticalAlertSettings.clearNotified()
    }
}
