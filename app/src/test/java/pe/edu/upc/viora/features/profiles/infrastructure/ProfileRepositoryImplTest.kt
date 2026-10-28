package pe.edu.upc.viora.features.profiles.infrastructure

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.database.LocalDataOwner
import pe.edu.upc.viora.core.datastore.SessionStore
import pe.edu.upc.viora.core.datastore.StoredSession
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.core.network.ApiErrorMapper
import pe.edu.upc.viora.features.iam.InMemoryPreferences
import pe.edu.upc.viora.features.profiles.domain.entity.UserProfile
import pe.edu.upc.viora.features.profiles.domain.repository.ProfileRepository
import pe.edu.upc.viora.features.profiles.domain.valueobject.ProfileId
import pe.edu.upc.viora.features.profiles.infrastructure.local.ProfileStore
import pe.edu.upc.viora.features.profiles.infrastructure.remote.ProfileService
import pe.edu.upc.viora.features.profiles.infrastructure.repository.ProfileRepositoryImpl
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Mi cuenta against a fake server: the session after a password change, sign-out and whose profile shows. */
class ProfileRepositoryImplTest {

    private val server = MockWebServer()
    private val json = Json { ignoreUnknownKeys = true }
    private val preferences = InMemoryPreferences()
    private val sessionStore = SessionStore(preferences)
    private val profileStore = ProfileStore(preferences)
    private lateinit var repository: ProfileRepositoryImpl
    private var wipes = 0

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
            sessionFreeService = service,
            apiCaller = ApiCaller(ApiErrorMapper(json), UnconfinedTestDispatcher()),
            profileStore = profileStore,
            sessionStore = sessionStore,
            clock = Clock.fixed(Instant.parse("2026-10-22T12:00:00Z"), ZoneOffset.UTC),
            localDataOwner = LocalDataOwner(preferences) { wipes++ },
        )
    }

    @After
    fun tearDown() {
        runCatching { server.shutdown() }
    }

    private suspend fun signedInAs(userId: String) =
        sessionStore.save(StoredSession(accessToken, "old-refresh", 0L, userId))

    @Test
    fun `a password change keeps the phone signed in with the session the server answers`() = runTest {
        signedInAs("u-1")
        server.enqueue(
            MockResponse().setBody(
                """{"accessToken":"new-access","refreshToken":"new-refresh","expiresIn":900,"userId":"u-1","role":"ROLE_PRODUCER"}""",
            ),
        )

        val result = repository.changePassword("Olivo2025", "Cosecha2026")

        assertTrue(result is AppResult.Success)
        assertEquals("""{"currentPassword":"Olivo2025","newPassword":"Cosecha2026"}""", server.takeRequest().body.readUtf8())
        val session = sessionStore.session()!!
        assertEquals("new-access", session.accessToken)
        assertEquals("new-refresh", session.refreshToken)
    }

    @Test
    fun `a wrong current password is told apart and the session is kept`() = runTest {
        signedInAs("u-1")
        server.enqueue(
            MockResponse().setResponseCode(422).setHeader("Content-Type", "application/problem+json")
                .setBody("""{"status":422,"code":"BUSINESS_RULE_VIOLATION","detail":"La contraseña actual es incorrecta"}"""),
        )

        val result = repository.changePassword("equivocada1", "Cosecha2026")

        val error = (result as AppResult.Failure).error as AppError.Validation
        assertEquals(ProfileRepository.WRONG_CURRENT_PASSWORD, error.code)
        assertEquals("old-refresh", sessionStore.session()!!.refreshToken)
    }

    @Test
    fun `signing out closes the session on the server and forgets it even if the server fails`() = runTest {
        signedInAs("u-1")
        profileStore.saveProfile(UserProfile(ProfileId("p-1"), "u-1", "Teodoro Mamani", "PE", "+51952184337"))
        server.enqueue(MockResponse().setResponseCode(500))

        repository.signOut()

        assertEquals("""{"refreshToken":"old-refresh"}""", server.takeRequest().body.readUtf8())
        assertNull(sessionStore.session())
        assertNull(profileStore.getProfile())
        // Nothing of the farm stays on the phone
        assertEquals(1, wipes)
    }

    @Test
    fun `deleting the account forgets the session, the profile and every local record`() = runTest {
        signedInAs("u-1")
        profileStore.saveProfile(UserProfile(ProfileId("p-1"), "u-1", "Teodoro Mamani", "PE", "+51952184337"))
        server.enqueue(MockResponse().setResponseCode(204))

        val result = repository.deleteAccount("Olivo2025")

        assertTrue(result is AppResult.Success)
        val request = server.takeRequest()
        assertEquals("/auth/account-deletions", request.path)
        assertEquals("""{"password":"Olivo2025"}""", request.body.readUtf8())
        assertNull(sessionStore.session())
        assertNull(profileStore.getProfile())
        assertEquals(1, wipes)
    }

    @Test
    fun `a wrong password deletes nothing and keeps the phone as it was`() = runTest {
        signedInAs("u-1")
        server.enqueue(
            MockResponse().setResponseCode(422).setHeader("Content-Type", "application/problem+json")
                .setBody("""{"status":422,"code":"BUSINESS_RULE_VIOLATION","detail":"La contraseña actual es incorrecta"}"""),
        )

        val result = repository.deleteAccount("equivocada1")

        val error = (result as AppResult.Failure).error as AppError.Validation
        assertEquals(ProfileRepository.WRONG_CURRENT_PASSWORD, error.code)
        assertEquals("old-refresh", sessionStore.session()!!.refreshToken)
        assertEquals(0, wipes)
    }

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

    @Test
    fun `the membership badge needs an active access, and no subscription answers no badge`() = runTest {
        signedInAs("u-1")
        server.enqueue(MockResponse().setResponseCode(204))
        server.enqueue(MockResponse().setBody("""{"status":"ACTIVE","entitlementActive":true}"""))

        assertEquals(false, (repository.hasActiveMembership() as AppResult.Success).value)
        assertEquals(true, (repository.hasActiveMembership() as AppResult.Success).value)
    }

    private fun token(email: String): String {
        val encoder = Base64.getUrlEncoder().withoutPadding()
        val payload = encoder.encodeToString("""{"sub":"u-1","email":"$email"}""".toByteArray())
        return "${encoder.encodeToString("""{"alg":"HS384"}""".toByteArray())}.$payload.signature"
    }
}
