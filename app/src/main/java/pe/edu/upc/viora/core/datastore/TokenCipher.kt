package pe.edu.upc.viora.core.datastore

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Encrypts the session tokens before they are written to disk. */
interface TokenCipher {

    /** The stored form of [plain]. */
    fun encrypt(plain: String): String

    /**
     * The token behind a stored value. A value saved before encryption existed is returned as it is (it gets
     * encrypted on the next save); null when it cannot be decrypted, such as after the key was lost.
     */
    fun decrypt(stored: String): String?

    companion object {
        /** No encryption, only for JVM unit tests, which have no Android Keystore. */
        val PLAIN: TokenCipher = object : TokenCipher {
            override fun encrypt(plain: String) = plain
            override fun decrypt(stored: String) = stored
        }
    }
}

/**
 * [TokenCipher] with an AES-256-GCM key that lives in the Android Keystore and never leaves it, so a copy of the
 * app's files alone does not reveal a reusable refresh token.
 */
class KeystoreTokenCipher : TokenCipher {

    override fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, key()) }
        val sealed = cipher.iv + cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return PREFIX + Base64.encodeToString(sealed, Base64.NO_WRAP)
    }

    override fun decrypt(stored: String): String? {
        if (!stored.startsWith(PREFIX)) return stored
        return try {
            val sealed = Base64.decode(stored.removePrefix(PREFIX), Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, sealed, 0, IV_BYTES))
            }
            String(cipher.doFinal(sealed, IV_BYTES, sealed.size - IV_BYTES), Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    private fun key(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        (keyStore.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE).apply {
            init(
                KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build(),
            )
        }.generateKey()
    }

    private companion object {
        const val KEYSTORE = "AndroidKeyStore"
        const val ALIAS = "viora_session_tokens"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_BYTES = 12
        const val TAG_BITS = 128
        const val PREFIX = "v1:"
    }
}
