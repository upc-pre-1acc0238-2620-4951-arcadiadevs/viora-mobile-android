package pe.edu.upc.viora.core.network

import java.util.Locale
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Sends the UI language so the backend localises validation and RFC 7807 messages.
 * The backend supports `es` and `en`; anything else falls back to Spanish (field language).
 */
class AcceptLanguageInterceptor(
    private val localeProvider: () -> Locale = { Locale.getDefault() },
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val language = localeProvider().language.takeIf { it in SUPPORTED } ?: DEFAULT
        val request = chain.request().newBuilder()
            .header("Accept-Language", language)
            .build()
        return chain.proceed(request)
    }

    private companion object {
        val SUPPORTED = setOf("es", "en")
        const val DEFAULT = "es"
    }
}
