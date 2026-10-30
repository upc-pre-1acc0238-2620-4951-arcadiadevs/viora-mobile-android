package pe.edu.upc.viora.core.presentation

import androidx.lifecycle.SavedStateHandle
import pe.edu.upc.viora.core.domain.PlotId

/**
 * The plot a screen was opened for. Every route reads its `plotId` through [PlotId], so a plot identifier is checked
 * the same way wherever it enters the app.
 */
fun SavedStateHandle.requirePlotId(): PlotId =
    PlotId(checkNotNull(get<String>(PLOT_ID)) { "This route needs a plotId" })

const val PLOT_ID = "plotId"
