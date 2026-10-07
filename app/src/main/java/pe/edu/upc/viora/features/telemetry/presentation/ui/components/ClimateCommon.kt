package pe.edu.upc.viora.features.telemetry.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.EditorialHeadline
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderItalic
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.CircleIconButton

/** Back button, plot name with a subtitle, and the "more options" button: the top bar of P90 and P91. */
@Composable
internal fun ClimateTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    onMore: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleIconButton(
            icon = R.drawable.ic_arrow_back,
            contentDescription = stringResource(R.string.action_back),
            onClick = onBack,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title.ifBlank { " " },
                style = MaterialTheme.typography.titleMedium,
                color = Neutral900,
            )
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = Neutral600)
        }
        if (onMore != null) {
            CircleIconButton(
                icon = R.drawable.ic_more_vert,
                contentDescription = stringResource(R.string.plot_menu_more),
                onClick = onMore,
            )
        } else {
            Spacer(Modifier.size(48.dp))
        }
    }
}

/** The two-line serif headline whose second line is italic ("El clima de / *tu lote.*"). */
@Composable
internal fun ClimateHeadline(lead: String, emphasis: String, modifier: Modifier = Modifier) =
    EditorialHeadline(lead = lead, emphasis = emphasis, modifier = modifier)

/** A pill the producer taps to pick a plot, a metric or a range. */
@Composable
internal fun SelectablePill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
        color = if (selected) Neutral50 else Neutral900,
        textAlign = TextAlign.Center,
        modifier = modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(if (selected) Green900 else Neutral0)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
    )
}

/** Yellow notice for stale data: shown when the last download failed and the cache is on screen. */
@Composable
internal fun ClimateNotice(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = Harvest800,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Harvest100)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

/** Terracotta card with the error message and a retry button, for when nothing is cached. */
@Composable
internal fun ClimateErrorCard(error: AppError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Terracotta100)
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(error.messageRes()),
            style = MaterialTheme.typography.bodyMedium,
            color = Terracotta700,
        )
        TextButton(onClick = onRetry) {
            Text(text = stringResource(R.string.climate_retry), color = Terracotta700)
        }
    }
}
