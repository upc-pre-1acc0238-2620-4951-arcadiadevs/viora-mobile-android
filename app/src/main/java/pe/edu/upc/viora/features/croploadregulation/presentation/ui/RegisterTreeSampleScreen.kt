package pe.edu.upc.viora.features.croploadregulation.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.presentation.formatDecimal

private val ScreenPadding = 24.dp

/**
 * P53 · Registrar Árbol de Muestreo en Campo.
 * Interactive counters for shoots and fruits with real-time live calculation chip,
 * tree tag, and optional trunk circumference.
 */
@Composable
fun RegisterTreeSampleScreen(
    plotName: String,
    treeIndex: Int,
    targetTreesCount: Int = 5,
    defaultIdentifier: String = "A-01",
    initialShootsCount: Int = 0,
    initialFruitSetCount: Int = 0,
    isOffline: Boolean = false,
    onClose: () -> Unit,
    onSaveTree: (identifier: String, shootsCount: Int, fruitsCount: Int, circumferenceCm: Double?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var treeTag by remember(defaultIdentifier) { mutableStateOf(defaultIdentifier) }
    var shootsCount by remember(initialShootsCount) { mutableIntStateOf(initialShootsCount) }
    var fruitSetCount by remember(initialFruitSetCount) { mutableIntStateOf(initialFruitSetCount) }
    var circumferenceText by remember { mutableStateOf("") }

    val maxAllowedFruits = shootsCount * 4
    val isOutOfRange = fruitSetCount > maxAllowedFruits

    val liveRatio = if (shootsCount > 0) fruitSetCount.toDouble() / shootsCount else 0.0
    val formattedRatio = formatDecimal(liveRatio, 2)

    val subtitleText = if (isOffline) {
        stringResource(R.string.sampling_register_tree_subtitle, plotName)
    } else {
        plotName
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral100)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
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

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.sampling_register_tree_title, treeIndex, targetTreesCount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 15.sp,
                        lineHeight = 20.sp,
                    ),
                    color = Neutral900,
                )
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    ),
                    color = Neutral600,
                )
            }

            Spacer(Modifier.size(48.dp))
        }

        // Scrollable Form Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = ScreenPadding),
        ) {

            Spacer(Modifier.height(24.dp))

            // Headline: "Árbol *A-14.*"
            Text(
                text = stringResource(R.string.sampling_register_tree_headline_lead),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 44.sp,
                    lineHeight = 46.sp,
                    letterSpacing = (-1).sp,
                ),
                color = Neutral900,
            )
            Text(
                text = "$treeTag.",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 44.sp,
                    lineHeight = 46.sp,
                    fontStyle = FontStyle.Italic,
                    letterSpacing = (-1).sp,
                ),
                color = Neutral900,
            )

            Spacer(Modifier.height(16.dp))

            // Tree Tag Input Field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Neutral0)
                    .padding(start = 18.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.sampling_tree_tag_label),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                        ),
                        color = Green800,
                    )
                    BasicTextField(
                        value = treeTag,
                        onValueChange = { treeTag = it },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            color = Neutral900,
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Neutral100),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_tag),
                        contentDescription = null,
                        tint = Green800,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Counter 1: Brotes observados
            CounterCard(
                title = stringResource(R.string.sampling_shoots_observed_title),
                subtitle = stringResource(R.string.sampling_shoots_observed_subtitle),
                count = shootsCount,
                onDecrement = { if (shootsCount > 1) shootsCount-- },
                onIncrement = { shootsCount++ },
            )

            Spacer(Modifier.height(12.dp))

            // Counter 2: Frutos cuajados (highlighted with Terracotta when out of range)
            CounterCard(
                title = stringResource(R.string.sampling_fruit_set_title),
                subtitle = stringResource(R.string.sampling_fruit_set_subtitle),
                count = fruitSetCount,
                onDecrement = { if (fruitSetCount > 0) fruitSetCount-- },
                onIncrement = { fruitSetCount++ },
                isError = isOutOfRange,
            )

            // Warning Alert Row (Figma P53: Conteo fuera de rango)
            if (isOutOfRange) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_warning),
                        contentDescription = null,
                        tint = Terracotta700,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(
                            R.string.sampling_error_fruits_out_of_range,
                            shootsCount,
                            maxAllowedFruits,
                        ),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                        ),
                        color = Terracotta700,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Live Calculation Chip (Harvest100)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Harvest100)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (isOutOfRange) "—" else formattedRatio,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        lineHeight = 32.sp,
                    ),
                    color = Neutral900,
                )
                Text(
                    text = if (isOutOfRange) {
                        stringResource(R.string.sampling_live_calculated_invalid)
                    } else {
                        stringResource(R.string.sampling_live_calculated_suffix)
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    ),
                    color = Neutral700,
                )
            }

            Spacer(Modifier.height(12.dp))

            // Field: Contorno del tronco (opcional)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Neutral0)
                    .padding(start = 18.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.sampling_trunk_circumference_label),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                        ),
                        color = Green800,
                    )
                    BasicTextField(
                        value = circumferenceText,
                        onValueChange = { circumferenceText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            color = Neutral900,
                        ),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                if (circumferenceText.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.sampling_trunk_circumference_hint),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 14.sp,
                                            color = Neutral600,
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    innerTextField()
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        innerTextField()
                                        Text(
                                            text = stringResource(R.string.sampling_trunk_circumference_suffix),
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontSize = 14.sp,
                                                color = Neutral600,
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Neutral100),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_straighten),
                        contentDescription = null,
                        tint = Green800,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        // Bottom CTA: Guardar árbol (docked above navigation bar)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ScreenPadding, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (isOutOfRange) 0.4f else 1f)
                    .clip(CircleShape)
                    .background(Green900)
                    .clickable(enabled = !isOutOfRange, role = Role.Button) {
                        val circumference = circumferenceText.toDoubleOrNull()
                        onSaveTree(treeTag, shootsCount, fruitSetCount, circumference)
                    }
                    .padding(start = 24.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.sampling_action_save_tree),
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
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = Green900,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CounterCard(
    title: String,
    subtitle: String,
    count: Int,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    val borderModifier = if (isError) {
        Modifier.border(2.dp, Terracotta500, RoundedCornerShape(28.dp))
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(borderModifier)
            .clip(RoundedCornerShape(28.dp))
            .background(Neutral0)
            .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
            color = Neutral900,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
            ),
            color = Neutral600,
        )

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 56.sp,
                    lineHeight = 56.sp,
                    letterSpacing = (-1).sp,
                ),
                color = if (isError) Terracotta700 else Neutral900,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Decrement Button
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Neutral100)
                        .clickable(role = Role.Button, onClick = onDecrement),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height(2.6.dp)
                            .clip(RoundedCornerShape(1.3.dp))
                            .background(Neutral900),
                    )
                }

                // Increment Button
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Green800)
                        .clickable(role = Role.Button, onClick = onIncrement),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = null,
                        tint = Neutral0,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFFF3F0EA)
@Composable
private fun RegisterTreeSampleScreenPreview_Normal() {
    pe.edu.upc.viora.core.designsystem.theme.VioraTheme {
        RegisterTreeSampleScreen(
            plotName = "La Finca 01",
            treeIndex = 1,
            targetTreesCount = 5,
            defaultIdentifier = "A-01",
            initialShootsCount = 40,
            initialFruitSetCount = 24,
            onClose = {},
            onSaveTree = { _, _, _, _ -> },
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFFF3F0EA)
@Composable
private fun RegisterTreeSampleScreenPreview_OutOfRange() {
    pe.edu.upc.viora.core.designsystem.theme.VioraTheme {
        RegisterTreeSampleScreen(
            plotName = "La Finca 01",
            treeIndex = 2,
            targetTreesCount = 5,
            defaultIdentifier = "A-14",
            initialShootsCount = 40,
            initialFruitSetCount = 168,
            onClose = {},
            onSaveTree = { _, _, _, _ -> },
        )
    }
}
