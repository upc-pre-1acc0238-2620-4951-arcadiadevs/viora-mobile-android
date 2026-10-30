package pe.edu.upc.viora.core.domain

/** Source of new identifiers, injectable so a test can hand out fixed ones. */
interface IdGenerator {
    fun newUuid(): Uuid

    fun newIdempotencyKey(): IdempotencyKey = IdempotencyKey.of(newUuid())
}

/** The production generator: random UUIDs. */
class RandomIdGenerator : IdGenerator {
    override fun newUuid(): Uuid = Uuid.random()
}
