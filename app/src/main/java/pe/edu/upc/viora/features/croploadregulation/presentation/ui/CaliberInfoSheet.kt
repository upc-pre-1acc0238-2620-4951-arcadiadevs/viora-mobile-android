package pe.edu.upc.viora.features.croploadregulation.presentation.ui

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral500
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.presentation.formatDecimal
import pe.edu.upc.viora.features.croploadregulation.domain.entity.CaliberProjection
import pe.edu.upc.viora.features.croploadregulation.domain.entity.LoadBalance
import pe.edu.upc.viora.features.croploadregulation.presentation.state.CaliberCard
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.PrimaryPillButton

/**
 * "¿Cómo se calcula?" of P63, built like the BBI sheet of Alternancia (P40): what the caliber compares, the plot's own
 * load and harvests, and the four answers it can give with the plot's one marked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CaliberInfoSheet(
    active: CaliberCard?,
    projection: CaliberProjection?,
    load: LoadBalance?,
    variety: String,
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
                text = stringResource(R.string.thinning_result_caliber_eyebrow),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.88.sp),
                color = Neutral600,
            )
            Column {
                val title = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp, lineHeight = 38.sp)
                Text(stringResource(R.string.caliber_info_title_lead), style = title, color = Neutral900)
                Text(stringResource(R.string.caliber_info_title_emphasis), style = title.copy(fontStyle = FontStyle.Italic), color = Neutral900)
            }
            Text(stringResource(R.string.caliber_info_body, variety), style = MaterialTheme.typography.bodyMedium, color = Neutral700)
            if (load != null) PlotCard(load, projection, variety)
            AnswersCard(active = active, required = projection?.requiredObservations?.takeIf { it > 0 })
            PrimaryPillButton(text = stringResource(R.string.harvest_info_got_it), onClick = onDismiss)
        }
    }
}

/** "Tu aclareo": the load before → after, and how many real harvests of the variety back the answer. */
@Composable
private fun PlotCard(load: LoadBalance, projection: CaliberProjection?, variety: String) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Neutral0).padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(stringResource(R.string.caliber_info_plot_title), style = MaterialTheme.typography.labelMedium, color = Neutral600)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            LoadPill(formatDecimal(load.beforeFruitsPerShoot, 2), background = Green800, textColor = Neutral0)
            Text("→", style = MaterialTheme.typography.bodyLarge, color = Neutral600)
            LoadPill(formatDecimal(load.residualFruitsPerShoot, 2), background = Harvest300, textColor = Neutral900)
            Text(stringResource(R.string.thinning_result_fruits_per_shoot), style = MaterialTheme.typography.bodyMedium, color = Neutral700)
        }
        projection?.let {
            Text(
                text = if (it.calibrationObservations >= it.requiredObservations) {
                    stringResource(R.string.caliber_info_plot_harvests_enough, it.calibrationObservations, variety)
                } else {
                    stringResource(R.string.caliber_info_plot_harvests, it.calibrationObservations, variety, it.requiredObservations)
                },
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
            )
        }
    }
}

@Composable
private fun LoadPill(text: String, background: Color, textColor: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
        color = textColor,
        modifier = Modifier.clip(CircleShape).background(background).padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

/** The four answers of the caliber, like the BBI scale: a colour bar, the name, what it means; "Tu lote" on the active one. */
@Composable
private fun AnswersCard(active: CaliberCard?, required: Int?) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Neutral0).padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CaliberCard.entries.forEach { entry ->
            val isActive = entry == active
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isActive) Harvest100 else Neutral0)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.width(6.dp).height(32.dp).clip(RoundedCornerShape(3.dp)).background(entry.color()))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(entry.labelRes()), style = MaterialTheme.typography.titleSmall, color = if (isActive) Green800 else Neutral900)
                    Text(entry.meaning(required), style = MaterialTheme.typography.bodySmall, color = Neutral600)
                }
                if (isActive) {
                    Text(
                        text = stringResource(R.string.caliber_info_your_plot),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                        color = Neutral0,
                        modifier = Modifier.clip(CircleShape).background(Green800).padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
        }
    }
}

private fun CaliberCard.color(): Color = when (this) {
    CaliberCard.ESTIMATED -> Green800
    CaliberCard.LEARNING -> Harvest300
    CaliberCard.LATE -> Terracotta500
    CaliberCard.OUT_OF_RANGE -> Neutral500
}

private fun CaliberCard.labelRes(): Int = when (this) {
    CaliberCard.ESTIMATED -> R.string.caliber_info_estimated
    CaliberCard.LEARNING -> R.string.caliber_info_learning
    CaliberCard.LATE -> R.string.caliber_info_late
    CaliberCard.OUT_OF_RANGE -> R.string.caliber_info_out_of_range
}

@Composable
private fun CaliberCard.meaning(required: Int?): String = when (this) {
    CaliberCard.ESTIMATED -> required?.let { stringResource(R.string.caliber_info_estimated_meaning, it) }
        ?: stringResource(R.string.caliber_info_estimated_meaning_plain)
    CaliberCard.LEARNING -> stringResource(R.string.caliber_info_learning_meaning)
    CaliberCard.LATE -> stringResource(R.string.caliber_info_late_meaning)
    CaliberCard.OUT_OF_RANGE -> stringResource(R.string.caliber_info_out_of_range_meaning)
}
