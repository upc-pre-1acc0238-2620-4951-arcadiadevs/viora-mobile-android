package pe.edu.upc.viora.features.subscription.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upc.viora.features.subscription.domain.valueobject.InvitationCode

class InvitationCodeTest {

    @Test
    fun `a code is COOP- and four letters or digits, typed in any case`() {
        assertEquals("COOP-7K2Q", InvitationCode.of(" coop-7k2q ")?.value)
        assertTrue(InvitationCode.isWellFormed("COOP-9HX3"))
    }

    @Test
    fun `anything else is not a code`() {
        assertNull(InvitationCode.of("COOP-"))
        assertNull(InvitationCode.of("COOP-7K2"))
        assertNull(InvitationCode.of("COOP-7K2QX"))
        assertNull(InvitationCode.of("COOP-7K-Q"))
        assertFalse(InvitationCode.isWellFormed("XXXX-7K2Q"))
    }
}
