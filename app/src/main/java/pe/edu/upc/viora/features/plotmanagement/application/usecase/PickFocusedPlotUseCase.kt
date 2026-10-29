package pe.edu.upc.viora.features.plotmanagement.application.usecase

import javax.inject.Inject
import pe.edu.upc.viora.features.plotmanagement.domain.FocusedPlotRule
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot

/** Which of [plots] the Home talks about: the one chosen last ([chosenId]), else the biggest ([FocusedPlotRule]). */
class PickFocusedPlotUseCase @Inject constructor() {
    operator fun invoke(plots: List<Plot>, chosenId: String?): Plot? = FocusedPlotRule.pick(plots, chosenId)
}
