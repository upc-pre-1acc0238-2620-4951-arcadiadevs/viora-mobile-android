package pe.edu.upc.viora.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700

// The rows of the option sheets ("Más opciones" of a plot and its sections).

@Composable
fun OptionRow(
    @DrawableRes icon: Int,
    title: String,
    hint: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(if (isDestructive) Terracotta100 else Green200),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = if (isDestructive) Terracotta700 else Neutral900,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (isDestructive) Terracotta700 else Neutral900,
            )
            Text(text = hint, style = MaterialTheme.typography.bodySmall, color = Neutral600)
        }
        if (!isDestructive && enabled) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = Neutral600,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun OptionDivider() {
    HorizontalDivider(color = Neutral200)
}
