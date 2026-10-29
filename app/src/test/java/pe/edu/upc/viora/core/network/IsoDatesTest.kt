package pe.edu.upc.viora.core.network

import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IsoDatesTest {

    @Test
    fun readsUtcInstantsAndOnesWithAnOffset() {
        assertEquals(Instant.parse("2026-05-14T15:40:00Z"), "2026-05-14T15:40:00Z".toInstantOrNull())
        assertEquals(Instant.parse("2026-05-14T20:40:00Z"), "2026-05-14T15:40:00-05:00".toInstantOrNull())
    }

    @Test
    fun aMissingOrBrokenInstantIsNull() {
        assertNull((null as String?).toInstantOrNull())
        assertNull("".toInstantOrNull())
        assertNull("14/05/2026".toInstantOrNull())
    }

    @Test
    fun readsPlainDates() {
        assertEquals(LocalDate.of(2026, 5, 14), "2026-05-14".toLocalDateOrNull())
        assertNull("2026-13-01".toLocalDateOrNull())
        assertNull((null as String?).toLocalDateOrNull())
    }
}
