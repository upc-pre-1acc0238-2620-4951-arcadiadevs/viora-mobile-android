package pe.edu.upc.viora.features.croploadregulation.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.features.croploadregulation.domain.entity.SamplingSummary
import pe.edu.upc.viora.features.croploadregulation.presentation.ui.component.SamplingSegmentedBar
import pe.edu.upc.viora.features.croploadregulation.presentation.ui.component.VoiceOfViora

private val ScreenPadding = 24.dp

/**
 * P54 · Resumen Estadístico de Ronda Completa.
 * Displays 4 summary cards: evaluated trees, mean fruits per shoot, shoots count, and fruits count,
 * along with plot plan CTA and Return to Bitácora link.
 */
@Composable
fun SamplingCompleteScreen(
    plotName: String,
    summary: SamplingSummary?,
    evaluatedTreesCount: Int = 0,
    totalShootsCount: Int = 0,
    totalFruitsCount: Int = 0,
    meanFruitsPerShoot: Double = 0.0,
    isOffline: Boolean = false,
    onClose: () -> Unit,
    onViewPlotPlan: () -> Unit,
    onBackToLogbook: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    val treesCount = summary?.evaluatedTreesCount ?: evaluatedTreesCount
    val mean = summary?.meanFruitsPerShoot ?: meanFruitsPerShoot
    val fruitsCount = summary?.sampledFruitSetCount ?: totalFruitsCount
    val shootsCount = summary?.sampledShootsCount ?: totalShootsCount

    val formattedMean = String.format(Locale.ROOT, "%.2f", mean).replace('.', ',')
    val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.ROOT)
    val syncTimeStr = LocalTime.now().format(timeFormatter).lowercase()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral100),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = topInset + 8.dp,
                    bottom = 140.dp,
                    start = ScreenPadding,
                    end = ScreenPadding,
                ),
        ) {
            // Top Bar: Close Button + Sync/Offline Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Neutral0),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.nav_action_close),
                        tint = Neutral900,
                    )
                }

                // Sincronizado badge (Green200) vs En el teléfono badge (Harvest100)
                if (isOffline) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Harvest100)
                            .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Neutral0),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_cloud_sync),
                                contentDescription = null,
                                tint = Harvest800,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Text(
                            text = stringResource(R.string.sampling_saved_offline_badge),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                            ),
                            color = Harvest800,
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Green200)
                            .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Neutral0),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_task_alt),
                                contentDescription = null,
                                tint = Green800,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Text(
                            text = stringResource(R.string.sampling_complete_synced, syncTimeStr),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                            ),
                            color = Green800,
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // Headline: "Ronda *completa.*" vs "Ronda *guardada.*"
            Text(
                text = stringResource(R.string.sampling_complete_headline_lead),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 48.sp,
                    lineHeight = 50.sp,
                    letterSpacing = (-1.2).sp,
                ),
                color = Neutral900,
            )
            Text(
                text = if (isOffline) {
                    stringResource(R.string.sampling_saved_offline_headline_emphasis)
                } else {
                    stringResource(R.string.sampling_complete_headline_emphasis)
                },
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 48.sp,
                    lineHeight = 50.sp,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = (-1.2).sp,
                ),
                color = Neutral900,
            )

            Spacer(Modifier.height(16.dp))

            // Editorial / Voice of Viora
            val voiceText = if (isOffline) {
                stringResource(R.string.sampling_saved_offline_voice)
            } else {
                stringResource(R.string.sampling_complete_voice, plotName)
            }
            VoiceOfViora(
                text = voiceText,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))

            // 4 Metrics (2x2 Grid)
            // Row 1: Árboles evaluados (Green800) & Media frutos/brote (Harvest100)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Card 1: Árboles evaluados
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Green800)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = treesCount.toString(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 44.sp,
                            lineHeight = 46.sp,
                        ),
                        color = Neutral50,
                    )
                    Text(
                        text = stringResource(R.string.sampling_complete_trees_label),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                        ),
                        color = Color(0xCCF9F6F1),
                    )
                }

                // Card 2: Media frutos/brote
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Harvest100)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = formattedMean,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 44.sp,
                            lineHeight = 46.sp,
                        ),
                        color = Neutral900,
                    )
                    Text(
                        text = stringResource(R.string.sampling_complete_mean_label),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                        ),
                        color = Neutral700,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Row 2: Brotes observados (Neutral0) & Frutos cuajados (Neutral0)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Card 3: Brotes observados
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Neutral0)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = shootsCount.toString(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 44.sp,
                            lineHeight = 46.sp,
                        ),
                        color = Neutral900,
                    )
                    Text(
                        text = stringResource(R.string.sampling_complete_shoots_label),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                        ),
                        color = Neutral700,
                    )
                }

                // Card 4: Frutos cuajados
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Neutral0)
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = fruitsCount.toString(),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 44.sp,
                            lineHeight = 46.sp,
                        ),
                        color = Neutral900,
                    )
                    Text(
                        text = stringResource(R.string.sampling_complete_fruits_label),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                        ),
                        color = Neutral700,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Full Segmented Bar (5 of 5 filled)
            SamplingSegmentedBar(
                completedCount = 5,
                totalCount = 5,
                height = 14.dp,
                activeColor = Green800,
                inactiveColor = Green200,
            )
        }

        // Bottom CTAs
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = ScreenPadding, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Notice shown only when offline (Figma P54: Guardada sin señal)
            if (isOffline) {
                Text(
                    text = stringResource(R.string.sampling_saved_offline_plan_notice),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    ),
                    color = Neutral600,
                )
            }

            // Button: Ver plan del lote (disabled with opacity 0.4 when offline)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (isOffline) 0.4f else 1f)
                    .clip(CircleShape)
                    .background(Green900)
                    .clickable(enabled = !isOffline, role = Role.Button, onClick = onViewPlotPlan)
                    .padding(start = 24.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.sampling_complete_action_view_plan),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                    ),
                    color = Neutral50,
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Harvest300),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_forward),
                        contentDescription = null,
                        tint = Green900,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            // Text button: Volver a Bitácora (always active)
            Text(
                text = stringResource(R.string.sampling_complete_action_back_logbook),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                ),
                color = Green800,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onBackToLogbook)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFFF3F0EA)
@Composable
private fun SamplingCompleteScreenPreview_Synced() {
    pe.edu.upc.viora.core.designsystem.theme.VioraTheme {
        SamplingCompleteScreen(
            plotName = "La Finca 01",
            summary = null,
            evaluatedTreesCount = 5,
            totalShootsCount = 200,
            totalFruitsCount = 120,
            meanFruitsPerShoot = 0.60,
            isOffline = false,
            onClose = {},
            onViewPlotPlan = {},
            onBackToLogbook = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFFF3F0EA)
@Composable
private fun SamplingCompleteScreenPreview_Offline() {
    pe.edu.upc.viora.core.designsystem.theme.VioraTheme {
        SamplingCompleteScreen(
            plotName = "La Finca 01",
            summary = null,
            evaluatedTreesCount = 5,
            totalShootsCount = 200,
            totalFruitsCount = 120,
            meanFruitsPerShoot = 0.60,
            isOffline = true,
            onClose = {},
            onViewPlotPlan = {},
            onBackToLogbook = {},
        )
    }
}
