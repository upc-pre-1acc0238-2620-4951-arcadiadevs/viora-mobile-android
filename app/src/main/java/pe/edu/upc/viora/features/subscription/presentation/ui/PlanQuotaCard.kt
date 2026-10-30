package pe.edu.upc.viora.features.subscription.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.Terracotta600
import pe.edu.upc.viora.core.presentation.formatHectares
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.MyPlanViewModel

/**
 * "Tus lotes" (Figma P20 · Cupo casi lleno / Sobre el cupo): the hectares in use against the plan,
 * shown only while the quota is tight (less than 1 ha free) or already exceeded; otherwise it draws
 * nothing and P99 keeps the detail. "Ampliar mi plan" opens P102.
 */
@Composable
fun PlanQuotaCard(onExpand: () -> Unit, modifier: Modifier = Modifier, viewModel: MyPlanViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // Back from an extension the quota has grown: read it again.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.refresh() }
    if (state.isLoading || state.error != null || !state.isQuotaTight) return
    val over = state.isOverQuota
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Neutral0)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "${formatHectares(state.usedHectares)} ha",
                style = MaterialTheme.typography.headlineMedium.copy(fontFamily = NewsreaderFamily, fontSize = 32.sp, lineHeight = 34.sp),
                color = Neutral900,
            )
            Text(
                stringResource(R.string.plots_quota_of, formatHectares(state.quotaHectares)),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        BoxWithConstraints(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(Neutral100)) {
            val used = (state.usedHectares / state.quotaHectares.coerceAtLeast(0.01)).toFloat().coerceIn(0f, 1f)
            Box(Modifier.width(maxWidth * used).fillMaxHeight().clip(RoundedCornerShape(5.dp))
                .background(if (over) Terracotta600 else Green900))
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (over) stringResource(R.string.plots_quota_over, formatHectares(state.excessHectares))
                else stringResource(R.string.plots_quota_left, formatHectares(state.freeHectares)),
                style = MaterialTheme.typography.bodySmall,
                color = if (over) Terracotta600 else Neutral700,
                modifier = Modifier.weight(1f),
            )
            Text(
                stringResource(R.string.plots_quota_expand),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = Neutral900,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(role = Role.Button, onClick = onExpand)
                    .padding(horizontal = 6.dp, vertical = 8.dp),
            )
        }
    }
}
