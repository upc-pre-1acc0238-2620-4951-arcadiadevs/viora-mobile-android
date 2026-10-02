package pe.edu.upc.viora.core.network

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.GET

@Serializable
private data class PingDto(val message: String)

private interface TestService {
    @GET("ping")
    suspend fun ping(): Response<PingDto>

    @DELETE("things/1")
    suspend fun delete(): Response<Unit>
}

@OptIn(ExperimentalCoroutinesApi::class)
class ApiCallerTest {

    private val server = MockWebServer()
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }
    private lateinit var service: TestService
    private lateinit var caller: ApiCaller

    @Before
    fun setUp() {
        server.start()
        val client = OkHttpClient.Builder()
            .readTimeout(300, TimeUnit.MILLISECONDS)
            .build()
        service = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TestService::class.java)
        caller = ApiCaller(ApiErrorMapper(json), UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        runCatching { server.shutdown() }
    }

    private fun problem(status: Int, code: String, detail: String) = MockResponse()
        .setResponseCode(status)
        .setHeader("Content-Type", "application/problem+json")
        .setBody("""{"type":"https://api.viora.com/errors/x","title":"t","status":$status,"detail":"$detail","code":"$code","timestamp":"2026-10-01T00:00:00Z","extra":"ignored"}""")

    private suspend fun ping(): AppResult<PingDto> = caller.call { service.ping() }

    @Test
    fun `success returns the parsed body`() = runTest {
        server.enqueue(MockResponse().setBody("""{"message":"pong"}"""))

        val result = ping()

        assertEquals(AppResult.Success(PingDto("pong")), result)
    }

    @Test
    fun `a success body missing a required field fails instead of producing a null`() = runTest {
        server.enqueue(MockResponse().setBody("""{"other":"x"}"""))

        val error = (ping() as AppResult.Failure).error

        assertTrue(error is AppError.Unknown)
    }

    @Test
    fun `400 maps to Validation with backend detail and code`() = runTest {
        server.enqueue(problem(400, "VALIDATION_ERROR", "Plot name cannot be blank"))

        val error = (ping() as AppResult.Failure).error

        assertEquals(AppError.Validation("Plot name cannot be blank", "VALIDATION_ERROR"), error)
    }

    @Test
    fun `404 maps to NotFound`() = runTest {
        server.enqueue(problem(404, "PLOT_NOT_FOUND", "Plot not found"))

        val error = (ping() as AppResult.Failure).error

        assertEquals(AppError.NotFound("Plot not found", "PLOT_NOT_FOUND"), error)
    }

    @Test
    fun `409 maps to Conflict`() = runTest {
        server.enqueue(problem(409, "PLOT_CONFLICT", "Duplicate name"))

        val error = (ping() as AppResult.Failure).error

        assertEquals(AppError.Conflict("Duplicate name", "PLOT_CONFLICT"), error)
    }

    @Test
    fun `412 maps to PreconditionFailed so the screen can reload`() = runTest {
        server.enqueue(problem(412, "PLOT_PRECONDITION_FAILED", "Revision changed"))

        val error = (ping() as AppResult.Failure).error

        assertEquals(AppError.PreconditionFailed("Revision changed", "PLOT_PRECONDITION_FAILED"), error)
    }

    @Test
    fun `5xx maps to Server with the status`() = runTest {
        server.enqueue(MockResponse().setResponseCode(503))

        val error = (ping() as AppResult.Failure).error

        assertEquals(AppError.Server(status = 503, detail = null), error)
    }

    @Test
    fun `an error body that is not JSON still yields the right error type`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404).setBody("<html>Not found</html>"))

        val error = (ping() as AppResult.Failure).error

        assertEquals(AppError.NotFound(detail = null, code = null), error)
    }

    @Test
    fun `an unreachable server maps to Offline`() = runTest {
        server.shutdown()

        val error = (ping() as AppResult.Failure).error

        assertEquals(AppError.Offline, error)
    }

    @Test
    fun `a server that never answers maps to Timeout`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE))

        val error = (ping() as AppResult.Failure).error

        assertEquals(AppError.Timeout, error)
    }

    @Test
    fun `call fails when a successful response has no body`() = runTest {
        server.enqueue(MockResponse().setResponseCode(204))

        val error = (ping() as AppResult.Failure).error

        assertTrue(error is AppError.Unknown)
    }

    @Test
    fun `callUnit succeeds on 204 without a body`() = runTest {
        server.enqueue(MockResponse().setResponseCode(204))

        val result = caller.callUnit { service.delete() }

        assertEquals(AppResult.Success(Unit), result)
    }

    @Test
    fun `callUnit still reports errors`() = runTest {
        server.enqueue(problem(409, "PLOT_CONFLICT", "Already removed"))

        val error = (caller.callUnit { service.delete() } as AppResult.Failure).error

        assertEquals(AppError.Conflict("Already removed", "PLOT_CONFLICT"), error)
    }
}
