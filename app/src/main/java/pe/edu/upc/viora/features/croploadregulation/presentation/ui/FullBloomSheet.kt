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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.croploadregulation.presentation.state.FullBloomSheetState
import pe.edu.upc.viora.features.phenology.presentation.ui.FieldLabel
import pe.edu.upc.viora.features.phenology.presentation.ui.StepButton
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.CircleIconButton
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.PrimaryPillButton

/**
 * P64 · Plena floración: the day most flowers of the plot were open, moved a day at a time, and the thinning window it
 * would give by the technical profile of the variety. [window] is null without a profile, and then it is not previewed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullBloomSheet(
    state: FullBloomSheetState,
    plotName: String,
    campaignYear: Int,
    window: Pair<LocalDate, LocalDate>?,
    varietyName: String?,
    onStep: (Long) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]
    val short = DateTimeFormatter.ofPattern(stringResource(R.string.thinning_pattern_short), locale)
    fun LocalDate.short(): String = short.format(this).replace(".", "")

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
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    val titleStyle = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp, lineHeight = 38.sp)
                    Text(stringResource(R.string.full_bloom_title_lead), style = titleStyle, color = Neutral900)
                    Text(stringResource(R.string.full_bloom_title_emphasis), style = titleStyle.copy(fontStyle = FontStyle.Italic), color = Neutral900)
                    Text(
                        stringResource(R.string.full_bloom_subtitle, plotName, campaignYear),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = Neutral600,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                CircleIconButton(icon = R.drawable.ic_close, contentDescription = stringResource(R.string.action_close), onClick = onDismiss)
            }

            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Neutral0)
                    .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FieldLabel(stringResource(R.string.full_bloom_date_label))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    StepButton(
                        icon = null,
                        symbol = "−",
                        description = stringResource(R.string.full_bloom_previous_day),
                        dark = false,
                        onClick = { if (state.canStepBack) onStep(-1) },
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            state.date.short(),
                            style = MaterialTheme.typography.displayMedium.copy(fontSize = 44.sp, lineHeight = 48.sp),
                            color = Neutral900,
                        )
                        val todayText = DateTimeFormatter.ofPattern(stringResource(R.string.thinning_pattern_day_month), locale).format(state.today)
                        val daysAgo = ChronoUnit.DAYS.between(state.date, state.today).toInt()
                        Text(
                            if (daysAgo == 0) {
                                stringResource(R.string.full_bloom_today, todayText)
                            } else {
                                pluralStringResource(R.plurals.full_bloom_days_ago, daysAgo, daysAgo, todayText)
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                            color = Neutral600,
                        )
                    }
                    StepButton(
                        icon = R.drawable.ic_add,
                        symbol = null,
                        description = stringResource(R.string.full_bloom_next_day),
                        dark = state.canStepForward,
                        onClick = { if (state.canStepForward) onStep(+1) },
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Neutral0)
                    .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                FieldLabel(stringResource(R.string.full_bloom_what_label))
                Text(stringResource(R.string.full_bloom_what_body), style = MaterialTheme.typography.bodySmall, color = Neutral700)
            }

            if (window != null && varietyName != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Harvest100)
                        .padding(start = 14.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(window.first.short(), style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp), color = Neutral600)
                        Icon(painterResource(R.drawable.ic_arrow_forward), contentDescription = null, tint = Neutral900, modifier = Modifier.size(18.dp))
                        Text(window.second.short(), style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp), color = Neutral900)
                    }
                    Text(
                        stringResource(R.string.full_bloom_preview, varietyName),
                        style = MaterialTheme.typography.bodySmall,
                        color = Neutral700,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            state.error?.let {
                Text(stringResource(it.messageRes()), style = MaterialTheme.typography.bodySmall, color = Terracotta700)
            }
            PrimaryPillButton(
                text = stringResource(R.string.full_bloom_save),
                onClick = onSave,
                isLoading = state.isSaving,
                trailingIcon = R.drawable.ic_check,
            )
        }
    }
}
