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

    /** Adds the hourly telemetry readings and the 7-day forecast caches of the plot climate. */
    val MIGRATION_3_4 = Migration(3, 4) { connection ->
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `telemetry_readings` (`id` TEXT NOT NULL, `plot_id` TEXT NOT NULL, " +
                "`observed_at_epoch_ms` INTEGER NOT NULL, `air_temperature_celsius` REAL, " +
                "`relative_humidity_percent` REAL, `soil_moisture_30cm_percent` REAL, " +
                "`soil_moisture_60cm_percent` REAL, PRIMARY KEY(`id`))",
        )
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `forecast_days` (`id` TEXT NOT NULL, `plot_id` TEXT NOT NULL, " +
                "`forecast_date` TEXT NOT NULL, `max_temperature_celsius` REAL NOT NULL, " +
                "`min_temperature_celsius` REAL NOT NULL, `precipitation_probability_percent` REAL NOT NULL, " +
                "`wind_speed_kmh` REAL NOT NULL, `synced_at_epoch_ms` INTEGER NOT NULL, PRIMARY KEY(`id`))",
        )
    }

    /**
     * The statements of [MIGRATION_4_5], kept visible so a test can compare them with the exported
     * `5.json`. The `agroclimatic_incidents` table is created here because it joined the v3 schema
     * without a migration: installs from 0.11.0 (v3) lack it, and a 3->4->5 upgrade gets it here before
     * Room validates the schema.
     * `IF NOT EXISTS` keeps the migration safe for databases that already have it.
     */
    internal val MIGRATION_4_5_STATEMENTS = listOf(
        "ALTER TABLE `harvest_settlements` ADD COLUMN `receipt_number` TEXT",
        "ALTER TABLE `harvest_settlements` ADD COLUMN `weighed_on` TEXT",
        "ALTER TABLE `harvest_settlements` ADD COLUMN `mill_ticket_number` TEXT",
        "ALTER TABLE `harvest_settlements` ADD COLUMN `commercial_size_grade` TEXT",
        "CREATE TABLE IF NOT EXISTS `pending_settlements` (`plot_id` TEXT NOT NULL, " +
            "`campaign_year` INTEGER NOT NULL, `green_olives_kg` REAL NOT NULL, " +
            "`black_olives_kg` REAL NOT NULL, `weighed_on` TEXT NOT NULL, `mill_ticket_number` TEXT, " +
            "`commercial_fruits_per_kg` REAL, `notes` TEXT, `idempotency_key` TEXT NOT NULL, " +
            "`status` TEXT NOT NULL, `last_error_code` TEXT, `attempt_count` INTEGER NOT NULL, " +
            "`existing_total_yield_kg` REAL, `existing_receipt_number` TEXT, `existing_weighed_on` TEXT, " +
            "`created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, " +
            "PRIMARY KEY(`plot_id`, `campaign_year`))",
        "CREATE TABLE IF NOT EXISTS `agroclimatic_incidents` (`id` TEXT NOT NULL, `plot_id` TEXT NOT NULL, " +
            "`plot_name` TEXT NOT NULL, `plot_variety` TEXT NOT NULL, `type` TEXT NOT NULL, " +
            "`severity` TEXT NOT NULL, `status` TEXT NOT NULL, `headline_key` TEXT NOT NULL, " +
            "`metric_name` TEXT NOT NULL, `current_value` REAL NOT NULL, `threshold_value` REAL NOT NULL, " +
            "`unit` TEXT NOT NULL, `triggered_at` TEXT NOT NULL, `stress_duration_minutes` INTEGER NOT NULL, " +
            "`snoozed_until` TEXT, PRIMARY KEY(`id`))",
    )

    /** Adds the receipt columns to the settlements cache, the offline settlement queue and the incidents cache. */
    val MIGRATION_4_5 = Migration(4, 5) { connection ->
        MIGRATION_4_5_STATEMENTS.forEach { connection.execSQL(it) }
    }

    internal const val MIGRATION_5_6_SQL =
        "CREATE TABLE IF NOT EXISTS `draft_tree_samples` (`id` TEXT NOT NULL, `plot_id` TEXT NOT NULL, " +
            "`plot_name` TEXT NOT NULL, `campaign_year` INTEGER NOT NULL, `tree_identifier` TEXT NOT NULL, " +
            "`shoots_count` INTEGER NOT NULL, `fruit_set_count` INTEGER NOT NULL, " +
            "`trunk_circumference_cm` REAL, `trunk_diameter_mm` REAL, `observed_on` TEXT NOT NULL, " +
            "`is_synced` INTEGER NOT NULL DEFAULT 0, `created_at` INTEGER NOT NULL, PRIMARY KEY(`id`))"

    /** Adds the draft tree samples cache for in-progress field sampling rounds. */
    val MIGRATION_5_6 = Migration(5, 6) { connection ->
        connection.execSQL(MIGRATION_5_6_SQL)
    }

    internal const val MIGRATION_6_7_SQL =
        "CREATE TABLE IF NOT EXISTS `thinning_events` (`id` TEXT NOT NULL, `event_type` TEXT NOT NULL, " +
            "`prescription_id` TEXT NOT NULL, `confirmation_id` TEXT, `plot_id` TEXT NOT NULL, " +
            "`plot_name` TEXT NOT NULL, `campaign_year` INTEGER NOT NULL, `occurred_at` TEXT NOT NULL, " +
            "`evaluated_trees_count` INTEGER, `total_shoots_count` INTEGER, `total_fruits_count` INTEGER, " +
            "`mean_fruits_per_shoot` REAL, `is_representative` INTEGER, `removal_percentage` REAL, " +
            "`removed_kg` REAL, `executed_date` TEXT, `labor_crew_size` INTEGER, `timeliness` TEXT, " +
            "PRIMARY KEY(`id`))"

    /** Adds the cache of the logbook timeline (sampling milestones and executed thinnings). */
    val MIGRATION_6_7 = Migration(6, 7) { connection ->
        connection.execSQL(MIGRATION_6_7_SQL)
    }

    internal const val MIGRATION_7_8_SQL =
        "CREATE TABLE IF NOT EXISTS `chill_trackers` (`plot_id` TEXT NOT NULL, " +
            "`accumulated_portions` REAL NOT NULL, `threshold_portions` REAL NOT NULL, " +
            "`days_above_24_celsius` INTEGER NOT NULL, `season_state` TEXT NOT NULL, " +
            "`projected_completion_date` TEXT, `previous_winter_completion_date` TEXT, " +
            "`enso_risk` TEXT NOT NULL, `curve_points_json` TEXT NOT NULL, " +
            "`synced_at_epoch_ms` INTEGER NOT NULL, PRIMARY KEY(`plot_id`))"

    /** Adds the cache of the winter chilling accumulation (Erez model, US22). */
    val MIGRATION_7_8 = Migration(7, 8) { connection ->
        connection.execSQL(MIGRATION_7_8_SQL)
    }

    val ALL = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
}
