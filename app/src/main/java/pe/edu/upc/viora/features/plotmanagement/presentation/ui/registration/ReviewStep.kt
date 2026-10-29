package pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.PrimaryPillButton
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.presentation.formatHectares
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.plotmanagement.presentation.state.RegisterPlotStep
import pe.edu.upc.viora.features.plotmanagement.presentation.state.RegisterPlotUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.state.SaveFailure
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.formatCount
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.formatMeters
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.labelRes

/** Step 3: a summary of everything the producer entered, and the button that saves it. */
@Composable
fun ReviewStep(
    state: RegisterPlotUiState,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    onAdjustOutline: () -> Unit = {},
    onExpandPlan: () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        StepHeader(step = RegisterPlotStep.REVIEW.number, onBack = onBack, onClose = onClose, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val titleStyle = MaterialTheme.typography.displaySmall
            Column {
                Text(text = stringResource(R.string.review_title_lead), style = titleStyle)
                Text(text = stringResource(R.string.review_title_emphasis), style = titleStyle, fontStyle = FontStyle.Italic)
            }
            OutlinePreview(
                corners = state.corners,
                caption = stringResource(R.string.trace_area_value, formatHectares(state.areaHectares)),
                captionAlignment = Alignment.TopStart,
                height = 200.dp,
            )
            SummaryTable(state)
            state.saveFailure?.let { failure -> SaveFailureMessage(failure) }
        }
        if (state.exceedsPlan) {
            OverQuotaCard(
                state = state,
                onAdjustOutline = onAdjustOutline,
                onExpandPlan = onExpandPlan,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
            )
        } else {
            state.planQuotaHectares?.let { quota ->
                Text(
                    text = stringResource(
                        R.string.review_plan_usage,
                        formatHectares(state.usedHectares + state.areaHectares),
                        formatHectares(quota),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral600,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
                )
            }
            PrimaryPillButton(
                text = stringResource(if (state.isSaving) R.string.review_saving else R.string.review_save),
                onClick = onSave,
                trailingIcon = R.drawable.ic_check,
                isLoading = state.isSaving,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun SummaryTable(state: RegisterPlotUiState) {
    val rows = listOf(
        stringResource(R.string.review_name) to state.name.trim(),
        stringResource(R.string.review_variety) to (state.variety?.let { stringResource(it.labelRes()) } ?: "—"),
        stringResource(R.string.review_frame) to stringResource(
            R.string.review_frame_value,
            formatMeters(state.rowSpacingMeters),
            formatMeters(state.treeSpacingMeters),
        ),
        stringResource(R.string.review_density) to stringResource(R.string.review_density_value, formatCount(state.treesPerHectare)),
        stringResource(R.string.review_trees) to stringResource(R.string.review_trees_value, formatCount(state.estimatedTrees)),
        stringResource(R.string.review_outline) to stringResource(
            R.string.review_outline_value,
            pluralStringResource(R.plurals.corners_count, state.corners.size, state.corners.size),
        ),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Neutral0)
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        rows.forEachIndexed { index, (label, value) ->
            if (index > 0) HorizontalDivider(color = Neutral200)
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Neutral600)
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = Neutral900,
                )
            }
        }
    }
}

@Composable
private fun SaveFailureMessage(failure: SaveFailure) {
    val text = when (failure) {
        SaveFailure.NameTaken -> stringResource(R.string.save_error_name_taken)
        is SaveFailure.Rejected ->
            failure.detail?.let { stringResource(R.string.save_error_rejected, it) } ?: stringResource(R.string.error_validation)
        is SaveFailure.Other -> stringResource(failure.error.messageRes())
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = Terracotta700,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Terracotta100)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

/** P25 · Área mayor al cupo del plan: the plot does not fit; adjust it or expand the plan. */
@Composable
private fun OverQuotaCard(
    state: RegisterPlotUiState,
    onAdjustOutline: () -> Unit,
    onExpandPlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Terracotta100)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.review_over_quota_title, formatHectares(state.areaHectares)),
            style = MaterialTheme.typography.headlineSmall,
            color = Terracotta700,
        )
        Text(
            text = stringResource(
                R.string.review_over_quota_body,
                formatHectares(state.freeHectares ?: 0.0),
                formatHectares(state.planQuotaHectares ?: 0.0),
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral900,
        )
        Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.review_adjust_outline),
                style = MaterialTheme.typography.labelLarge,
                color = Neutral50,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Green900)
                    .clickable(role = Role.Button, onClick = onAdjustOutline)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
            )
            Text(
                text = stringResource(R.string.review_expand_plan),
                style = MaterialTheme.typography.labelLarge,
                color = Neutral900,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Neutral0)
                    .clickable(role = Role.Button, onClick = onExpandPlan)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
            )
        }
    }
}
