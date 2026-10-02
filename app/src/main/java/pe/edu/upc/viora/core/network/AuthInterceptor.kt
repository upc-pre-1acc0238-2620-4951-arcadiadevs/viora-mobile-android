package pe.edu.upc.viora.core.network

import okhttp3.Interceptor
import okhttp3.Response

/** Adds `Authorization: Bearer <token>` when a session exists. Public endpoints stay untouched. */
class AuthInterceptor(private val tokenProvider: AccessTokenProvider) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider.accessToken()
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
