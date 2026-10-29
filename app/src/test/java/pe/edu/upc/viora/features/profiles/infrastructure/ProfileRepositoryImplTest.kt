package pe.edu.upc.viora.features.profiles.infrastructure

import java.util.Base64
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.datastore.SessionStore
import pe.edu.upc.viora.core.datastore.TokenCipher
import pe.edu.upc.viora.core.datastore.StoredSession
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.core.network.ApiErrorMapper
import pe.edu.upc.viora.features.iam.InMemoryPreferences
import pe.edu.upc.viora.features.profiles.infrastructure.local.ProfileStore
import pe.edu.upc.viora.features.profiles.infrastructure.remote.ProfileService
import pe.edu.upc.viora.features.profiles.infrastructure.repository.ProfileRepositoryImpl
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Mi cuenta against a fake server: whose profile shows. The account operations are tested in IAM. */
class ProfileRepositoryImplTest {

    private val server = MockWebServer()
    private val json = Json { ignoreUnknownKeys = true }
    private val preferences = InMemoryPreferences()
    private val sessionStore = SessionStore(preferences, TokenCipher.PLAIN)
    private val profileStore = ProfileStore(preferences)
    private lateinit var repository: ProfileRepositoryImpl

    private val accessToken = token(email = "teodoro.mamani@gmail.com")

    @Before
    fun setUp() {
        server.start()
        val service = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ProfileService::class.java)
        repository = ProfileRepositoryImpl(
            service = service,
            apiCaller = ApiCaller(ApiErrorMapper(json), UnconfinedTestDispatcher()),
            profileStore = profileStore,
            sessionStore = sessionStore,
        )
    }

    @After
    fun tearDown() {
        runCatching { server.shutdown() }
    }

    private suspend fun signedInAs(userId: String) =
        sessionStore.save(StoredSession(accessToken, "old-refresh", 0L, userId))

    @Test
    fun `the profile is downloaded with the email of the token, and another account's never shows`() = runTest {
        signedInAs("u-1")
        server.enqueue(
            MockResponse().setBody("""{"id":"p-1","userId":"u-1","fullName":"Teodoro Mamani","country":"PE","phoneNumber":"+51952184337"}"""),
        )

        val profile = (repository.refreshProfile() as AppResult.Success).value

        assertEquals("teodoro.mamani@gmail.com", profile.email)
        assertEquals("Teodoro Mamani", repository.observeProfile().first()?.fullName)

        signedInAs("u-2")
        assertNull(repository.observeProfile().first())
    }

    private fun token(email: String): String {
        val encoder = Base64.getUrlEncoder().withoutPadding()
        val payload = encoder.encodeToString("""{"sub":"u-1","email":"$email"}""".toByteArray())
        return "${encoder.encodeToString("""{"alg":"HS384"}""".toByteArray())}.$payload.signature"
    }
}
