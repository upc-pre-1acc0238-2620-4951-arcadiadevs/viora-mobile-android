package pe.edu.upc.viora.features.telemetry.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.compose.runtime.SideEffect
import android.os.Build
import android.view.WindowManager
import java.time.Duration
import java.time.Instant
import pe.edu.upc.viora.core.designsystem.theme.Terracotta600
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderItalic
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.designsystem.theme.VioraTheme
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorNode
import pe.edu.upc.viora.features.telemetry.domain.entity.SensorType
import pe.edu.upc.viora.features.telemetry.presentation.state.ConfigureNodeUiState
import pe.edu.upc.viora.features.telemetry.presentation.viewmodel.ConfigureNodeViewModel

@Composable
fun ConfigureNodeScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onUnlinked: () -> Unit,
    plotName: String,
    modifier: Modifier = Modifier,
    viewModel: ConfigureNodeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showUnlinkDialog by remember { mutableStateOf(false) }

    ConfigureNodeScreenContent(
        state = state,
        plotName = plotName,
        onBack = onBack,
        onNameChange = viewModel::onNameChange,
        onDepthChange = viewModel::onDepthChange,
        onTransmitReadingsChange = viewModel::onTransmitReadingsChange,
        onUnlink = { showUnlinkDialog = true },
        onSave = { viewModel.save(onSaved) },
        modifier = modifier,
    )

    if (showUnlinkDialog) {
        UnlinkNodeDialog(
            node = state.node,
            activeNodeCount = state.nodeCount,
            isLoading = state.isUnlinking,
            error = state.error,
            onConfirm = { viewModel.unlink { onUnlinked() } },
            onDismiss = { if (!state.isUnlinking) showUnlinkDialog = false },
        )
    }
}

@Composable
private fun ConfigureNodeScreenContent(
    state: ConfigureNodeUiState,
    plotName: String,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onDepthChange: (Int) -> Unit,
    onTransmitReadingsChange: (Boolean) -> Unit,
    onUnlink: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral100)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircleIconButton(
                icon = R.drawable.ic_arrow_back,
                contentDescription = stringResource(R.string.action_back),
                onClick = onBack,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.node?.name.orEmpty().ifBlank { " " }, style = MaterialTheme.typography.titleMedium, color = Neutral900)
                Text(
                    stringResource(R.string.sensors_config_subtitle, state.plotName.ifBlank { plotName }),
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral600,
                )
            }
            Spacer(Modifier.size(44.dp))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(18.dp))
            Text(
                text = stringResource(R.string.sensors_config_title_lead),
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp, lineHeight = 40.sp),
                color = Neutral900,
            )
            Text(
                text = stringResource(R.string.sensors_config_title_emphasis),
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp, lineHeight = 40.sp).merge(NewsreaderItalic),
                color = Neutral900,
            )

            Spacer(Modifier.height(22.dp))

            state.node?.let { node ->
                ReadingCard(node)
            } ?: run {
                Box(Modifier.fillMaxWidth().padding(vertical = 50.dp), contentAlignment = Alignment.Center) {
                    if (state.isLoading) CircularProgressIndicator(color = Green900)
                }
            }

            Spacer(Modifier.height(14.dp))

            NodeNameField(
                value = state.name,
                isError = state.nameError,
                onValueChange = onNameChange,
            )

            Spacer(Modifier.height(20.dp))
            Text(
                stringResource(R.string.sensors_config_depth_label),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                color = Neutral600,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ConfigChoicePill(stringResource(R.string.sensors_depth_30), state.depthCm == 30, { onDepthChange(30) })
                ConfigChoicePill(stringResource(R.string.sensors_depth_60), state.depthCm == 60, { onDepthChange(60) })
            }
            Text(
                stringResource(R.string.sensors_config_depth_hint),
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = Neutral600,
                modifier = Modifier.padding(top = 8.dp),
            )

            Spacer(Modifier.height(22.dp))
            TransmissionRow(
                enabled = state.transmitReadings,
                onChange = onTransmitReadingsChange,
            )

            if (state.error != null) {
                Text(
                    stringResource(state.error.messageRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = Terracotta700,
                    modifier = Modifier.padding(top = 12.dp, start = 4.dp),
                )
            }

            Spacer(Modifier.height(20.dp))
            TextButton(
                onClick = onUnlink,
                enabled = !state.isSaving && !state.isUnlinking,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text(stringResource(R.string.sensors_unlink_action), color = Terracotta700)
            }
            Spacer(Modifier.height(12.dp))
        }

        SaveNodeButton(
            enabled = state.canSave,
            loading = state.isSaving,
            onClick = onSave,
        )
    }
}

