package pe.edu.upc.viora.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Spacing

/** A screen or section that is still loading: the spinner centred in the space [modifier] gives it. */
@Composable
fun VioraLoadingState(modifier: Modifier = Modifier, color: Color = Green900) {
    Box(modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = color)
    }
}

/**
 * Nothing could be loaded and nothing is cached: an optional [title], the [message] (what went wrong, from
 * `AppError.messageRes()`) and the "Reintentar" pill that calls [onRetry].
 */
@Composable
fun VioraErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier, title: String? = null) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        if (title != null) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(text = title, style = MaterialTheme.typography.headlineSmall)
                Text(text = message, style = MaterialTheme.typography.bodyMedium, color = Neutral600)
            }
        } else {
            Text(text = message, style = MaterialTheme.typography.bodyLarge)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .background(Green900)
                .clickable(role = Role.Button, onClick = onRetry)
                .heightIn(min = Spacing.minTouchTarget)
                .padding(horizontal = Spacing.lg),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = stringResource(R.string.common_retry), style = MaterialTheme.typography.labelLarge, color = Neutral50)
        }
    }
}

/** A list with nothing in it yet: what is missing ([title]) and how it gets filled ([body]). */
@Composable
fun VioraEmptyState(title: String, body: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Text(text = body, style = MaterialTheme.typography.bodyLarge, color = Neutral600)
    }
}
