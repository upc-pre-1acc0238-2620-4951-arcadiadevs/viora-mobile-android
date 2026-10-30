package pe.edu.upc.viora.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral500
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900

// The buttons of the app, in one place so every screen draws them alike (Figma "Botón").

/**
 * Full-width 60 dp dark pill, optionally with a yellow circle holding an icon at its end. While
 * [isLoading] the spinner takes the icon's place, or the text's when there is no icon.
 */
@Composable
fun PrimaryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes trailingIcon: Int? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    val background = if (enabled) Green900 else Neutral200
    val content = if (enabled) Neutral50 else Neutral600
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = enabled && !isLoading, role = Role.Button, onClick = onClick)
            .padding(start = 24.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (trailingIcon == null) Arrangement.Center else Arrangement.SpaceBetween,
    ) {
        if (trailingIcon == null && isLoading) {
            ButtonSpinner(color = content)
            return@Row
        }
        Text(text = text, style = MaterialTheme.typography.titleMedium, color = content)
        if (trailingIcon != null) {
            Box(
                // Disabled, the icon keeps its circle in a lighter tone (Figma P71 "Kilos no válidos").
                modifier = Modifier.size(44.dp).clip(CircleShape).background(if (enabled) Harvest300 else Neutral100),
                contentAlignment = Alignment.Center,
            ) {
                if (isLoading) {
                    ButtonSpinner(color = Neutral900)
                } else {
                    Icon(painter = painterResource(trailingIcon), contentDescription = null, tint = Neutral900)
                }
            }
        }
    }
}

/** Full-width 56 dp dark pill of the sign-in and onboarding flows; disabled it is the flat sand pill. */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(CircleShape)
            .background(if (enabled) Green900 else Neutral200)
            .clickable(enabled = enabled && !loading, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            ButtonSpinner(color = Neutral50)
        } else {
            Text(text, style = MaterialTheme.typography.titleMedium, color = if (enabled) Neutral50 else Neutral500)
        }
    }
}

/** Figma "Botón" (Secundario): outlined 56 dp pill for an equivalent alternative. */
@Composable
fun OutlinedPillButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(CircleShape)
            .border(1.dp, Neutral300, CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = Neutral900)
    }
}

/** Figma "Botón" (Texto): a 56 dp tall text-only pill for skip and secondary links. */
@Composable
fun TextPillButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, color = Neutral900)
    }
}

/**
 * A circle holding one icon: back, close and more-options buttons. It is never smaller than the
 * 48 dp touch target, and [contentDescription] is required because the icon is the only label.
 */
@Composable
fun CircleIconButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = MIN_TOUCH_TARGET,
    background: Color = Color.White,
) {
    Box(
        modifier = modifier
            .size(size.coerceAtLeast(MIN_TOUCH_TARGET).dp)
            .clip(CircleShape)
            .background(background)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painter = painterResource(icon), contentDescription = contentDescription, tint = Neutral900)
    }
}

@Composable
private fun ButtonSpinner(color: Color) {
    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp, color = color)
}

private const val MIN_TOUCH_TARGET = 48
