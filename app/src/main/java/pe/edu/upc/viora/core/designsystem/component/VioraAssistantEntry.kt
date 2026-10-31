package pe.edu.upc.viora.core.designsystem.component

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.ShadowTint
import pe.edu.upc.viora.core.designsystem.theme.newsreader

/**
 * The assistant sheet a screen opens from its entry. The app shell provides it, so a context never
 * imports the screens of another one; null where the assistant is not offered.
 */
typealias AssistantSheetSlot = @Composable (plotId: String, plotName: String, onDismiss: () -> Unit) -> Unit

/**
 * The round entry of the Viora assistant (Figma P114): a 48 dp pale yellow circle with Viora's isotype,
 * under the ⋮ of the header, with the same shadow as the header buttons.
 */
@Composable
fun AssistantEntryButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .shadow(elevation = 6.dp, shape = CircleShape, ambientColor = ShadowTint, spotColor = ShadowTint)
            .clip(CircleShape)
            .background(Harvest100)
            .clickable(role = Role.Button, onClickLabel = stringResource(R.string.assistant_entry), onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_isotype),
            contentDescription = stringResource(R.string.assistant_entry),
            tint = Green800,
            modifier = Modifier.size(26.dp),
        )
    }
}

/**
 * The card entry of the assistant where it fits the content (Figma P120, Plan del lote): "Pregúntale
 * a Viora" with two example questions and a dark round arrow.
 */
@Composable
fun AskVioraCard(examples: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Harvest100)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 22.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(R.string.assistant_entry), style = newsreader(26.sp, 32.sp), color = Neutral900)
            Text(examples, style = MaterialTheme.typography.bodyMedium, color = Neutral600, maxLines = 1)
        }
        Box(Modifier.size(44.dp).clip(CircleShape).background(Green800), contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = Neutral0, modifier = Modifier.size(24.dp))
        }
    }
}
