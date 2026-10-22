package pe.edu.upc.viora.core.designsystem.component

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VioraVoiceMotionTest {

    private val fixed = VioraVoiceState.Ready("Entra con tu correo y contraseña.")
    private val reply = VioraVoiceState.Ready("Riega antes de las 9 a. m.", fromAssistant = true)
    private val streaming = VioraVoiceState.Streaming("Riega antes")

    @Test
    fun `fixed texts follow the producer's preference`() {
        assertTrue(isVoiceAnimated(fixed, preferenceOn = true, systemAnimationsOff = false))
        assertFalse(isVoiceAnimated(fixed, preferenceOn = false, systemAnimationsOff = false))
    }

    @Test
    fun `the assistant's replies are written even with the preference off`() {
        for (state in listOf(VioraVoiceState.Thinking, streaming, reply)) {
            assertTrue(isVoiceAnimated(state, preferenceOn = false, systemAnimationsOff = false))
        }
    }

    @Test
    fun `with the system animations off nothing moves, replies included`() {
        for (state in listOf(fixed, VioraVoiceState.Thinking, streaming, reply)) {
            assertFalse(isVoiceAnimated(state, preferenceOn = true, systemAnimationsOff = true))
        }
    }
}
