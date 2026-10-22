package pe.edu.upc.viora.features.profiles.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.viora.features.profiles.domain.entity.UserProfile
import pe.edu.upc.viora.features.profiles.domain.valueobject.ProfileId

class UserProfileTest {

    @Test
    fun `initials are extracted correctly from full name`() {
        val user1 = UserProfile(
            id = ProfileId("1"),
            userId = "u1",
            fullName = "Rubén Ticona",
            country = "PE",
            phoneNumber = "+51951406118",
        )
        assertEquals("RT", user1.initials)

        val user2 = UserProfile(
            id = ProfileId("2"),
            userId = "u2",
            fullName = "Maria",
            country = "PE",
            phoneNumber = "+51987654321",
        )
        assertEquals("MA", user2.initials)

        val user3 = UserProfile(
            id = ProfileId("3"),
            userId = "u3",
            fullName = "Carlos Alberto Mendoza",
            country = "PE",
            phoneNumber = "+51912345678",
        )
        assertEquals("CA", user3.initials)
    }

    @Test
    fun `formatted phone number formats international`() {
        val user = UserProfile(
            id = ProfileId("1"),
            userId = "u1",
            fullName = "Rubén Ticona",
            country = "PE",
            phoneNumber = "+51951406118",
        )
        assertEquals("+51 951 406 118", user.formattedPhoneNumber)
    }
}
