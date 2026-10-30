package pe.edu.upc.viora.features.iam.infrastructure

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.edu.upc.viora.core.database.LocalDataOwner
import pe.edu.upc.viora.core.datastore.SessionStore
import pe.edu.upc.viora.core.datastore.StoredSession
import pe.edu.upc.viora.core.datastore.TokenCipher
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.core.network.ApiErrorMapper
import pe.edu.upc.viora.features.iam.InMemoryPreferences
import pe.edu.upc.viora.features.iam.domain.model.AuthError
import pe.edu.upc.viora.features.iam.domain.model.AuthResult
import pe.edu.upc.viora.features.iam.domain.model.Registration
import pe.edu.upc.viora.features.iam.domain.repository.AuthRepository
import pe.edu.upc.viora.features.iam.infrastructure.remote.AccountService
import pe.edu.upc.viora.features.iam.infrastructure.remote.IamService
import pe.edu.upc.viora.features.iam.infrastructure.repository.AuthRepositoryImpl
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** The account against a fake server: the typed errors of the public flows and what the phone keeps or forgets. */
class AuthRepositoryImplTest {

    private val server = MockWebServer()
    private val json = Json { ignoreUnknownKeys = true }
    private val preferences = InMemoryPreferences()
    private val sessionStore = SessionStore(preferences, TokenCipher.PLAIN)
    private lateinit var repository: AuthRepositoryImpl
    private var wipes = 0

    private val session = """{"accessToken":"new-access","refreshToken":"new-refresh","expiresIn":900,"userId":"u-1","role":"ROLE_PRODUCER"}"""

    @Before
    fun setUp() {
        server.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        repository = AuthRepositoryImpl(
            iamService = retrofit.create(IamService::class.java),
            accountService = retrofit.create(AccountService::class.java),
            apiCaller = ApiCaller(ApiErrorMapper(json), UnconfinedTestDispatcher()),
            sessionStore = sessionStore,
            localDataOwner = LocalDataOwner(preferences) { wipes++ },
            clock = Clock.fixed(Instant.parse("2026-10-22T12:00:00Z"), ZoneOffset.UTC),
        )
    }

    @After
    fun tearDown() {
        runCatching { server.shutdown() }
    }

    private suspend fun signedInAs(userId: String) =
        sessionStore.save(StoredSession("old-access", "old-refresh", 0L, userId))

    private fun problem(status: Int, body: String) =
        MockResponse().setResponseCode(status).setHeader("Content-Type", "application/problem+json").setBody(body)

    // ---- Public flows -----------------------------------------------------------------------

    @Test
    fun `signing in keeps the session and claims the phone for the account`() = runTest {
        server.enqueue(MockResponse().setBody(session))

        val result = repository.signIn("teodoro@viora.ag", "Olivo2025")

        assertTrue(result is AuthResult.Success)
        assertEquals("new-access", sessionStore.session()!!.accessToken)
        assertEquals("u-1", sessionStore.session()!!.userId)
        assertEquals(1, wipes)
    }

    @Test
    fun `an unverified email is told apart from wrong credentials`() = runTest {
        server.enqueue(problem(403, """{"status":403,"code":"EMAIL_VERIFICATION_FORBIDDEN"}"""))
        server.enqueue(problem(403, """{"status":403,"code":"BAD_CREDENTIALS"}"""))

        assertEquals(AuthResult.Failure(AuthError.EmailNotVerified), repository.signIn("a@viora.ag", "x"))
        assertEquals(AuthResult.Failure(AuthError.InvalidCredentials), repository.signIn("a@viora.ag", "x"))
        assertNull(sessionStore.session())
    }

    @Test
    fun `a wrong code says how many attempts are left`() = runTest {
        server.enqueue(problem(422, """{"status":422,"reason":"user.email_verification.invalid_code","remainingAttempts":2}"""))
        server.enqueue(problem(422, """{"status":422,"reason":"user.email_verification.code_expired"}"""))

        assertEquals(AuthResult.Failure(AuthError.InvalidCode(2)), repository.verifyEmail("a@viora.ag", "000000"))
        assertEquals(AuthResult.Failure(AuthError.CodeExpired), repository.verifyEmail("a@viora.ag", "000000"))
    }

