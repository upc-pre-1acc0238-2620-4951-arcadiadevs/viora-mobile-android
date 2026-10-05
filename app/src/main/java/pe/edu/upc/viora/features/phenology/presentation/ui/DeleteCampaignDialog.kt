package pe.edu.upc.viora.features.phenology.presentation.ui

import androidx.compose.foundation.background
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.phenology.presentation.state.IndexPreview

/** "¿Eliminar la campaña 2021?" (Figma): what is deleted and the index without that campaign. */
@Composable
fun DeleteCampaignDialog(
    year: Int,
    plotName: String,
    preview: IndexPreview,
    isWorking: Boolean,
    error: AppError?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = { if (!isWorking) onDismiss() }) {
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(32.dp)).background(Neutral0).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(Terracotta100), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_warning), contentDescription = null, tint = Terracotta700)
            }
            Column {
                val titleStyle = MaterialTheme.typography.headlineMedium
                Text(stringResource(R.string.harvest_delete_lead), style = titleStyle)
                Text(stringResource(R.string.harvest_delete_emphasis, year), style = titleStyle, fontStyle = FontStyle.Italic)
            }
            Text(
                text = pluralStringResource(R.plurals.harvest_delete_body, preview.campaignsAfter, plotName, preview.campaignsAfter),
                style = MaterialTheme.typography.bodyMedium,
                color = Neutral900,
            )
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.surfaceContainerLow).padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IndexChange(before = preview.before, after = preview.after)
                Text(
                    text = stringResource(R.string.harvest_delete_index_caption, year),
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral600,
                    modifier = Modifier.weight(1f),
                )
            }
            if (preview.after == null) {
                Text(
                    text = stringResource(R.string.harvest_delete_no_index),
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral600,
                )
            }
            if (error != null) {
                Text(stringResource(error.messageRes()), style = MaterialTheme.typography.bodySmall, color = Terracotta700)
            }
            DangerPill(text = stringResource(R.string.harvest_delete_confirm), enabled = !isWorking, onClick = onConfirm)
            TextButton(onClick = onDismiss, enabled = !isWorking, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.harvest_cancel), style = MaterialTheme.typography.titleSmall, color = Neutral900)
            }
        }
    }
}

@Composable
private fun DangerPill(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 0.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Terracotta500, contentColor = Neutral0),
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}
