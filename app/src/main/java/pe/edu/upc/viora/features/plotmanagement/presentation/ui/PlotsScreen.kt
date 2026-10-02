package pe.edu.upc.viora.features.plotmanagement.presentation.ui

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.VioraTabBarDefaults
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Spacing
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.designsystem.theme.VioraTheme
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.plotmanagement.domain.entity.Plot
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.GeoPoint
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.OliveVariety
import pe.edu.upc.viora.features.plotmanagement.domain.valueobject.PlotId
import pe.edu.upc.viora.features.plotmanagement.presentation.state.PlotsUiState
import pe.edu.upc.viora.features.plotmanagement.presentation.viewmodel.PlotsViewModel

@Composable
fun PlotsScreen(modifier: Modifier = Modifier, viewModel: PlotsViewModel = hiltViewModel()) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    PlotsScreenContent(state = state, onRefresh = viewModel::refresh, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlotsScreenContent(state: PlotsUiState, onRefresh: () -> Unit, modifier: Modifier = Modifier) {
    PullToRefreshBox(
        isRefreshing = (state as? PlotsUiState.Content)?.isRefreshing == true,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spacing.lg,
                end = Spacing.lg,
                top = Spacing.lg,
                bottom = VioraTabBarDefaults.ContentBottomPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { PlotsTitle() }
            when (state) {
                PlotsUiState.Loading -> item { Centered { CircularProgressIndicator(color = MaterialTheme.colorScheme.primary) } }
                PlotsUiState.Empty -> item { EmptyState() }
                is PlotsUiState.Error -> item { ErrorState(error = state.error, onRetry = onRefresh) }
                is PlotsUiState.Content -> {
                    item { FilterAndStatus(state) }
                    items(state.plots, key = { it.id.value }) { plot -> PlotCard(plot) }
                }
            }
        }
    }
}

@Composable
private fun PlotsTitle() {
    val style = MaterialTheme.typography.displayMedium.copy(fontSize = 44.sp, lineHeight = 46.sp, letterSpacing = (-1.1).sp)
    Column(modifier = Modifier.padding(bottom = Spacing.sm)) {
        Text(text = stringResource(R.string.plots_title_lead), style = style)
        Text(text = stringResource(R.string.plots_title_emphasis), style = style, fontStyle = FontStyle.Italic)
    }
}

@Composable
private fun FilterAndStatus(state: PlotsUiState.Content) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs), modifier = Modifier.padding(bottom = Spacing.xs)) {
        ActiveFilterChip(count = state.plots.size)
        state.refreshError?.let { OfflineBanner(it) }
        state.lastRefresh?.let { LastRefreshLabel(it) }
    }
}

@Composable
private fun ActiveFilterChip(count: Int) {
    Text(
        text = stringResource(R.string.plots_filter_active, count),
        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, lineHeight = 18.sp),
        color = Neutral50,
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(Green900)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

@Composable
private fun OfflineBanner(error: AppError) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Terracotta100)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
    ) {
        Text(text = stringResource(error.messageRes()), style = MaterialTheme.typography.bodyMedium, color = Terracotta700)
        Text(text = stringResource(R.string.plots_showing_saved_data), style = MaterialTheme.typography.bodySmall, color = Terracotta700)
    }
}

@Composable
private fun LastRefreshLabel(lastRefresh: Instant) {
    val now = System.currentTimeMillis()
    val text = if (now - lastRefresh.toEpochMilli() < DateUtils.MINUTE_IN_MILLIS) {
        stringResource(R.string.plots_updated_just_now)
    } else {
        val relative = DateUtils.getRelativeTimeSpanString(lastRefresh.toEpochMilli(), now, DateUtils.MINUTE_IN_MILLIS)
        stringResource(R.string.plots_updated, relative)
    }
    Text(text = text, style = MaterialTheme.typography.bodySmall, color = Neutral600)
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Text(text = stringResource(R.string.plots_empty_title), style = MaterialTheme.typography.headlineMedium)
        Text(
            text = stringResource(R.string.plots_empty_body),
            style = MaterialTheme.typography.bodyLarge,
            color = Neutral600,
        )
    }
}

@Composable
private fun ErrorState(error: AppError, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text(text = stringResource(error.messageRes()), style = MaterialTheme.typography.bodyLarge)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(Green900)
                .clickable(role = Role.Button, onClick = onRetry)
                .heightIn(min = Spacing.minTouchTarget)
                .padding(horizontal = Spacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.plots_retry),
                style = MaterialTheme.typography.labelLarge,
                color = Neutral50,
            )
        }
    }
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xxl), contentAlignment = Alignment.Center) {
        content()
    }
}

private fun samplePlot(id: String, name: String, variety: OliveVariety, area: Double, density: Int) = Plot(
    id = PlotId(id),
    name = name,
    variety = variety,
    areaHectares = area,
    treesPerHectare = density,
    rowSpacingMeters = 7.0,
    treeSpacingMeters = 5.0,
    outline = listOf(
        GeoPoint(-18.0500, -70.2500),
        GeoPoint(-18.0490, -70.2400),
        GeoPoint(-18.0600, -70.2410),
        GeoPoint(-18.0610, -70.2490),
    ),
    lastPruningDate = null,
    isActive = true,
    revision = 0,
)

private val previewPlots = listOf(
    samplePlot("1", "La Yarada 02", OliveVariety.SEVILLANA, 2.5, 72),
    samplePlot("2", "Lote Norte", OliveVariety.CRIOLLA, 1.5, 73),
)

@Preview(showBackground = true, backgroundColor = 0xFFF9F6F1, heightDp = 640)
@Composable
private fun PlotsContentPreview() {
    VioraTheme {
        PlotsScreenContent(
            state = PlotsUiState.Content(previewPlots, isRefreshing = false, refreshError = null, lastRefresh = Instant.now()),
            onRefresh = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF9F6F1, heightDp = 640)
@Composable
private fun PlotsOfflinePreview() {
    VioraTheme {
        PlotsScreenContent(
            state = PlotsUiState.Content(previewPlots, isRefreshing = false, refreshError = AppError.Offline, lastRefresh = Instant.now()),
            onRefresh = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF9F6F1, heightDp = 480)
@Composable
private fun PlotsEmptyPreview() {
    VioraTheme { PlotsScreenContent(state = PlotsUiState.Empty, onRefresh = {}) }
}

@Preview(showBackground = true, backgroundColor = 0xFFF9F6F1, heightDp = 480)
@Composable
private fun PlotsErrorPreview() {
    VioraTheme { PlotsScreenContent(state = PlotsUiState.Error(AppError.Offline), onRefresh = {}) }
}
