package pe.edu.upc.viora.core.database

import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards the hand-written migration SQL against the schema Room exported for the same version. */
class AppMigrationsTest {

    private fun createSqlByTable(version: Int): Map<String, String> {
        val file = File("schemas/pe.edu.upc.viora.core.database.AppDatabase/$version.json")
        assertTrue("missing exported schema ${file.absolutePath}", file.exists())
        val entities = Json.parseToJsonElement(file.readText()).jsonObject
            .getValue("database").jsonObject.getValue("entities").jsonArray
        return entities.map { it as JsonObject }.associate { entity ->
            val table = entity.getValue("tableName").jsonPrimitive.content
            table to entity.getValue("createSql").jsonPrimitive.content.replace("\${TABLE_NAME}", table)
        }
    }

    private val v5 = createSqlByTable(5)
    private val v6 = createSqlByTable(6)
    private val v7 = createSqlByTable(7)
    private val v8 = createSqlByTable(8)
    private val v9 = createSqlByTable(9)
    private val v10 = createSqlByTable(10)
    private val v11 = createSqlByTable(11)
    private val v12 = createSqlByTable(12)
    private val v13 = createSqlByTable(13)
    private val v14 = createSqlByTable(14)

    @Test
    fun `new tables are created exactly as the exported v5 schema`() {
        val created = AppMigrations.MIGRATION_4_5_STATEMENTS.filter { it.startsWith("CREATE TABLE") }

        assertEquals(setOf("pending_settlements", "agroclimatic_incidents"), created.map { tableOf(it) }.toSet())
        created.forEach { sql -> assertEquals(v5.getValue(tableOf(sql)), sql) }
    }

    @Test
    fun `the altered settlements table ends up as the exported v5 schema`() {
        val added = AppMigrations.MIGRATION_4_5_STATEMENTS
            .filter { it.startsWith("ALTER TABLE `harvest_settlements` ADD COLUMN") }
            .map { it.substringAfter("ADD COLUMN ") }
        val v4Table = createSqlByTable(4).getValue("harvest_settlements")
        val migrated = v4Table.replace(", PRIMARY KEY(`id`))", ", ${added.joinToString(", ")}, PRIMARY KEY(`id`))")

        assertEquals(4, added.size)
        assertEquals(v5.getValue("harvest_settlements"), migrated)
    }

    @Test
    fun `every other v5 table is untouched since v4`() {
        val v4 = createSqlByTable(4)
        val changed = setOf("harvest_settlements", "pending_settlements")

        (v5.keys - changed).forEach { assertEquals(v4[it], v5[it]) }
    }

    @Test
    fun `new draft_tree_samples table in v6 matches migration`() {
        assertEquals(v6.getValue("draft_tree_samples"), AppMigrations.MIGRATION_5_6_SQL)
    }

    @Test
    fun `every other v6 table is untouched since v5`() {
        val changed = setOf("draft_tree_samples")
        (v6.keys - changed).forEach { assertEquals(v5[it], v6[it]) }
    }

    @Test
    fun `new thinning_events table in v7 matches migration`() {
        assertEquals(v7.getValue("thinning_events"), AppMigrations.MIGRATION_6_7_SQL)
    }

    @Test
    fun `every other v7 table is untouched since v6`() {
        assertEquals(v6.keys + "thinning_events", v7.keys)
        v6.keys.forEach { assertEquals(v6[it], v7[it]) }
    }

    @Test
    fun `new chill_trackers table in v8 matches migration`() {
        assertEquals(v8.getValue("chill_trackers"), AppMigrations.MIGRATION_7_8_SQL)
    }

    @Test
    fun `every other v8 table is untouched since v7`() {
        assertEquals(v7.keys + "chill_trackers", v8.keys)
        v7.keys.forEach { assertEquals(v7[it], v8[it]) }
    }

    @Test
    fun `new thinning_prescriptions table in v9 matches migration`() {
        assertEquals(v9.getValue("thinning_prescriptions"), AppMigrations.MIGRATION_8_9_SQL)
    }

    @Test
    fun `the altered plots table ends up as the exported v9 schema`() {
        val added = AppMigrations.MIGRATION_8_9_PLOTS_SQL.substringAfter("ADD COLUMN ")
        val migrated = v8.getValue("plots").replace(", PRIMARY KEY(`id`))", ", $added, PRIMARY KEY(`id`))")

        assertEquals(v9.getValue("plots"), migrated)
    }

    @Test
    fun `every other v9 table is untouched since v8`() {
        assertEquals(v8.keys + "thinning_prescriptions", v9.keys)
        (v8.keys - "plots").forEach { assertEquals(v8[it], v9[it]) }
    }

    @Test
    fun `new agronomic_dossiers table in v10 matches migration`() {
        assertEquals(v10.getValue("agronomic_dossiers"), AppMigrations.MIGRATION_9_10_SQL)
    }

    @Test
    fun `every other v10 table is untouched since v9`() {
        assertEquals(v9.keys + "agronomic_dossiers", v10.keys)
        v9.keys.forEach { assertEquals(v9[it], v10[it]) }
    }

    @Test
    fun `every version from 1 to the current one has a migration`() {
        (1 until 13).forEach { from ->
            assertTrue("missing $from -> ${from + 1}", AppMigrations.ALL.any { it.startVersion == from && it.endVersion == from + 1 })
        }
    }

    private fun tableOf(createSql: String) = createSql.substringAfter("EXISTS `").substringBefore("`")

    @Test fun `field notes migration matches the exported v14 schema and leaves every other table intact`() {
        assertEquals(v14.getValue("field_notes"), AppMigrations.MIGRATION_13_14_STATEMENTS.first())
        assertEquals(v13.keys + "field_notes", v14.keys)
        v13.keys.forEach { assertEquals(v13[it], v14[it]) }
    }

    @Test fun `purchase journal migration matches its schema and leaves every existing table intact`() {
        assertEquals(v13.getValue("individual_purchase_journal"), AppMigrations.MIGRATION_12_13_SQL)
        assertEquals(v12.keys + "individual_purchase_journal", v13.keys)
        v12.keys.forEach { assertEquals(v12[it], v13[it]) }
    }

    @Test
    fun `producer offer migration matches the exact exported v11 schema`() {
        assertEquals(v11.getValue("producer_plan_offers"), AppMigrations.MIGRATION_10_11_SQL)
    }

    @Test
    fun `the producer catalog leaves all previous tables untouched`() {
        assertEquals(v10.keys + "producer_plan_offers", v11.keys)
        v10.keys.forEach { assertEquals(v10[it], v11[it]) }
    }

    @Test
    fun `tax inclusion migration only adds its declared column and preserves all other tables`() {
        val column = AppMigrations.MIGRATION_11_12_SQL.substringAfter("ADD COLUMN ")
        assertEquals(v11.keys, v12.keys)
        assertEquals(v11.getValue("producer_plan_offers").replace(", PRIMARY KEY(`code`))", ", $column, PRIMARY KEY(`code`))"),
            v12.getValue("producer_plan_offers"))
        (v11.keys - "producer_plan_offers").forEach { assertEquals(v11[it], v12[it]) }
    }
}
