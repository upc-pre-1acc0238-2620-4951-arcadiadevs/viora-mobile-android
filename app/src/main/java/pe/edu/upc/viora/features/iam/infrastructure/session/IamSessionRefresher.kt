package pe.edu.upc.viora.features.iam.infrastructure.session

import kotlin.coroutines.cancellation.CancellationException
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
 * The refresh token works only once and presenting a used one can close every session of the account,
 * so renewals are serialized: a request that waited while another one renewed simply takes the new
 * token. The token is renewed a minute before it expires, not only after a 401, to avoid a failed
 * request. After a passing failure the requests in the queue do not try again for a few seconds, so
 * without signal each one does not wait for its own timeout nor send the same token over and over.
 */
@Singleton
class IamSessionRefresher @Inject constructor(
    private val sessionStore: SessionStore,
    private val service: IamService,
    private val events: SessionEvents,
    private val clock: Clock,
) : SessionRefresher {

    private val lock = Any()

    /** Epoch millis before which no renewal is tried, because the last one just failed. Guarded by [lock]. */
    private var noRenewalBeforeMillis = 0L

    override fun freshAccessToken(rejected: String?): String? = synchronized(lock) {
        runBlocking {
            val session = sessionStore.session() ?: return@runBlocking null
            val wasRejected = rejected != null && rejected == session.accessToken
            val expiresSoon = session.expiresAtMillis - clock.millis() < RENEW_BEFORE_MILLIS
            val mayRenew = clock.millis() >= noRenewalBeforeMillis
            if ((wasRejected || expiresSoon) && mayRenew) renew(session) else session.accessToken
        }
    }

    private suspend fun renew(session: StoredSession): String? = try {
        val renewed = service.refresh(RefreshTokenRequest(session.refreshToken)).toStoredSession(clock)
        sessionStore.save(renewed)
        noRenewalBeforeMillis = 0L
        renewed.accessToken
    } catch (e: HttpException) {
        if (e.code() in REJECTED_CODES) {
            // Expired, reused or revoked: the producer has to sign in again.
            sessionStore.clear()
            events.notifyExpired()
            null
        } else {
            afterPassingFailure(session)
        }
    } catch (e: IOException) {
        // Offline or the server is waking up: keep the current token and try again in a moment.
        afterPassingFailure(session)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        // An unexpected answer (a body that does not parse, a proxy page): this runs inside OkHttp, where an uncaught
        // exception would crash the app, so it is treated like a passing failure and retried in a moment.
        afterPassingFailure(session)
    }

    private fun afterPassingFailure(session: StoredSession): String {
        noRenewalBeforeMillis = clock.millis() + RETRY_AFTER_FAILURE_MILLIS
        return session.accessToken
    }

    private companion object {
        const val RENEW_BEFORE_MILLIS = 60_000L
        const val RETRY_AFTER_FAILURE_MILLIS = 15_000L

        /** Only the server's own refusal ends a session: a 400 or 404 may come from a proxy or a deploy in between. */
        val REJECTED_CODES = setOf(401, 403)
    }
}

/** The session to store from a sign-in, verification or refresh answer. */
fun AuthSession.toStoredSession(clock: Clock): StoredSession = StoredSession(
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresAtMillis = clock.millis() + expiresIn * 1_000L,
    userId = userId,
)
