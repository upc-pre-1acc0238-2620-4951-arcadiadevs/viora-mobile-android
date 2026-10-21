package pe.edu.upc.viora.core.network

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * Answers a 401 by renewing the session once and repeating the request with the new token. Gives
 * up (the 401 reaches the caller) when the request had no token, the session cannot be renewed or
 * the renewed token was refused too.
 */
class TokenAuthenticator(private val refresher: SessionRefresher) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.priorResponse != null) return null
        val rejected = response.request.header("Authorization")?.removePrefix("Bearer ")?.trim() ?: return null
        val renewed = refresher.freshAccessToken(rejected) ?: return null
        if (renewed == rejected) return null
        return response.request.newBuilder().header("Authorization", "Bearer $renewed").build()
    }
}
