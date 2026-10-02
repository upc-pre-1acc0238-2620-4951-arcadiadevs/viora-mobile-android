package pe.edu.upc.viora.core.network

import java.util.Locale
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class InterceptorsTest {

    private val server = MockWebServer()

    @Before
    fun setUp() {
        server.start()
        server.enqueue(MockResponse().setBody("{}"))
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun execute(client: OkHttpClient) =
        client.newCall(Request.Builder().url(server.url("/ping")).build()).execute().close()

    @Test
    fun `AuthInterceptor adds the bearer token when a session exists`() {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { "abc123" })
            .build()

        execute(client)

        assertEquals("Bearer abc123", server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `AuthInterceptor sends no header without a session`() {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { null })
            .build()

        execute(client)

        assertNull(server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `AcceptLanguageInterceptor forwards a supported language`() {
        val client = OkHttpClient.Builder()
            .addInterceptor(AcceptLanguageInterceptor { Locale.ENGLISH })
            .build()

        execute(client)

        assertEquals("en", server.takeRequest().getHeader("Accept-Language"))
    }

    @Test
    fun `AcceptLanguageInterceptor falls back to Spanish for unsupported languages`() {
        val client = OkHttpClient.Builder()
            .addInterceptor(AcceptLanguageInterceptor { Locale.FRENCH })
            .build()

        execute(client)

        assertEquals("es", server.takeRequest().getHeader("Accept-Language"))
    }
}
