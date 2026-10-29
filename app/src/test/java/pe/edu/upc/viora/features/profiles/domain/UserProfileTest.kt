package pe.edu.upc.viora.features.profiles.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.viora.features.profiles.domain.entity.UserProfile
import pe.edu.upc.viora.features.profiles.domain.valueobject.ProfileId

class UserProfileTest {

    private fun profile(fullName: String, phone: String = "+51952184337", country: String = "PE") =
        UserProfile(ProfileId("p-1"), "u-1", fullName, country, phone)

    @Test
    fun `initials take the first letters of the first two names`() {
        assertEquals("TM", profile("Teodoro Mamani").initials)
        assertEquals("TM", profile("  teodoro   mamani quispe ").initials)
        assertEquals("TE", profile("Teodoro").initials)
        assertEquals("VI", profile("   ").initials)
    }

    @Test
    fun `the stored E164 number reads as national digits and in international format`() {
        val peru = profile("Teodoro Mamani")
        assertEquals("952184337", peru.nationalNumber)
        assertEquals("+51 952 184 337", peru.formattedPhoneNumber)

        // A Chilean number keeps its own dial code, not Peru's.
        val chile = profile("Ana Rojas", phone = "+56912345678", country = "CL")
        assertEquals("912345678", chile.nationalNumber)
        assertEquals("+56 912345678", chile.formattedPhoneNumber)
    }

    @Test
    fun `a profile id is never blank, like a plot id`() {
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            pe.edu.upc.viora.features.profiles.domain.valueobject.ProfileId(" ")
        }
    }
}
