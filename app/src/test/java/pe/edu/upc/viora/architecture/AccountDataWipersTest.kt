package pe.edu.upc.viora.architecture

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Each bounded context empties its own tables when the account leaves the phone (step 5): every entity of
 * `AppDatabase` has a `DELETE FROM` of its whole table in its DAO, and a wiper of the same context calls it. A table
 * added without its wiper fails here instead of keeping the previous account's data.
 */
class AccountDataWipersTest {

    private val root = File("src/main/java/pe/edu/upc/viora")
    private val database = File(root, "di/AppDatabase.kt")

    private data class Table(val entity: String, val name: String, val dao: File)

    private fun kotlinFiles(): Sequence<File> = root.walkTopDown().filter { it.extension == "kt" }

    private fun tables(): List<Table> {
        val entities = Regex("""(\w+)Entity::class""").findAll(database.readText()).map { it.groupValues[1] }.toList()
        val files = kotlinFiles().toList()
        return entities.map { entity ->
            val entityFile = files.single { Regex("""class ${entity}Entity\b""") in it.readText() }
            val table = Regex("""tableName = "(\w+)"""").find(entityFile.readText())!!.groupValues[1]
            val dao = files.single { Regex("""interface ${entity}Dao\b""") in it.readText() }
            Table(entity, table, dao)
        }
    }

    @Test
    fun `the tables are found`() {
        assertTrue("run from the app module: ${root.absolutePath}", database.isFile)
        assertTrue(tables().size >= 18)
    }

    @Test
    fun `every table can be emptied by its dao`() {
        val missing = tables().filterNot { """@Query("DELETE FROM ${it.name}")""" in it.dao.readText() }.map { it.name }

        assertEquals(emptyList<String>(), missing)
    }

    @Test
    fun `every table is emptied by a wiper of its own context`() {
        val wipers = kotlinFiles().filter { ": AccountDataWiper" in it.readText() }.toList() + File(root, "di/DatabaseModule.kt")
        val missing = tables().filterNot { table ->
            val context = table.dao.parentFile.parentFile.parentFile
            wipers.any { wiper ->
                (wiper.startsWith(context) || table.entity == "CacheMetadata") &&
                    Regex("""\b${table.entity}Dao\b""") in wiper.readText() &&
                    ".wipeAll()" in wiper.readText()
            }
        }.map { it.name }

        assertEquals(emptyList<String>(), missing)
    }
}
