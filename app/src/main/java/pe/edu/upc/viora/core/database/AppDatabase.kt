package pe.edu.upc.viora.core.database

import androidx.room3.Database
import androidx.room3.RoomDatabase

/**
 * The single Room database of the app. Every bounded context contributes its entities and DAOs
 * here (one line each in [entities] and one `abstract fun xDao()`), then provides the DAO from
 * its own `infrastructure/di` module.
 *
 * Rules:
 * - Bump [version] and add a Migration whenever a schema changes. Never use destructive
 *   migration: the offline sampling queue holds data that is not on the server yet.
 * - Entities never leave `infrastructure`; map them to domain types in a mapper.
 */
@Database(
    entities = [
        CacheMetadataEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheMetadataDao(): CacheMetadataDao
}