@Composable
private fun ReadingCard(node: SensorNode) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Neutral0)
            .padding(18.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column {
                Text(stringResource(R.string.sensors_config_last_reading), style = MaterialTheme.typography.labelSmall, color = Neutral600)
                Text(
                    text = node.lastHumidityPercent?.let { String.format("%.0f %%", it) } ?: "—",
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 48.sp, lineHeight = 52.sp),
                    color = Neutral900,
                )
                Text(
                    stringResource(R.string.sensors_config_soil_reading, node.depthCm ?: 30),
                    style = MaterialTheme.typography.bodySmall,
                    color = Neutral600,
                )
            }
            Box(
                Modifier.size(46.dp).clip(CircleShape).background(Neutral200),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(R.drawable.ic_water_drop), null, tint = Neutral700, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier.clip(RoundedCornerShape(percent = 50)).background(Green200).padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(Neutral700))
            Text(
                if (node.status.name == "ACTIVE") {
                    timeAgo(node.lastReadingAt)?.let { stringResource(R.string.sensors_active_since, it) }
                        ?: stringResource(R.string.sensors_status_active)
                } else stringResource(R.string.sensors_paused_simple),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                color = Neutral900,
            )
        }
    }
}

@Composable
private fun TransmissionRow(enabled: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Neutral0).padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.sensors_transmit_title), style = MaterialTheme.typography.titleMedium, color = Neutral900)
            Text(stringResource(R.string.sensors_transmit_subtitle), style = MaterialTheme.typography.bodySmall, color = Neutral600)
        }
        SimpleToggle(enabled = enabled, onChange = onChange)
    }
}

@Composable
private fun SimpleToggle(enabled: Boolean, onChange: (Boolean) -> Unit) {
    Box(
        Modifier
            .size(width = 64.dp, height = 36.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(if (enabled) Green900 else Neutral300)
            .clickable(role = Role.Switch, onClick = { onChange(!enabled) }),
        contentAlignment = if (enabled) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(Modifier.padding(4.dp).size(28.dp).clip(CircleShape).background(Neutral0))
    }
}

@Composable
private fun ConfigChoicePill(text: String, selected: Boolean, onClick: () -> Unit) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
        color = if (selected) Neutral0 else Neutral900,
        modifier = Modifier.clip(RoundedCornerShape(percent = 50))
            .background(if (selected) Green900 else Neutral0)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
    )
}

@Composable
private fun SaveNodeButton(enabled: Boolean, loading: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(if (enabled) Green900 else Neutral300)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(start = 22.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.sensors_save_changes), style = MaterialTheme.typography.titleMedium, color = Neutral0)
        Box(Modifier.size(44.dp).clip(CircleShape).background(if (enabled) Harvest300 else Neutral200), contentAlignment = Alignment.Center) {
            if (loading) CircularProgressIndicator(color = Neutral900, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
            else Icon(painterResource(R.drawable.ic_check), null, tint = Neutral900, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun NodeNameField(value: String, isError: Boolean, onValueChange: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Neutral0)
            .padding(horizontal = 18.dp, vertical = 12.dp),
    ) {
        Text(
            stringResource(R.string.sensors_config_name_label),
            style = MaterialTheme.typography.bodySmall,
            color = if (isError) Terracotta700 else Neutral600,
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = Neutral900),
            cursorBrush = SolidColor(Green900),
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
        )
    }
}

