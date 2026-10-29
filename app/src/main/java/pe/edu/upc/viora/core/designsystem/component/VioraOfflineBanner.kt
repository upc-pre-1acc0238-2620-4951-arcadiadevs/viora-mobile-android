package pe.edu.upc.viora.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700

/**
 * Figma "Aviso · sin conexión": a terracotta pill with the cloud icon in a white circle and [text].
 * The defaults are the Home notice; a denser screen passes a smaller [iconCircle] and [contentPadding].
 */
@Composable
fun VioraOfflineBanner(
    text: String,
    modifier: Modifier = Modifier,
    iconCircle: Dp = 40.dp,
    spacing: Dp = 12.dp,
    contentPadding: PaddingValues = PaddingValues(start = 10.dp, end = 18.dp, top = 10.dp, bottom = 10.dp),
    textStyle: TextStyle = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.sp),
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(Terracotta100)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(iconCircle).clip(CircleShape).background(Neutral0), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.ic_cloud_sync),
                contentDescription = null,
                tint = Terracotta700,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(text = text, style = textStyle, color = Terracotta700, modifier = Modifier.weight(1f))
    }
}
