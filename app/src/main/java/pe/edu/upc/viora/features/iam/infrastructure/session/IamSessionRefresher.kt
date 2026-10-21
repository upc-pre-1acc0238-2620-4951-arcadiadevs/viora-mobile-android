package pe.edu.upc.viora.features.iam.infrastructure.session

import java.io.IOException
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import pe.edu.upc.viora.core.datastore.SessionEvents
import pe.edu.upc.viora.core.datastore.SessionStore
import pe.edu.upc.viora.core.datastore.StoredSession
import pe.edu.upc.viora.core.network.SessionRefresher
import pe.edu.upc.viora.features.iam.infrastructure.remote.AuthSession
import pe.edu.upc.viora.features.iam.infrastructure.remote.IamService
import pe.edu.upc.viora.features.iam.infrastructure.remote.RefreshTokenRequest
import retrofit2.HttpException

/**
 * Renews the session with `POST /auth/refresh-tokens` (guide 02, section 2.3).
 *
 * The refresh token works only once and presenting a used one closes every session of the account,
 * so renewals are serialized: a request that waited while another one renewed simply takes the new
 * token. The token is renewed a minute before it expires, not only after a 401, because the backend
 * treats an expired token as no session and would answer with the demo producer's data.
 */
@Singleton
class IamSessionRefresher @Inject constructor(
    private val sessionStore: SessionStore,
    private val service: IamService,
    private val events: SessionEvents,
    private val clock: Clock,
) : SessionRefresher {

    private val lock = Any()

    override fun freshAccessToken(rejected: String?): String? = synchronized(lock) {
        runBlocking {
            val session = sessionStore.session() ?: return@runBlocking null
            val wasRejected = rejected != null && rejected == session.accessToken
            val expiresSoon = session.expiresAtMillis - clock.millis() < RENEW_BEFORE_MILLIS
            if (wasRejected || expiresSoon) renew(session) else session.accessToken
        }
    }

    private suspend fun renew(session: StoredSession): String? = try {
        val renewed = service.refresh(RefreshTokenRequest(session.refreshToken)).toStoredSession(clock)
        sessionStore.save(renewed)
        renewed.accessToken
    } catch (e: HttpException) {
        if (e.code() in REJECTED_CODES) {
            // Expired, reused or revoked: the producer has to sign in again.
            sessionStore.clear()
            events.notifyExpired()
            null
        } else {
            session.accessToken
        }
    } catch (e: IOException) {
        // Offline or the server is waking up: keep the current token and try again next time.
        session.accessToken
    }

    private companion object {
        const val RENEW_BEFORE_MILLIS = 60_000L
        val REJECTED_CODES = setOf(400, 401, 403, 404)
    }
}

/** The session to store from a sign-in, verification or refresh answer. */
fun AuthSession.toStoredSession(clock: Clock): StoredSession = StoredSession(
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresAtMillis = clock.millis() + expiresIn * 1_000L,
    userId = userId,
)
