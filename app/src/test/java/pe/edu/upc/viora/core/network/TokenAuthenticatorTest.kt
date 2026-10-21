package pe.edu.upc.viora.core.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TokenAuthenticatorTest {

    private val server = MockWebServer()

    @Before
    fun setUp() = server.start()

    @After
    fun tearDown() = server.shutdown()

    /** Hands out "renewed" once the old token is reported as rejected. */
    private class FakeRefresher(private val renewed: String?) : SessionRefresher {
        var rejectedSeen: String? = null
        override fun freshAccessToken(rejected: String?): String? {
            if (rejected != null) rejectedSeen = rejected
            return if (rejected == null) "old" else renewed
        }
    }

    private fun client(refresher: SessionRefresher) = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor { refresher.freshAccessToken() })
        .authenticator(TokenAuthenticator(refresher))
        .build()

    @Test
    fun `a 401 is repeated once with the renewed token`() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setBody("ok"))
        val refresher = FakeRefresher(renewed = "new")

        val response = client(refresher).newCall(Request.Builder().url(server.url("/plots")).build()).execute()

        assertEquals(200, response.code)
        assertEquals("old", refresher.rejectedSeen)
        assertEquals("Bearer old", server.takeRequest().getHeader("Authorization"))
        assertEquals("Bearer new", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `the 401 reaches the caller when the session cannot be renewed`() {
        server.enqueue(MockResponse().setResponseCode(401))

        val response = client(FakeRefresher(renewed = null)).newCall(Request.Builder().url(server.url("/plots")).build()).execute()

        assertEquals(401, response.code)
        assertEquals(1, server.requestCount)
    }
}
