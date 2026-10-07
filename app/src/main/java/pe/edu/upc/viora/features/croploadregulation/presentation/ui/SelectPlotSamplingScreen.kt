package pe.edu.upc.viora.features.croploadregulation.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.features.croploadregulation.domain.entity.PlotSamplingOverview
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.SamplingStatus
import pe.edu.upc.viora.features.croploadregulation.presentation.state.SelectPlotSamplingUiState
import pe.edu.upc.viora.features.croploadregulation.presentation.ui.component.VoiceOfViora
import pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel.SelectPlotSamplingViewModel

private val ScreenPadding = 24.dp

/**
 * Uses [plotSilhouetteSlot] to decouple visual representation from plotmanagement domain models.
 */
@Composable
fun SelectPlotSamplingScreen(
    onNavigateBack: () -> Unit,
    onPlotSelected: (plotId: String, plotName: String, isCompleted: Boolean) -> Unit,
    plotSilhouetteSlot: @Composable (plotId: String, modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SelectPlotSamplingViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value

    SelectPlotSamplingContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onSelectPlot = viewModel::selectPlot,
        onConfirmPlot = { plot ->
            onPlotSelected(
                plot.plotId,
                plot.plotName,
                plot.samplingStatus == SamplingStatus.COMPLETED,
            )
        },
        plotSilhouetteSlot = plotSilhouetteSlot,
        modifier = modifier,
    )
}

@Composable
fun SelectPlotSamplingContent(
    state: SelectPlotSamplingUiState,
    onNavigateBack: () -> Unit,
    onSelectPlot: (plotId: String) -> Unit,
    onConfirmPlot: (PlotSamplingOverview) -> Unit,
    plotSilhouetteSlot: @Composable (plotId: String, modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral100),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = topInset + 8.dp,
                    bottom = 100.dp,
                    start = ScreenPadding,
                    end = ScreenPadding,
                ),
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Neutral0),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.logbook_period_earlier),
                        tint = Neutral900,
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.sampling_select_plot_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                        ),
                        color = Neutral900,
                    )
                    val campaignYear = (state as? SelectPlotSamplingUiState.Content)?.campaignYear ?: java.time.Year.now().value
                    Text(
                        text = stringResource(R.string.sampling_select_plot_subtitle, campaignYear),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                        ),
                        color = Neutral600,
                    )
                }

                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Neutral0),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.nav_action_close),
                        tint = Neutral900,
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Headline: "¿Dónde vas a *muestrear?*"
            Text(
                text = stringResource(R.string.sampling_select_plot_headline_lead),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 40.sp,
                    lineHeight = 44.sp,
                    letterSpacing = (-1).sp,
                ),
                color = Neutral900,
            )
            Text(
                text = stringResource(R.string.sampling_select_plot_headline_emphasis),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 40.sp,
                    lineHeight = 44.sp,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = (-1).sp,
                ),
                color = Neutral900,
            )

            Spacer(Modifier.height(16.dp))

            // Editorial / Voice of Viora
            VoiceOfViora(
                text = stringResource(R.string.sampling_select_plot_voice),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))

            when (state) {
                is SelectPlotSamplingUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Green900)
                    }
                }

                is SelectPlotSamplingUiState.Content -> {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        state.plots.forEach { plot ->
                            val isSelected = plot.plotId == state.selectedPlotId
                            PlotSamplingCard(
                                plot = plot,
                                isSelected = isSelected,
                                onClick = { onSelectPlot(plot.plotId) },
                                plotSilhouetteSlot = plotSilhouetteSlot,
                            )
                        }
                    }
                }
            }
        }

        // Bottom CTA Button
        if (state is SelectPlotSamplingUiState.Content && state.selectedPlot != null) {
            val selected = state.selectedPlot!!
            val buttonText = when (selected.samplingStatus) {
                SamplingStatus.IN_PROGRESS -> stringResource(R.string.sampling_action_continue)
                SamplingStatus.COMPLETED -> stringResource(R.string.sampling_action_view_summary)
                SamplingStatus.NOT_STARTED -> stringResource(R.string.sampling_action_start)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = ScreenPadding, vertical = 16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(Green900)
                        .clickable(role = Role.Button) { onConfirmPlot(selected) }
                        .padding(start = 24.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                        ),
                        color = Neutral50,
                    )
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Harvest300),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_forward),
                            contentDescription = null,
                            tint = Green900,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlotSamplingCard(
    plot: PlotSamplingOverview,
    isSelected: Boolean,
    onClick: () -> Unit,
    plotSilhouetteSlot: @Composable (plotId: String, modifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCompleted = plot.samplingStatus == SamplingStatus.COMPLETED
    val alpha = if (isCompleted && !isSelected) 0.65f else 1f

    val cardBackground = if (isSelected) Harvest100 else Neutral0
    val borderModifier = if (isSelected) {
        Modifier.border(2.dp, Green800, RoundedCornerShape(28.dp))
    } else {
        Modifier
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .then(borderModifier)
            .background(cardBackground)
            .alpha(alpha)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(start = 10.dp, end = 18.dp, top = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Thumbnail Slot API
        plotSilhouetteSlot(plot.plotId, Modifier.size(64.dp))

        // Info Column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = plot.plotName,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    letterSpacing = (-0.22).sp,
                ),
                color = Neutral900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            val areaStr = java.text.NumberFormat.getNumberInstance().apply {
                minimumFractionDigits = 1
                maximumFractionDigits = 2
            }.format(plot.areaHectares)
            Text(
                text = "${plot.variety} · $areaStr ha",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                ),
                color = Neutral600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )


            val targetTrees = (plot.sampledTreesCount + plot.treesNeeded).coerceAtLeast(plot.sampledTreesCount)
            val statusText = when (plot.samplingStatus) {
                SamplingStatus.IN_PROGRESS -> stringResource(
                    R.string.sampling_status_in_progress,
                    plot.sampledTreesCount,
                    targetTrees,
                )
                SamplingStatus.COMPLETED -> stringResource(
                    R.string.sampling_status_completed,
                    plot.sampledTreesCount,
                    targetTrees,
                )
                SamplingStatus.NOT_STARTED -> stringResource(R.string.sampling_status_not_started)
            }
            val statusColor = if (plot.samplingStatus == SamplingStatus.IN_PROGRESS) Harvest800 else Green800

            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                ),
                color = statusColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Selection Radio Indicator
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Green800),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = Neutral0,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Neutral300, CircleShape)
                    .background(Neutral0),
            )
        }
    }
}
