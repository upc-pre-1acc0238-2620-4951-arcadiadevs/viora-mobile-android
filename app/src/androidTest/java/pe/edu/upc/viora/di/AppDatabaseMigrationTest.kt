package pe.edu.upc.viora.di

import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs the real migrations on a database file, from every version the app ever shipped up to the current one, and
 * lets Room compare the result with the exported schema (app/schemas, packaged as test assets).
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = instrumentation,
        file = context.getDatabasePath(TEST_DB),
        driver = AndroidSQLiteDriver(),
        databaseClass = AppDatabase::class,
    )

    @Before
    fun deleteDatabase() {
        context.deleteDatabase(TEST_DB)
    }

    @Test
    fun migratesFromVersion1ToTheCurrentSchemaKeepingThePlots() = runBlocking {
        helper.createDatabase(1).apply {
            execSQL(
                "INSERT INTO plots (id, name, variety, area_ha, tree_density, row_spacing_m, tree_spacing_m, " +
                    "polygon_geojson, last_pruning_date, status, revision) " +
                    "VALUES ('plot-1', 'La Yarada 02', 'SEVILLANA', 2.5, 400, 5.0, 5.0, '{}', NULL, 'ACTIVE', 3)",
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(CURRENT_VERSION, AppMigrations.ALL.toList())

        migrated.prepare("SELECT name, area_ha, revision FROM plots WHERE id = 'plot-1'").use { row ->
            assertTrue("the plot saved at version 1 was lost", row.step())
            assertEquals("La Yarada 02", row.getText(0))
            assertEquals(2.5, row.getDouble(1), 0.0)
            assertEquals(3L, row.getLong(2))
        }
        migrated.close()
    }

    @Test
    fun everyOlderVersionMigratesToTheCurrentSchema() = runBlocking {
        for (version in 1 until CURRENT_VERSION) {
            context.deleteDatabase(TEST_DB)
            helper.createDatabase(version).close()
            helper.runMigrationsAndValidate(CURRENT_VERSION, AppMigrations.ALL.toList()).close()
        }
    }

    private companion object {
        const val TEST_DB = "migration-test.db"
        const val CURRENT_VERSION = 15
    }
}
