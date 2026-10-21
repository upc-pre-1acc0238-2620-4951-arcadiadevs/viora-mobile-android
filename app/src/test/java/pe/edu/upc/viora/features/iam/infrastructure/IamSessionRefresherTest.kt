package pe.edu.upc.viora.features.iam.infrastructure

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.features.iam.InMemoryPreferences
import pe.edu.upc.viora.core.datastore.SessionEvents
import pe.edu.upc.viora.core.datastore.SessionStore
import pe.edu.upc.viora.core.datastore.StoredSession
import pe.edu.upc.viora.features.iam.infrastructure.remote.IamService
import pe.edu.upc.viora.features.iam.infrastructure.session.IamSessionRefresher
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class IamSessionRefresherTest {

    private val server = MockWebServer()
    private val now = Instant.parse("2026-10-21T15:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)
    private val events = SessionEvents()
    private lateinit var store: SessionStore
    private lateinit var refresher: IamSessionRefresher

    @Before
    fun setUp() {
        server.start()
        val dataStore = InMemoryPreferences()
        store = SessionStore(dataStore)
        val json = Json { ignoreUnknownKeys = true }
        val service = Retrofit.Builder()
            .baseUrl(server.url("/api/v1/"))
            .client(OkHttpClient.Builder().readTimeout(2, TimeUnit.SECONDS).build())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(IamService::class.java)
        refresher = IamSessionRefresher(store, service, events, clock)
    }

    @After
    fun tearDown() {
        runCatching { server.shutdown() }
    }

    private fun saveSession(expiresInMillis: Long) = runBlocking {
        store.save(StoredSession("old-access", "old-refresh", now.toEpochMilli() + expiresInMillis, "user-1"))
    }

    private fun enqueueRenewed() = server.enqueue(
        MockResponse().setBody(
            """{"accessToken":"new-access","refreshToken":"new-refresh","tokenType":"Bearer","expiresIn":900,"userId":"user-1","role":"ROLE_PRODUCER"}""",
        ),
    )

    @Test
    fun `keeps the token while it has more than a minute left`() {
        saveSession(expiresInMillis = 5 * 60_000)

        assertEquals("old-access", refresher.freshAccessToken())
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `renews a token about to expire and stores both new tokens`() {
        saveSession(expiresInMillis = 30_000)
        enqueueRenewed()

        assertEquals("new-access", refresher.freshAccessToken())

        val request = server.takeRequest()
        assertEquals("/api/v1/auth/refresh-tokens", request.path)
        assertTrue(request.body.readUtf8().contains("\"refreshToken\":\"old-refresh\""))
        val saved = runBlocking { store.session() }!!
        assertEquals("new-refresh", saved.refreshToken)
        assertEquals(now.toEpochMilli() + 900_000, saved.expiresAtMillis)
    }

    @Test
    fun `renews when the server rejected the current token`() {
        saveSession(expiresInMillis = 5 * 60_000)
        enqueueRenewed()

        assertEquals("new-access", refresher.freshAccessToken(rejected = "old-access"))
    }

    @Test
    fun `a token rejected after another request renewed it just takes the new one`() {
        saveSession(expiresInMillis = 30_000)
        enqueueRenewed()
        refresher.freshAccessToken()

        // A request that was sent with the old token comes back with 401: no second rotation.
        assertEquals("new-access", refresher.freshAccessToken(rejected = "old-access"))
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `a refused refresh token ends the session and tells the app`() = runBlocking {
        saveSession(expiresInMillis = 0)
        server.enqueue(MockResponse().setResponseCode(403))
        val expired = async(Dispatchers.Default) { withTimeout(2_000) { events.expired.first() } }
        Thread.sleep(100)

        assertNull(refresher.freshAccessToken())
        assertNull(store.session())
        expired.await()
        Unit
    }

    @Test
    fun `offline keeps the current token and the session`() {
        saveSession(expiresInMillis = 0)
        server.shutdown()

        assertEquals("old-access", refresher.freshAccessToken())
        assertEquals("old-refresh", runBlocking { store.session() }!!.refreshToken)
    }

    @Test
    fun `no session means no token`() {
        assertNull(refresher.freshAccessToken())
    }
}
