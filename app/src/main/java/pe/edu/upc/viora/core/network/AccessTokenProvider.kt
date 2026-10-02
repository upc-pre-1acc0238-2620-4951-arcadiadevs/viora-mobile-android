package pe.edu.upc.viora.core.network

/** Blocking accessor used by OkHttp interceptors, which already run off the main thread. */
fun interface AccessTokenProvider {
    fun accessToken(): String?
}