    @Test
    fun `signing up sends the onboarding profile and answers the email the code went to`() = runTest {
        server.enqueue(MockResponse().setBody("""{"userId":"u-1","email":"teodoro@viora.ag","status":"PENDING"}"""))

        val result = repository.signUp(Registration(" teodoro@viora.ag ", "Olivo2025", " Teodoro Mamani ", "PE", "952184337", termsAccepted = true))

        assertEquals(AuthResult.Success("teodoro@viora.ag"), result)
        val body = server.takeRequest().body.readUtf8()
        assertTrue(body, body.contains(""""email":"teodoro@viora.ag""""))
        assertTrue(body, body.contains(""""termsAccepted":true"""))
        assertTrue(body, body.contains(""""fullName":"Teodoro Mamani""""))
        assertTrue(body, body.contains(""""phoneNumber":"+51952184337""""))
    }

    @Test
    fun `a code asked too soon says how long to wait, and the phone is rejected by name`() = runTest {
        server.enqueue(problem(429, """{"status":429}""").setHeader("Retry-After", "30"))
        server.enqueue(problem(400, """{"status":400,"violations":["profile.phoneNumber"]}"""))

        assertEquals(AuthResult.Failure(AuthError.CodeNotYetResendable(30)), repository.signUp(Registration("a@viora.ag", "Olivo2025", "Ana", "PE", "952184337")))
        assertEquals(AuthResult.Failure(AuthError.PhoneRejected), repository.signUp(Registration("a@viora.ag", "Olivo2025", "Ana", "PE", "952184337")))
    }

    @Test
    fun `an expired recovery link and a reused password are told apart`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))
        server.enqueue(problem(422, """{"status":422,"reason":"user.password.reused"}"""))

        assertEquals(AuthResult.Failure(AuthError.ResetLinkExpired), repository.checkPasswordResetLink("token"))
        assertEquals(AuthResult.Failure(AuthError.PasswordReused), repository.resetPassword("token", "Cosecha2026"))
    }

    @Test
    fun `no answer from the server is offline`() = runTest {
        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))

        assertEquals(AuthResult.Failure(AuthError.Offline), repository.requestPasswordReset("a@viora.ag"))
    }

    // ---- Signed-in operations (moved from Mi cuenta) -----------------------------------------

    @Test
    fun `a password change keeps the phone signed in with the session the server answers`() = runTest {
        signedInAs("u-1")
        server.enqueue(MockResponse().setBody(session))

        val result = repository.changePassword("Olivo2025", "Cosecha2026")

        assertTrue(result is AppResult.Success)
        assertEquals("""{"currentPassword":"Olivo2025","newPassword":"Cosecha2026"}""", server.takeRequest().body.readUtf8())
        assertEquals("new-access", sessionStore.session()!!.accessToken)
        assertEquals("new-refresh", sessionStore.session()!!.refreshToken)
    }

    @Test
    fun `a wrong current password is told apart and the session is kept`() = runTest {
        signedInAs("u-1")
        server.enqueue(problem(422, """{"status":422,"code":"BUSINESS_RULE_VIOLATION","detail":"La contraseña actual es incorrecta"}"""))

        val result = repository.changePassword("equivocada1", "Cosecha2026")

        val error = (result as AppResult.Failure).error as AppError.Validation
        assertEquals(AuthRepository.WRONG_CURRENT_PASSWORD, error.code)
        assertEquals("old-refresh", sessionStore.session()!!.refreshToken)
    }

    @Test
    fun `signing out closes the session on the server and forgets it even if the server fails`() = runTest {
        signedInAs("u-1")
        server.enqueue(MockResponse().setResponseCode(500))

        repository.signOut()

        val request = server.takeRequest()
        assertEquals("/auth/sign-out", request.path)
        assertEquals("""{"refreshToken":"old-refresh"}""", request.body.readUtf8())
        assertNull(sessionStore.session())
        // Nothing of the farm stays on the phone
        assertEquals(1, wipes)
    }

    @Test
    fun `deleting the account forgets the session and every local record`() = runTest {
        signedInAs("u-1")
        server.enqueue(MockResponse().setResponseCode(204))

        val result = repository.deleteAccount("Olivo2025")

        assertTrue(result is AppResult.Success)
        val request = server.takeRequest()
        assertEquals("/auth/account-deletions", request.path)
        assertEquals("""{"password":"Olivo2025"}""", request.body.readUtf8())
        assertNull(sessionStore.session())
        assertEquals(1, wipes)
    }

    @Test
    fun `a wrong password deletes nothing and keeps the phone as it was`() = runTest {
        signedInAs("u-1")
        server.enqueue(problem(422, """{"status":422,"code":"BUSINESS_RULE_VIOLATION","detail":"La contraseña actual es incorrecta"}"""))

        val result = repository.deleteAccount("equivocada1")

        val error = (result as AppResult.Failure).error as AppError.Validation
        assertEquals(AuthRepository.WRONG_CURRENT_PASSWORD, error.code)
        assertEquals("old-refresh", sessionStore.session()!!.refreshToken)
        assertEquals(0, wipes)
    }
}
