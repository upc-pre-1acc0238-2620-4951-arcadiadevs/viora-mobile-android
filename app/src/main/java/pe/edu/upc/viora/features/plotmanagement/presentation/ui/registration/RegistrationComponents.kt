package pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900

/** Full-width dark pill, optionally with a yellow circle holding an icon at its end (Figma "Botón"). */
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
        Text(text = text, style = MaterialTheme.typography.titleMedium, color = content)
        if (trailingIcon != null) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(if (enabled) Harvest300 else Color.Transparent),
                contentAlignment = Alignment.Center,
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp, color = Neutral900)
                } else {
                    Icon(painter = painterResource(trailingIcon), contentDescription = null, tint = Neutral900)
                }
            }
        }
    }
}

/** A white or tinted rounded tile with a big serif number and a small caption underneath. */
@Composable
fun StatTile(
    value: String,
    caption: String,
    background: Color,
    modifier: Modifier = Modifier,
    valueColor: Color = Neutral900,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(text = value, style = MaterialTheme.typography.displaySmall, color = valueColor)
        Text(text = caption, style = MaterialTheme.typography.bodySmall, color = Neutral600)
    }
}

/** 48 dp white circle holding one icon: the back and close buttons of the wizard header. */
@Composable
fun CircleIconButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 48,
    background: Color = Color.White,
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(painter = painterResource(icon), contentDescription = contentDescription, tint = Neutral900)
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = Neutral600,
        modifier = modifier.fillMaxWidth().padding(top = 4.dp),
    )
}
