package pe.edu.upc.viora.core.network

/**
 * Keeps the access token usable. IAM implements it (it owns the refresh endpoint); the network
 * layer only knows this contract. Blocking: OkHttp interceptors already run off the main thread.
 */
interface SessionRefresher {

    /**
     * The access token to send, renewed first when it expires within a minute or when it is
     * [rejected] (the token the server just answered 401 to). Null when there is no session or it
     * could not be renewed.
     */
    fun freshAccessToken(rejected: String? = null): String?
}