/** "10 min", "3 h" or "2 d" since [iso]; null when there is no usable timestamp. */
private fun timeAgo(iso: String?): String? {
    val at = iso?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: return null
    val d = Duration.between(at, Instant.now()).coerceAtLeast(Duration.ZERO)
    return when {
        d.toMinutes() < 60 -> "${maxOf(d.toMinutes(), 1)} min"
        d.toHours() < 24 -> "${d.toHours()} h"
        else -> "${d.toDays()} d"
    }
}

@Composable
private fun UnlinkNodeDialog(
    node: SensorNode?,
    activeNodeCount: Int,
    isLoading: Boolean,
    error: pe.edu.upc.viora.core.domain.AppError?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        // Blur what is behind the dialog (Android 12+); older versions keep the plain dim.
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect {
            if (window != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                window.attributes = window.attributes.apply { blurBehindRadius = 24 }
            }
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Neutral0)
                .padding(24.dp),
        ) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(Terracotta100), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_warning), null, tint = Terracotta700, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.sensors_unlink_title_lead), style = MaterialTheme.typography.headlineSmall, color = Neutral900)
            Text(
                stringResource(R.string.sensors_unlink_title_emphasis, node?.name.orEmpty()),
                style = MaterialTheme.typography.headlineSmall.merge(NewsreaderItalic),
                color = Neutral900,
            )
            Spacer(Modifier.height(14.dp))
            Text(stringResource(R.string.sensors_unlink_body), style = MaterialTheme.typography.bodyMedium, color = Neutral700)
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Neutral100).padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("$activeNodeCount", style = MaterialTheme.typography.displaySmall.copy(fontSize = 22.sp, lineHeight = 26.sp), color = Neutral600)
                Text("→", style = MaterialTheme.typography.bodyMedium, color = Neutral900)
                Text(
                    "${(activeNodeCount - 1).coerceAtLeast(0)}",
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 26.sp, lineHeight = 30.sp),
                    color = Neutral900,
                )
                Text(stringResource(R.string.sensors_unlink_count_label), style = MaterialTheme.typography.bodySmall, color = Neutral600)
            }
            if (error != null) {
                Text(
                    stringResource(error.messageRes()),
                    color = Terracotta700,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
            Spacer(Modifier.height(18.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(percent = 50))
                    .background(Terracotta600)
                    .clickable(enabled = !isLoading, role = Role.Button, onClick = onConfirm),
                contentAlignment = Alignment.Center,
            ) {
                if (isLoading) CircularProgressIndicator(color = Neutral0, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                else Text(stringResource(R.string.sensors_unlink_confirm), style = MaterialTheme.typography.titleMedium, color = Neutral0)
            }
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text(stringResource(R.string.sensors_cancel), color = Neutral900)
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFFF3F0EA, heightDp = 800)
@Composable
private fun ConfigureNodePreview() {
    VioraTheme {
        ConfigureNodeScreenContent(
            state = ConfigureNodeUiState(
                node = SensorNode("1", "p1", "Sonda Sector Norte", SensorType.SONDA_SUELO, 30, pe.edu.upc.viora.features.telemetry.domain.entity.SensorStatus.ACTIVE, "", null, 34.0),
                name = "Sonda Sector Norte",
                depthCm = 30,
            ),
            plotName = "La Yarada 02",
            onBack = {}, onNameChange = {}, onDepthChange = {}, onTransmitReadingsChange = {}, onUnlink = {}, onSave = {},
        )
    }
}

@Composable
private fun CircleIconButton(
    icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Neutral0)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = Neutral900,
            modifier = Modifier.size(22.dp),
        )
    }
}
