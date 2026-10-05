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

    /** Adds the harvest settlements cache of the logbook. */
    val MIGRATION_2_3 = Migration(2, 3) { connection ->
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `harvest_settlements` (`id` TEXT NOT NULL, `report_id` TEXT NOT NULL, " +
                "`plot_id` TEXT NOT NULL, `campaign_year` INTEGER NOT NULL, `green_kg` REAL NOT NULL, " +
                "`black_kg` REAL NOT NULL, `total_yield_kg` REAL NOT NULL, `commercial_fruits_per_kg` REAL, " +
                "`notes` TEXT, `status` TEXT NOT NULL, `settled_at` TEXT, `thinning_status` TEXT NOT NULL, " +
                "`thinning_executed_date` TEXT, `thinning_prescribed_pct` REAL, `thinning_actual_pct` REAL, " +
                "`thinning_deviation_pp` REAL, `stabilization_status` TEXT NOT NULL, " +
                "`baseline_campaigns` INTEGER NOT NULL, `settled_campaigns` INTEGER NOT NULL, " +
                "`baseline_yield_kg` REAL, `baseline_alternation_index` REAL, `managed_alternation_index` REAL, " +
                "`amplitude_reduction_rate` REAL, `target_achieved` INTEGER, `interannual_variance_kg2` REAL, " +
                "`coefficient_of_variation` REAL, `required_consecutive_pairs` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        )
    }

    val ALL = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
}
