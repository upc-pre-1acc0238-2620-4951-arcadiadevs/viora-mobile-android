package pe.edu.upc.viora.features.home.presentation.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.viora.core.designsystem.component.AgronomicTerm
import pe.edu.upc.viora.features.home.presentation.state.HomePhase

class HomePhaseTermTest {
    @Test fun everyHomePhaseHasItsOwnExplanation() {
        assertEquals(AgronomicTerm.DORMANCY, HomePhase.DORMANCY.helpTerm())
        assertEquals(AgronomicTerm.BLOOM, HomePhase.BLOOM.helpTerm())
        assertEquals(AgronomicTerm.FRUIT_SET, HomePhase.FRUIT_SET.helpTerm())
        assertEquals(AgronomicTerm.THINNING, HomePhase.THINNING.helpTerm())
        assertEquals(AgronomicTerm.HARVEST, HomePhase.HARVEST.helpTerm())
    }
}
