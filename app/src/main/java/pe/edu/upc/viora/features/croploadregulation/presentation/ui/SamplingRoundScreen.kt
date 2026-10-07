package pe.edu.upc.viora.features.croploadregulation.presentation.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.designsystem.theme.VioraTheme
import pe.edu.upc.viora.core.presentation.formatDecimal
import pe.edu.upc.viora.features.croploadregulation.domain.entity.TreeSample
import pe.edu.upc.viora.features.croploadregulation.presentation.state.SamplingSessionUiState
import pe.edu.upc.viora.features.croploadregulation.presentation.ui.component.SamplingSegmentedBar
import pe.edu.upc.viora.features.croploadregulation.presentation.viewmodel.SamplingSessionViewModel

private val ScreenPadding = 24.dp

/**
 * P52 · Ronda de Muestreo en Campo.
 * Shows representativeness target, live mean fruits per shoot, evaluated trees list and actions.
 */
@Composable
fun SamplingRoundScreen(
    state: SamplingSessionUiState,
    onNavigateBack: () -> Unit,
    onAddTree: (nextIdentifier: String, nextIndex: Int) -> Unit,
    onFinishSampling: () -> Unit,
    onContinueLater: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral100)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenPadding, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Neutral0),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.logbook_period_earlier),
                        tint = Neutral900,
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.plotName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                        ),
                        color = Neutral900,
                    )
                    Text(
                        text = stringResource(R.string.sampling_round_subtitle, state.campaignYear),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                        ),
                        color = Neutral600,
                    )
                }

                IconButton(
                    onClick = { /* More options */ },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Neutral0),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert),
                        contentDescription = null,
                        tint = Neutral900,
                    )
                }
            }

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding),
        ) {
            // Connectivity Banner
            if (state.isOffline) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(Terracotta100)
                        .padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Neutral0),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_cloud_sync),
                            contentDescription = null,
                            tint = Terracotta700,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Text(
                        text = stringResource(R.string.sampling_offline_banner),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                        ),
                        color = Terracotta700,
                    )
                }
                Spacer(Modifier.height(16.dp))
            } else if (state.hasRecoveredConnection) {
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Green900)
                            .padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Harvest300),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_task_alt),
                                contentDescription = null,
                                tint = Green900,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        val syncCount = if (state.syncedOnResumeCount > 0) state.syncedOnResumeCount else state.evaluatedTreesCount
                        Text(
                            text = stringResource(R.string.sampling_synced_banner, syncCount),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                            ),
                            color = Neutral50,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            } else {
                Spacer(Modifier.height(24.dp))
            }

            // Representativeness Card (Green800, radius 32dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(Green800)
                    .padding(22.dp),
            ) {
                Text(
                    text = stringResource(R.string.sampling_representativeness_title),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 1.5.sp,
                    ),
                    color = Color(0xC7F9F6F1),
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = state.evaluatedTreesCount.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 92.sp,
                            lineHeight = 92.sp,
                            letterSpacing = (-2).sp,
                        ),
                        color = Neutral50,
                    )
                    Text(
                        text = stringResource(R.string.sampling_trees_count_suffix, state.targetTreesCount),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 28.sp,
                            lineHeight = 34.sp,
                            fontStyle = FontStyle.Italic,
                        ),
                        color = Neutral50,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Segmented progress bar
                SamplingSegmentedBar(
                    completedCount = state.evaluatedTreesCount,
                    totalCount = state.targetTreesCount,
                    activeColor = Harvest300,
                    inactiveColor = Color(0x3DF9F6F1),
                )

                Spacer(Modifier.height(16.dp))

                val helperText = if (state.isRepresentative) {
                    stringResource(R.string.sampling_trees_sufficient_notice)
                } else {
                    stringResource(R.string.sampling_trees_missing_notice, state.treesMissing)
                }
                Text(
                    text = helperText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    ),
                    color = Color(0xCCF9F6F1),
                )
            }

            Spacer(Modifier.height(16.dp))

            // Two stats cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Card 1: mean fruits per shoot (Harvest100)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Harvest100)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    val formattedMean = formatDecimal(state.meanFruitsPerShoot, 2)
                    Text(
                        text = formattedMean,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 28.sp,
                            lineHeight = 32.sp,
                        ),
                        color = Neutral900,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.sampling_stat_mean_fruits),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                        ),
                        color = Neutral700,
                    )
                }

                // Card 2: saved in phone vs synchronized (Neutral0)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Neutral0)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    val localCount = state.samples.count { !it.isSynced }
                    val displayCount = if (localCount > 0) localCount else state.evaluatedTreesCount
                    Text(
                        text = displayCount.toString(),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 28.sp,
                            lineHeight = 32.sp,
                        ),
                        color = Neutral900,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (localCount > 0) {
                            stringResource(R.string.sampling_stat_saved_locally)
                        } else {
                            stringResource(R.string.sampling_stat_synced)
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                        ),
                        color = Neutral700,
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Evaluated Trees Section
            val treesTitle = if (state.evaluatedTreesCount > 0) {
                stringResource(R.string.sampling_evaluated_trees_title_with_count, state.evaluatedTreesCount)
            } else {
                stringResource(R.string.sampling_evaluated_trees_title)
            }
            Text(
                text = treesTitle,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                ),
                color = Neutral900,
            )

            Spacer(Modifier.height(12.dp))

            var isTreesExpanded by rememberSaveable { mutableStateOf(false) }
            val totalTrees = state.samples.size
            val visibleSamples = if (!isTreesExpanded && totalTrees > 3) {
                state.samples.take(3)
            } else {
                state.samples
            }

            if (state.samples.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.sampling_evaluated_trees_empty),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                        ),
                        color = Neutral600,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                Column(
                    modifier = Modifier.animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    visibleSamples.forEach { sample ->
                        EvaluatedTreeItem(
                            sample = sample,
                            isOffline = state.isOffline,
                        )
                    }
                }

                if (totalTrees > 3) {
                    Spacer(Modifier.height(14.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (isTreesExpanded) {
                                stringResource(R.string.sampling_action_view_less_trees)
                            } else {
                                stringResource(R.string.sampling_action_view_all_trees, totalTrees)
                            },
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                            ),
                            color = Green800,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(role = Role.Button) {
                                    isTreesExpanded = !isTreesExpanded
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // "Continuar después" link
            Text(
                text = stringResource(R.string.sampling_action_continue_later),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                ),
                color = Green800,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onContinueLater)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            )

            Spacer(Modifier.height(16.dp))
        }

        // Docked Action Bar pinned above the system navigation bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenPadding, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val isSampleComplete = state.isRepresentative || state.evaluatedTreesCount >= 5
                val addTreeBg = if (isSampleComplete) Neutral0 else Harvest300
                val iconCircleBg = if (isSampleComplete) Neutral100 else Neutral0
                val iconTint = if (isSampleComplete) Neutral900 else Green900
                val addTreeText = if (isSampleComplete) {
                    stringResource(R.string.sampling_action_another_tree)
                } else {
                    stringResource(R.string.sampling_action_add_tree, state.evaluatedTreesCount + 1)
                }

                // Left Button
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(CircleShape)
                        .background(addTreeBg)
                        .clickable(role = Role.Button) {
                            onAddTree(state.nextTreeIdentifier, state.evaluatedTreesCount + 1)
                        }
                        .padding(start = 6.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(iconCircleBg),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_add),
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    Text(
                        text = addTreeText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                        ),
                        color = Neutral900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Right Button: "Finalizar" / "Finalizar ronda"
                val isEnabled = isSampleComplete && !state.isSubmitting
                val finalizeBg = if (isEnabled) Green900 else Neutral200
                val finalizeText = if (isEnabled) Neutral50 else Neutral600
                val finalizeLabel = if (isSampleComplete) {
                    stringResource(R.string.sampling_action_finalize_round)
                } else {
                    stringResource(R.string.sampling_action_finish)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(CircleShape)
                        .background(finalizeBg)
                        .clickable(enabled = isEnabled, role = Role.Button, onClick = onFinishSampling),
                    contentAlignment = Alignment.Center,
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            color = Neutral50,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = finalizeLabel,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 15.sp,
                                lineHeight = 20.sp,
                            ),
                            color = finalizeText,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EvaluatedTreeItem(
    sample: TreeSample,
    isOffline: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(Neutral0)
            .padding(start = 8.dp, end = 14.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Tag circle
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Green200),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = sample.treeIdentifier,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                ),
                color = Green800,
            )
        }

        // Tree metrics
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = stringResource(R.string.sampling_tree_details, sample.shootsCount, sample.fruitSetCount),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                ),
                color = Neutral900,
                maxLines = 1,
            )

            val ratioStr = formatDecimal(sample.fruitsPerShoot, 2)
            Text(
                text = stringResource(R.string.sampling_tree_ratio, ratioStr),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                ),
                color = Neutral600,
                maxLines = 1,
            )
        }

        // Sync state badge
        if (sample.isSynced) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Green200)
                    .padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_task_alt),
                    contentDescription = null,
                    tint = Green800,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = stringResource(R.string.sampling_badge_synced),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                    ),
                    color = Green800,
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Harvest100)
                    .padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_cloud_sync),
                    contentDescription = null,
                    tint = Harvest800,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = stringResource(R.string.sampling_saved_on_phone_badge),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                    ),
                    color = Harvest800,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F0EA)
