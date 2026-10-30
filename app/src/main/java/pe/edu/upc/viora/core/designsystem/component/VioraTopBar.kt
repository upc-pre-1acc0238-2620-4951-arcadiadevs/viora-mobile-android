package pe.edu.upc.viora.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900

/**
 * The top bar of the plot and account screens: a round back (or close) button on the left, [center]
 * in the middle and, on the right, the "more options" button or an empty 48 dp slot that keeps the
 * centre centred. Screens set their own padding through [modifier].
 */
@Composable
fun VioraTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes navigationIcon: Int = R.drawable.ic_arrow_back,
    navigationDescription: String = stringResource(R.string.action_back),
    onMore: (() -> Unit)? = null,
    center: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleIconButton(icon = navigationIcon, contentDescription = navigationDescription, onClick = onBack)
        center()
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

/**
 * The centred title over a subtitle of [VioraTopBar]. By default it takes the free width with
 * [horizontalPadding] on each side and keeps each line to one; a screen that sizes it to its text
 * passes `fill = false`.
 */
@Composable
fun RowScope.VioraTopBarTitle(
    title: String,
    subtitle: String,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    subtitleStyle: TextStyle = MaterialTheme.typography.bodySmall,
    fill: Boolean = true,
    horizontalPadding: Dp = 8.dp,
    maxLines: Int = 1,
    subtitleOverflow: TextOverflow = TextOverflow.Clip,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = if (fill) Modifier.weight(1f).padding(horizontal = horizontalPadding) else Modifier,
    ) {
        Text(title, style = titleStyle, color = Neutral900, maxLines = maxLines)
        Text(subtitle, style = subtitleStyle, color = Neutral600, maxLines = maxLines, overflow = subtitleOverflow)
    }
}
