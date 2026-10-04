package pe.edu.upc.viora.features.phenology.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.phenology.domain.entity.BbiClass
import pe.edu.upc.viora.features.phenology.presentation.state.BbiExample
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.PrimaryPillButton

/** "What does this index measure?" (Figma P40 sheet), worked out with the plot's own figures. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BbiInfoSheet(
    index: Double?,
    bbiClass: BbiClass?,
    example: BbiExample?,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { Box(Modifier.padding(top = 12.dp).width(36.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Neutral300)) },
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = stringResource(R.string.harvest_bbi_eyebrow),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.88.sp),
                color = Neutral600,
            )
            Column {
                Text(stringResource(R.string.harvest_info_title_lead), style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp, lineHeight = 38.sp), color = Neutral900)
                Text(
                    stringResource(R.string.harvest_info_title_emphasis),
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp, lineHeight = 38.sp, fontStyle = FontStyle.Italic),
                    color = Neutral900,
                )
            }
            Text(stringResource(R.string.harvest_info_body), style = MaterialTheme.typography.bodyMedium, color = Neutral700)
            if (example != null) ExampleCard(example)
            ScaleCard(index = index, active = bbiClass)
            PrimaryPillButton(text = stringResource(R.string.harvest_info_got_it), onClick = onDismiss)
        }
    }
}

@Composable
private fun ExampleCard(example: BbiExample) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Neutral0).padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = stringResource(R.string.harvest_info_example_title, example.fromYear, example.toYear),
            style = MaterialTheme.typography.labelMedium,
            color = Neutral600,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            KgPill(formatKg(example.fromKg), background = Green800, textColor = Neutral0)
            Text("→", style = MaterialTheme.typography.bodyLarge, color = Neutral600)
            KgPill(formatKg(example.toKg), background = Harvest300, textColor = Neutral900)
            Text("=", style = MaterialTheme.typography.bodyLarge, color = Neutral600)
            Text(formatIndex(example.value), style = MaterialTheme.typography.headlineLarge, color = Neutral900)
        }
        Text(
            text = stringResource(R.string.harvest_info_example_note, formatKg(example.differenceKg), formatKg(example.sumKg)),
            style = MaterialTheme.typography.bodySmall,
            color = Neutral600,
        )
    }
}

@Composable
private fun KgPill(text: String, background: androidx.compose.ui.graphics.Color, textColor: androidx.compose.ui.graphics.Color) {
    Text(
        text = stringResource(R.string.harvest_kg_value, text),
        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
        color = textColor,
        modifier = Modifier.clip(CircleShape).background(background).padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
private fun ScaleCard(index: Double?, active: BbiClass?) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Neutral0).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        BbiClass.entries.forEach { entry ->
            val isActive = entry == active
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isActive) Terracotta100 else Neutral0)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.width(6.dp).height(32.dp).clip(RoundedCornerShape(3.dp)).background(entry.color()))
                Column(Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(entry.labelRes()),
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isActive) Terracotta700 else Neutral900,
                        )
                        Text(stringResource(entry.rangeRes()), style = MaterialTheme.typography.bodySmall, color = Neutral600)
                    }
                    Text(stringResource(entry.meaningRes()), style = MaterialTheme.typography.bodySmall, color = Neutral600)
                }
                if (isActive && index != null) {
                    Text(
                        text = stringResource(R.string.harvest_info_your_plot, formatIndex(index)),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                        color = Neutral0,
                        modifier = Modifier.clip(CircleShape).background(Terracotta500).padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
        }
    }
}

private fun BbiClass.meaningRes(): Int = when (this) {
    BbiClass.LOW -> R.string.harvest_info_low_meaning
    BbiClass.MODERATE -> R.string.harvest_info_moderate_meaning
    BbiClass.SEVERE -> R.string.harvest_info_severe_meaning
}
