package pe.edu.upc.viora.core.domain

/**
 * The `Idempotency-Key` header of a request that must not run twice (a payment, a settlement). It is a
 * UUID, and a retry of the same attempt must send the same key, so it is generated once per attempt and
 * kept until the attempt ends.
 */
@JvmInline
value class IdempotencyKey private constructor(val value: String) {

    override fun toString(): String = value

    companion object {
        fun of(uuid: Uuid): IdempotencyKey = IdempotencyKey(uuid.value)

        /** The key for a text saved earlier (a draft, a pending row), or null when it is not a UUID. */
        fun parseOrNull(text: String?): IdempotencyKey? = Uuid.parseOrNull(text)?.let(::of)
    }
}
