package pe.edu.upc.viora.di

import androidx.room3.Database
import androidx.room3.RoomDatabase
import pe.edu.upc.viora.core.database.CacheMetadataDao
import pe.edu.upc.viora.core.database.CacheMetadataEntity
import pe.edu.upc.viora.features.assistant.infrastructure.local.AssistantAnswerDao
import pe.edu.upc.viora.features.assistant.infrastructure.local.AssistantAnswerEntity
import pe.edu.upc.viora.features.croploadregulation.infrastructure.local.DraftTreeSampleDao
import pe.edu.upc.viora.features.croploadregulation.infrastructure.local.DraftTreeSampleEntity
import pe.edu.upc.viora.features.croploadregulation.infrastructure.local.ThinningEventDao
import pe.edu.upc.viora.features.croploadregulation.infrastructure.local.ThinningEventEntity
import pe.edu.upc.viora.features.croploadregulation.infrastructure.local.ThinningPrescriptionDao
import pe.edu.upc.viora.features.croploadregulation.infrastructure.local.ThinningPrescriptionEntity
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.AgronomicDossierDao
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.AgronomicDossierEntity
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.HarvestSettlementDao
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.HarvestSettlementEntity
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.PendingSettlementDao
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.PendingSettlementEntity
import pe.edu.upc.viora.features.phenology.infrastructure.local.BearingIndexDao
import pe.edu.upc.viora.features.phenology.infrastructure.local.BearingIndexEntity
import pe.edu.upc.viora.features.phenology.infrastructure.local.ChillTrackerDao
import pe.edu.upc.viora.features.phenology.infrastructure.local.ChillTrackerEntity
import pe.edu.upc.viora.features.phenology.infrastructure.local.HarvestRecordDao
import pe.edu.upc.viora.features.phenology.infrastructure.local.HarvestRecordEntity
import pe.edu.upc.viora.features.plotmanagement.infrastructure.local.FieldNoteDao
import pe.edu.upc.viora.features.plotmanagement.infrastructure.local.FieldNoteEntity
import pe.edu.upc.viora.features.plotmanagement.infrastructure.local.PlotDao
import pe.edu.upc.viora.features.plotmanagement.infrastructure.local.PlotEntity
import pe.edu.upc.viora.features.subscription.infrastructure.local.IndividualPurchaseDao
import pe.edu.upc.viora.features.subscription.infrastructure.local.IndividualPurchaseEntity
import pe.edu.upc.viora.features.subscription.infrastructure.local.ProducerPlanDao
import pe.edu.upc.viora.features.subscription.infrastructure.local.ProducerPlanEntity
import pe.edu.upc.viora.features.telemetry.infrastructure.local.ForecastDayDao
import pe.edu.upc.viora.features.telemetry.infrastructure.local.ForecastDayEntity
import pe.edu.upc.viora.features.telemetry.infrastructure.local.IncidentDao
import pe.edu.upc.viora.features.telemetry.infrastructure.local.IncidentEntity
import pe.edu.upc.viora.features.telemetry.infrastructure.local.SensorNodeDao
import pe.edu.upc.viora.features.telemetry.infrastructure.local.SensorNodeEntity
import pe.edu.upc.viora.features.telemetry.infrastructure.local.TelemetryReadingDao
import pe.edu.upc.viora.features.telemetry.infrastructure.local.TelemetryReadingEntity

/**
 * The single Room database of the app. It lives with the app's wiring, not in core, because it
 * names the entities of every context. Every bounded context contributes its entities and DAOs
 * here (one line each in [entities] and one `abstract fun xDao()`), then provides the DAO from
 * its own `infrastructure/di` module.
 *
 * Rules:
 * - Before the first release the schema stays at version 1 and is edited in place (clear the
 *   app data or reinstall after pulling a schema change). From the first release on, bump
 *   [version] and add a Migration for every change; never use destructive migration because
 *   the offline sampling queue holds data that is not on the server yet.
 * - Entities never leave `infrastructure`; map them to domain types in a mapper.
 */
@Database(
    entities = [
        CacheMetadataEntity::class,
        ProducerPlanEntity::class,
        IndividualPurchaseEntity::class,
        PlotEntity::class,
        SensorNodeEntity::class,
        IncidentEntity::class,
        HarvestRecordEntity::class,
        BearingIndexEntity::class,
        HarvestSettlementEntity::class,
        TelemetryReadingEntity::class,
        ForecastDayEntity::class,
        PendingSettlementEntity::class,
        DraftTreeSampleEntity::class,
        ThinningEventEntity::class,
        ChillTrackerEntity::class,
        ThinningPrescriptionEntity::class,
        AgronomicDossierEntity::class,
        FieldNoteEntity::class,
        AssistantAnswerEntity::class,
    ],
    version = 16,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun individualPurchaseDao(): IndividualPurchaseDao
    abstract fun producerPlanDao(): ProducerPlanDao
    abstract fun cacheMetadataDao(): CacheMetadataDao
    abstract fun plotDao(): PlotDao
    abstract fun sensorNodeDao(): SensorNodeDao
    abstract fun incidentDao(): IncidentDao
    abstract fun harvestRecordDao(): HarvestRecordDao
    abstract fun bearingIndexDao(): BearingIndexDao
    abstract fun harvestSettlementDao(): HarvestSettlementDao
    abstract fun telemetryReadingDao(): TelemetryReadingDao
    abstract fun forecastDayDao(): ForecastDayDao
    abstract fun pendingSettlementDao(): PendingSettlementDao
    abstract fun draftTreeSampleDao(): DraftTreeSampleDao
    abstract fun thinningEventDao(): ThinningEventDao
    abstract fun chillTrackerDao(): ChillTrackerDao
    abstract fun thinningPrescriptionDao(): ThinningPrescriptionDao
    abstract fun agronomicDossierDao(): AgronomicDossierDao
    abstract fun fieldNoteDao(): FieldNoteDao
    abstract fun assistantAnswerDao(): AssistantAnswerDao
}
