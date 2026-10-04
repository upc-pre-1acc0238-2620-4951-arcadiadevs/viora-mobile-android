package pe.edu.upc.viora.core.database

import androidx.room3.migration.Migration
import androidx.sqlite.execSQL

/**
 * Schema migrations of [AppDatabase]. Every change to an entity needs a new version here: Room
 * refuses to open a database whose identity hash no longer matches the one on the phone.
 */
object AppMigrations {

    /** Adds the sensor nodes, the harvest records and the bearing index caches. */
    val MIGRATION_1_2 = Migration(1, 2) { connection ->
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `sensor_nodes` (`id` TEXT NOT NULL, `plot_id` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, `type` TEXT NOT NULL, `depth_cm` INTEGER, `status` TEXT NOT NULL, " +
                "`last_reading_at` TEXT, `last_temperature_celsius` REAL, `last_humidity_percent` REAL, " +
                "PRIMARY KEY(`id`))",
        )
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `harvest_records` (`id` TEXT NOT NULL, `plot_id` TEXT NOT NULL, " +
                "`campaign_year` INTEGER NOT NULL, `total_yield_kg` REAL NOT NULL, `green_kg` REAL, " +
                "`black_kg` REAL, `bearing` TEXT NOT NULL, `recorded_at` TEXT, PRIMARY KEY(`id`))",
        )
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `bearing_indexes` (`plot_id` TEXT NOT NULL, `value` REAL, " +
                "`evaluated_years` INTEGER NOT NULL, `evaluated_at` TEXT, PRIMARY KEY(`plot_id`))",
        )
    }

    val ALL = arrayOf(MIGRATION_1_2)
}
