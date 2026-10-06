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
    fun `migrations 3 to 4 and 4 to 5 are registered`() {
        assertTrue(AppMigrations.ALL.any { it.startVersion == 3 && it.endVersion == 4 })
        assertTrue(AppMigrations.ALL.any { it.startVersion == 4 && it.endVersion == 5 })
    }

    private fun tableOf(createSql: String) = createSql.substringAfter("EXISTS `").substringBefore("`")
}
