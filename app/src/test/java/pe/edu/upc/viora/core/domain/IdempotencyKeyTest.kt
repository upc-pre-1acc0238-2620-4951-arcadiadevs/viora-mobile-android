package pe.edu.upc.viora.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IdempotencyKeyTest {

    private val stored = "123e4567-e89b-42d3-a456-426614174000"

    @Test
    fun aKeyIsTheUuidItWasBuiltFrom() {
        val uuid = Uuid.parse(stored)
        assertEquals(stored, IdempotencyKey.of(uuid).value)
        assertEquals(stored, IdempotencyKey.of(uuid).toString())
    }

    @Test
    fun aKeySavedInAnEarlierSessionIsRecoveredOnlyIfItIsAUuid() {
        assertEquals(stored, IdempotencyKey.parseOrNull(stored)?.value)
        assertNull(IdempotencyKey.parseOrNull("not-a-key"))
        assertNull(IdempotencyKey.parseOrNull(null))
    }

    @Test
    fun theGeneratorHandsOutAFreshKeyEachTime() {
        val generator = RandomIdGenerator()
        assertNotEquals(generator.newIdempotencyKey(), generator.newIdempotencyKey())
    }

    @Test
    fun aTestGeneratorCanPinTheIds() {
        val fixed = Uuid.parse(stored)
        val generator = object : IdGenerator {
            override fun newUuid(): Uuid = fixed
        }
        assertEquals(fixed, generator.newUuid())
        assertEquals(stored, generator.newIdempotencyKey().value)
    }
}
