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
import pe.edu.upc.viora.di.AppDatabase
import pe.edu.upc.viora.di.AppMigrations
import pe.edu.upc.viora.core.database.CacheMetadataEntity
import pe.edu.upc.viora.features.subscription.infrastructure.local.PRODUCER_PLAN_CATALOG_KEY
import pe.edu.upc.viora.features.subscription.infrastructure.local.ProducerPlanEntity

@RunWith(AndroidJUnit4::class)
class ProducerPlanCacheInstrumentedTest {
    @Test fun purchaseJournalSurvivesReopeningAndKeepsAccountsSeparate() = runBlocking {
        val name = "purchase-journal-${UUID.randomUUID()}"
        val row = pe.edu.upc.viora.features.subscription.infrastructure.local.IndividualPurchaseEntity(
            "owner-a", "PRODUCER_ANNUAL", "0.5", "create-key", "checkout-key", null, null, null, null)
        try {
            val initial = open(name)
            try {
                initial.individualPurchaseDao().insert(row)
                initial.cacheMetadataDao().upsert(CacheMetadataEntity("keep", 123))
            } finally { initial.close() }
            val reopened = open(name)
            try {
                assertEquals(row, reopened.individualPurchaseDao().find("owner-a"))
                assertNull(reopened.individualPurchaseDao().find("owner-b"))
                reopened.individualPurchaseDao().insert(row.copy(createKey = "must-not-replace"))
                assertEquals(row, reopened.individualPurchaseDao().find("owner-a"))
                assertEquals(123L, reopened.cacheMetadataDao().fetchedAt("keep"))
            } finally { reopened.close() }
        } finally { context.deleteDatabase(name) }
    }
    private val context: Context = ApplicationProvider.getApplicationContext()
    private fun offer(rate: String = "55.123456789012345678") = ProducerPlanEntity(
        "PRODUCER_ANNUAL", PRODUCER_PLAN_CATALOG_KEY, "PEN", rate, "0.18", "0.5", "0.5", 12, "test", true)
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
            val schema = contextForTests().assets.open("pe.edu.upc.viora.di.AppDatabase/10.json")
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

    @Test fun migrationFromElevenInvalidatesOnlyLegacyOffersAndPreservesQueuedData() = runBlocking {
        val name = "producer-tax-migration-${UUID.randomUUID()}"
        try {
            val schema = contextForTests().assets.open("pe.edu.upc.viora.di.AppDatabase/11.json")
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
                sqlite.execSQL("INSERT INTO producer_plan_offers VALUES ('PRODUCER_ANNUAL', " +
                    "'subscription:producer-plans', 'PEN', '55', '0.18', '0.5', '0.5', 12, 'legacy')")
                sqlite.execSQL("INSERT INTO cache_metadata VALUES ('subscription:producer-plans', 100), ('keep', 99)")
                sqlite.execSQL("INSERT INTO pending_settlements (plot_id, campaign_year, green_olives_kg, " +
                    "black_olives_kg, weighed_on, idempotency_key, status, attempt_count, created_at, updated_at) " +
                    "VALUES ('keep-plot', 2026, 1, 0, '2026-10-22', 'keep-key', 'PENDING', 0, 1, 1)")
                sqlite.version = 11
            } finally { sqlite.close() }
            val db = open(name)
            try {
                assertNull(db.producerPlanDao().observeCatalog(PRODUCER_PLAN_CATALOG_KEY).first())
                assertNull(db.cacheMetadataDao().fetchedAt(PRODUCER_PLAN_CATALOG_KEY))
                assertEquals(99L, db.cacheMetadataDao().fetchedAt("keep"))
                db.producerPlanDao().replaceCatalog(listOf(offer()), 300)
                assertTrue(db.producerPlanDao().observeCatalog(PRODUCER_PLAN_CATALOG_KEY).first()!!.offers.single().taxIncluded)
            } finally { db.close() }
            val verified = SQLiteDatabase.openDatabase(context.getDatabasePath(name).path, null, SQLiteDatabase.OPEN_READONLY)
            try {
                assertEquals(13, verified.version)
                verified.rawQuery("SELECT idempotency_key FROM pending_settlements WHERE plot_id = 'keep-plot'", null).use {
                    assertTrue(it.moveToFirst())
                    assertEquals("keep-key", it.getString(0))
                }
            } finally { verified.close() }
        } finally { context.deleteDatabase(name) }
    }
}
