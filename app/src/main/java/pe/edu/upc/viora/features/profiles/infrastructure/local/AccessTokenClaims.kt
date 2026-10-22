package pe.edu.upc.viora.features.profiles.infrastructure.local

import java.util.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Reads claims of the access token without verifying it (the server already did). The profile does
 * not hold the email, but the token the server signed for this account does.
 */
internal object AccessTokenClaims {

    fun email(accessToken: String): String? = runCatching {
        val payload = accessToken.split('.')[1]
        val json = String(Base64.getUrlDecoder().decode(payload), Charsets.UTF_8)
        Json.parseToJsonElement(json).jsonObject["email"]?.jsonPrimitive?.content
    }.getOrNull()?.takeIf { it.isNotBlank() }
}
