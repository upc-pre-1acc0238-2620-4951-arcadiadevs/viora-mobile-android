package pe.edu.upc.viora.features.subscription

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room3.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import pe.edu.upc.viora.core.database.AppDatabase
import pe.edu.upc.viora.core.database.AppMigrations
import pe.edu.upc.viora.core.database.CacheMetadataEntity
import pe.edu.upc.viora.features.subscription.infrastructure.local.PRODUCER_PLAN_CATALOG_KEY
import pe.edu.upc.viora.features.subscription.infrastructure.local.ProducerPlanEntity

@RunWith(AndroidJUnit4::class)
class ProducerPlanCacheInstrumentedTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private fun offer(rate: String = "55.123456789012345678") = ProducerPlanEntity(
        "PRODUCER_ANNUAL", PRODUCER_PLAN_CATALOG_KEY, "PEN", rate, "0.18", "0.5", "0.5", 12, "test")
    private fun open(name: String) = Room.databaseBuilder<AppDatabase>(context, name)
        .addMigrations(*AppMigrations.ALL).build()

    @Test fun relationPreservesExactDecimalsAndSuccessfulEmptyCatalog() = runBlocking {
        val name = "producer-cache-${UUID.randomUUID()}"
        val db = open(name)
        try {
            db.cacheMetadataDao().upsert(CacheMetadataEntity("other", 99))
            val dao = db.producerPlanDao()
            dao.replaceCatalog(listOf(offer()), 100)
            val saved = dao.observeCatalog(PRODUCER_PLAN_CATALOG_KEY).first()!!
            assertEquals(offer(), saved.offers.single())
            assertEquals(100L, saved.metadata.fetchedAtEpochMs)
            dao.replaceCatalog(emptyList(), 200)
            val empty = dao.observeCatalog(PRODUCER_PLAN_CATALOG_KEY).first()!!
            assertTrue(empty.offers.isEmpty())
            assertEquals(200L, empty.metadata.fetchedAtEpochMs)
            assertEquals(99L, db.cacheMetadataDao().fetchedAt("other"))
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun failedTimestampWriteRollsBackOfferReplacement() = runBlocking {
        val name = "producer-rollback-${UUID.randomUUID()}"
        try {
            val initial = open(name)
            try { initial.producerPlanDao().replaceCatalog(listOf(offer()), 100) } finally { initial.close() }
            val sqlite = SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, SQLiteDatabase.OPEN_READWRITE)
            try {
                sqlite.execSQL("CREATE TRIGGER fail_catalog_stamp BEFORE INSERT ON cache_metadata " +
                    "WHEN NEW.resource_key = '$PRODUCER_PLAN_CATALOG_KEY' AND NEW.fetched_at_epoch_ms = 200 " +
                    "BEGIN SELECT RAISE(ABORT, 'test rollback'); END")
            } finally { sqlite.close() }
            val db = open(name)
            try {
                var failed = false
                try { db.producerPlanDao().replaceCatalog(listOf(offer("99")), 200) }
                catch (_: Exception) { failed = true }
                assertTrue(failed)
                val saved = db.producerPlanDao().observeCatalog(PRODUCER_PLAN_CATALOG_KEY).first()!!
                assertEquals(offer(), saved.offers.single())
                assertEquals(100L, saved.metadata.fetchedAtEpochMs)
            } finally { db.close() }
        } finally { context.deleteDatabase(name) }
    }

    @Test fun migrationFromExportedVersionTenKeepsExistingMetadata() = runBlocking {
        val name = "producer-migration-${UUID.randomUUID()}"
        try {
            val schema = contextForTests().assets.open("pe.edu.upc.viora.core.database.AppDatabase/10.json")
                .bufferedReader().use { JSONObject(it.readText()).getJSONObject("database") }
            val sqlite = context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null)
            try {
                val entities = schema.getJSONArray("entities")
                for (index in 0 until entities.length()) {
                    val entity = entities.getJSONObject(index)
                    val table = entity.getString("tableName")
                    sqlite.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
                    val indices = entity.optJSONArray("indices") ?: org.json.JSONArray()
                    for (i in 0 until indices.length()) {
                        sqlite.execSQL(indices.getJSONObject(i).getString("createSql").replace("\${TABLE_NAME}", table))
                    }
                }
                sqlite.execSQL("INSERT INTO cache_metadata VALUES ('migration:keep', 123)")
                sqlite.version = 10
            } finally { sqlite.close() }
            val db = open(name)
            try {
                assertEquals(123L, db.cacheMetadataDao().fetchedAt("migration:keep"))
                db.producerPlanDao().replaceCatalog(listOf(offer()), 300)
                assertEquals(offer(), db.producerPlanDao().observeCatalog(PRODUCER_PLAN_CATALOG_KEY).first()!!.offers.single())
            } finally { db.close() }
        } finally { context.deleteDatabase(name) }
    }

    private fun contextForTests() = androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().context
}
