package pe.edu.upc.viora.core.database

import androidx.room3.Database
import androidx.room3.RoomDatabase
import pe.edu.upc.viora.features.plotmanagement.infrastructure.local.PlotDao
import pe.edu.upc.viora.features.plotmanagement.infrastructure.local.PlotEntity

/**
 * The single Room database of the app. Every bounded context contributes its entities and DAOs
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
        PlotEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheMetadataDao(): CacheMetadataDao
    abstract fun plotDao(): PlotDao
}
