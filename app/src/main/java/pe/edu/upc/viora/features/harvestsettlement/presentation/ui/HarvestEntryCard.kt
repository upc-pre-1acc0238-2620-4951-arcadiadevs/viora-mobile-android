package pe.edu.upc.viora.features.harvestsettlement.presentation.ui

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.VioraSectionHeader
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily

/**
 * Home "AHORA · COSECHA" card (Figma P70 behind the sheet): how many plots are left to register
 * in the campaign, how far along the producer is, and the "Registrar cosecha" pill.
 */
@Composable
fun HarvestEntryCard(
    pendingCount: Int,
    registeredCount: Int,
    totalCount: Int,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val onDark = Neutral50.copy(alpha = 0.78f)
    val actionLabel = stringResource(R.string.settle_entry_action)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Green900)
            .padding(start = 22.dp, end = 22.dp, top = 24.dp, bottom = 22.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(Harvest300))
            Text(
                stringResource(R.string.settle_entry_eyebrow),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.54.sp),
                color = onDark,
            )
        }
        Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
            Text(
                text = pendingCount.toString(),
                style = MaterialTheme.typography.displayLarge.copy(fontFamily = NewsreaderFamily, fontSize = 96.sp, lineHeight = 96.sp, letterSpacing = (-2.88).sp),
                color = Neutral50,
            )
            Text(
                text = pluralStringResource(R.plurals.settle_entry_unit, pendingCount),
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp, lineHeight = 36.sp),
                fontStyle = FontStyle.Italic,
                color = Neutral50,
                modifier = Modifier.padding(bottom = 14.dp),
            )
        }
        Text(stringResource(R.string.settle_entry_body), style = MaterialTheme.typography.bodyMedium, color = onDark, modifier = Modifier.padding(top = 4.dp))
        ProgressCapsule(registered = registeredCount, total = totalCount, modifier = Modifier.padding(top = 20.dp))
        Row(
            modifier = Modifier
                .padding(top = 20.dp)
                .clip(CircleShape)
                .background(Neutral50)
                .clickable(role = Role.Button, onClick = onRegister)
                .padding(start = 20.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(actionLabel, style = MaterialTheme.typography.titleSmall, color = Green900)
            Box(Modifier.size(36.dp).clip(CircleShape).background(Harvest300), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_arrow_forward), contentDescription = null, tint = Neutral900, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/** How many of the producer's plots are already registered this campaign. */
@Composable
private fun ProgressCapsule(registered: Int, total: Int, modifier: Modifier = Modifier) {
    val fraction = if (total > 0) (registered.toFloat() / total).coerceIn(0f, 1f) else 0f
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(Neutral50.copy(alpha = 0.14f))) {
            if (fraction > 0f) Box(Modifier.fillMaxWidth(fraction).height(12.dp).clip(RoundedCornerShape(6.dp)).background(Harvest300))
        }
        Text(
            stringResource(R.string.settle_entry_progress, registered, total),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
            color = Neutral50.copy(alpha = 0.6f),
        )
    }
}