@Composable
private fun SamplingRoundScreenPreview_Normal() {
    val sampleList = listOf(
        TreeSample("A-01", 40, 24, 92.0, observedOn = java.time.LocalDate.now(), isSynced = true),
        TreeSample("A-02", 38, 20, null, observedOn = java.time.LocalDate.now(), isSynced = true),
        TreeSample("A-03", 42, 28, null, observedOn = java.time.LocalDate.now(), isSynced = true),
    )
    VioraTheme {
        SamplingRoundScreen(
            state = SamplingSessionUiState(
                plotId = "1",
                plotName = "La Finca 01",
                campaignYear = 2026,
                samples = sampleList,
                targetTreesCount = 5,
                isOffline = false,
                hasRecoveredConnection = false,
            ),
            onNavigateBack = {},
            onAddTree = { _, _ -> },
            onFinishSampling = {},
            onContinueLater = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F0EA)
@Composable
private fun SamplingRoundScreenPreview_Offline() {
    val sampleList = listOf(
        TreeSample("A-01", 40, 24, 92.0, observedOn = java.time.LocalDate.now(), isSynced = false),
        TreeSample("A-02", 38, 20, null, observedOn = java.time.LocalDate.now(), isSynced = false),
        TreeSample("A-03", 42, 28, null, observedOn = java.time.LocalDate.now(), isSynced = false),
    )
    VioraTheme {
        SamplingRoundScreen(
            state = SamplingSessionUiState(
                plotId = "1",
                plotName = "La Finca 01",
                campaignYear = 2026,
                samples = sampleList,
                targetTreesCount = 5,
                isOffline = true,
                hasRecoveredConnection = false,
            ),
            onNavigateBack = {},
            onAddTree = { _, _ -> },
            onFinishSampling = {},
            onContinueLater = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F0EA)
@Composable
private fun SamplingRoundScreenPreview_RecoveredConnection() {
    val sampleList = listOf(
        TreeSample("A-01", 40, 24, 92.0, observedOn = java.time.LocalDate.now(), isSynced = true),
        TreeSample("A-02", 38, 20, null, observedOn = java.time.LocalDate.now(), isSynced = true),
        TreeSample("A-03", 42, 28, null, observedOn = java.time.LocalDate.now(), isSynced = true),
        TreeSample("A-04", 40, 22, null, observedOn = java.time.LocalDate.now(), isSynced = true),
        TreeSample("A-05", 40, 26, null, observedOn = java.time.LocalDate.now(), isSynced = true),
    )
    VioraTheme {
        SamplingRoundScreen(
            state = SamplingSessionUiState(
                plotId = "1",
                plotName = "La Finca 01",
                campaignYear = 2026,
                samples = sampleList,
                targetTreesCount = 5,
                isOffline = false,
                hasRecoveredConnection = true,
                syncedOnResumeCount = 5,
            ),
            onNavigateBack = {},
            onAddTree = { _, _ -> },
            onFinishSampling = {},
            onContinueLater = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F0EA)
@Composable
private fun SamplingRoundScreenPreview_Empty() {
    VioraTheme {
        SamplingRoundScreen(
            state = SamplingSessionUiState(
                plotId = "1",
                plotName = "Magollo",
                campaignYear = 2026,
                samples = emptyList(),
                targetTreesCount = 5,
                isOffline = false,
                hasRecoveredConnection = false,
            ),
            onNavigateBack = {},
            onAddTree = { _, _ -> },
            onFinishSampling = {},
            onContinueLater = {},
        )
    }
}
