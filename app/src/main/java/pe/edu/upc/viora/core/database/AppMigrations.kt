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
        "CREATE TABLE IF NOT EXISTS `chill_trackers` (`plot_id` TEXT NOT NULL, `season_year` INTEGER NOT NULL, " +
            "`accumulated_portions` REAL NOT NULL, `threshold_portions` REAL NOT NULL, " +
            "`season_state` TEXT NOT NULL, `evaluated_through` TEXT, `completion_date` TEXT, " +
            "`projection_status` TEXT NOT NULL, `projected_completion_date` TEXT, " +
            "`days_above_24_celsius` INTEGER NOT NULL, `current_warm_streak_days` INTEGER NOT NULL, " +
            "`longest_warm_streak_days` INTEGER NOT NULL, `thermal_anomaly` TEXT NOT NULL, " +
            "`previous_season_year` INTEGER, `previous_season_portions` REAL, " +
            "`previous_season_completion_date` TEXT, `curve_points_json` TEXT NOT NULL, " +
            "`synced_at_epoch_ms` INTEGER NOT NULL, PRIMARY KEY(`plot_id`))"

    /** Adds the cache of the winter chill computed by the backend (Dynamic Model, US22/US23). */
    val MIGRATION_7_8 = Migration(7, 8) { connection ->
        connection.execSQL(MIGRATION_7_8_SQL)
    }

    internal const val MIGRATION_8_9_SQL =
        "CREATE TABLE IF NOT EXISTS `thinning_prescriptions` (`plot_id` TEXT NOT NULL, `campaign_year` INTEGER NOT NULL, " +
            "`payload_json` TEXT, `synced_at_epoch_ms` INTEGER NOT NULL, `execution_json` TEXT, PRIMARY KEY(`plot_id`, `campaign_year`))"

    internal const val MIGRATION_8_9_PLOTS_SQL = "ALTER TABLE `plots` ADD COLUMN `registered_at` TEXT"

    /**
     * Adds the cache of the thinning plan of each plot and campaign and of its recorded thinning (P61-P63), and the
     * registration date of the plots (P60).
     */
    val MIGRATION_8_9 = Migration(8, 9) { connection ->
        connection.execSQL(MIGRATION_8_9_SQL)
        connection.execSQL(MIGRATION_8_9_PLOTS_SQL)
    }

    internal const val MIGRATION_9_10_SQL =
        "CREATE TABLE IF NOT EXISTS `agronomic_dossiers` (`plot_id` TEXT NOT NULL, `campaign_year` INTEGER NOT NULL, " +
            "`payload_json` TEXT NOT NULL, `pdf_path` TEXT, `pdf_saved_at_epoch_ms` INTEGER, PRIMARY KEY(`plot_id`, `campaign_year`))"

    /** Adds the cache of the sealed agronomic dossiers and of their downloaded PDF (P76-P78). */
    val MIGRATION_9_10 = Migration(9, 10) { connection ->
        connection.execSQL(MIGRATION_9_10_SQL)
    }

    internal const val MIGRATION_10_11_SQL =
        "CREATE TABLE IF NOT EXISTS `producer_plan_offers` (`code` TEXT NOT NULL, `catalog_key` TEXT NOT NULL, " +
            "`currency` TEXT NOT NULL, `monthly_rate_per_ha` TEXT NOT NULL, `tax_rate` TEXT NOT NULL, " +
            "`minimum_quota_ha` TEXT NOT NULL, `quota_increment_ha` TEXT NOT NULL, `billing_months` INTEGER NOT NULL, " +
            "`tariff_version` TEXT NOT NULL, PRIMARY KEY(`code`))"

    /** Adds the exact-decimal producer offer cache without altering agronomic or queued records. */
    val MIGRATION_10_11 = Migration(10, 11) { connection -> connection.execSQL(MIGRATION_10_11_SQL) }

    internal const val MIGRATION_11_12_SQL =
        "ALTER TABLE `producer_plan_offers` ADD COLUMN `tax_included` INTEGER NOT NULL DEFAULT 0"

    /** Invalidates only the old public catalog; account, agronomic and queued data stay intact. */
    val MIGRATION_11_12 = Migration(11, 12) { connection ->
        connection.execSQL(MIGRATION_11_12_SQL)
        connection.execSQL("DELETE FROM `producer_plan_offers`")
        connection.execSQL("DELETE FROM `cache_metadata` WHERE `resource_key` = 'subscription:producer-plans'")
    }

    internal const val MIGRATION_12_13_SQL =
        "CREATE TABLE IF NOT EXISTS `individual_purchase_journal` (`owner` TEXT NOT NULL, `plan` TEXT NOT NULL, " +
            "`quota` TEXT NOT NULL, `createKey` TEXT NOT NULL, `checkoutKey` TEXT NOT NULL, `contractJson` TEXT, " +
            "`intentId` TEXT, `checkoutUrl` TEXT, `expiresAt` TEXT, PRIMARY KEY(`owner`))"

    val MIGRATION_12_13 = Migration(12, 13) { connection -> connection.execSQL(MIGRATION_12_13_SQL) }

    internal val MIGRATION_13_14_STATEMENTS = listOf(
        "CREATE TABLE IF NOT EXISTS `field_notes` (`id` TEXT NOT NULL, `plot_id` TEXT NOT NULL, `type` TEXT NOT NULL, " +
            "`text` TEXT NOT NULL, `observed_on` TEXT NOT NULL, `created_at` INTEGER NOT NULL, `has_photo` INTEGER NOT NULL, " +
            "`photo_path` TEXT, `photo_pending` INTEGER NOT NULL, `sync_state` TEXT NOT NULL, `last_error_code` TEXT, " +
            "PRIMARY KEY(`id`))",
        "CREATE INDEX IF NOT EXISTS `index_field_notes_plot_id` ON `field_notes` (`plot_id`)",
    )

    /** Logbook notes (P75), written on the phone and sent when there is signal. */
    val MIGRATION_13_14 = Migration(13, 14) { connection ->
        MIGRATION_13_14_STATEMENTS.forEach { connection.execSQL(it) }
    }

    val ALL = arrayOf(
        MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9,
        MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14,
    )
}
