package pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.plotmanagement.presentation.state.MarkingMethod
import pe.edu.upc.viora.features.plotmanagement.presentation.state.RegisterPlotStep

/**
 * Step 1: how will the corners be marked, walking with the GPS or tracing on the map. The GPS is
 * the recommended way, but only while the app may use the precise location: without it the card
 * is locked (tapping it asks for the permission again, or opens the settings when Android will
 * not ask any more) and the map becomes the recommended way, so nobody gets stuck.
 */
@Composable
fun MethodStep(
    permission: LocationPermission,
    onStart: (MarkingMethod) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val recommended = if (permission.isGranted) MarkingMethod.GPS_WALK else MarkingMethod.MAP_TRACE
    var chosen by rememberSaveable { mutableStateOf<MarkingMethod?>(null) }
    // A GPS choice is void once the permission is gone (e.g. revoked in the settings meanwhile).
    val selected = chosen?.takeIf { it != MarkingMethod.GPS_WALK || permission.isGranted } ?: recommended

    // The settings are offered right after a request that Android answered with "never ask again".
    var requested by rememberSaveable { mutableStateOf(false) }
    var offerSettings by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(permission.isBlocked) {
        if (permission.isBlocked && requested) {
            offerSettings = true
            requested = false
        }
    }

    fun onGpsCardClick() {
        when {
            permission.isGranted -> chosen = MarkingMethod.GPS_WALK
            permission.isBlocked -> permission.openSettings()
            else -> {
                requested = true
                permission.request { granted -> if (granted) chosen = MarkingMethod.GPS_WALK }
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        StepHeader(
            step = RegisterPlotStep.METHOD.number,
            onBack = onBack,
            onClose = onClose,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
        )
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            val titleStyle = MaterialTheme.typography.displaySmall
            Column {
                Text(text = stringResource(R.string.method_title_lead), style = titleStyle)
                Text(text = stringResource(R.string.method_title_emphasis), style = titleStyle, fontStyle = FontStyle.Italic)
            }
            VoiceLine(text = stringResource(R.string.method_voice))
            Spacer(Modifier.size(2.dp))
            val locked = !permission.isGranted
            MethodCard(
                icon = if (locked) R.drawable.ic_lock else R.drawable.ic_location_on,
                title = stringResource(R.string.method_gps_title),
                body = stringResource(if (locked) R.string.method_gps_locked_body else R.string.method_gps_body),
                hint = stringResource(
                    when {
                        !locked -> R.string.method_gps_hint
                        permission.isBlocked -> R.string.method_gps_blocked_hint
                        else -> R.string.method_gps_locked_hint
                    },
                ),
                selected = selected == MarkingMethod.GPS_WALK,
                locked = locked,
                recommended = recommended == MarkingMethod.GPS_WALK,
                onClick = ::onGpsCardClick,
            )
            MethodCard(
                icon = R.drawable.ic_map,
                title = stringResource(R.string.method_map_title),
                body = stringResource(R.string.method_map_body),
                hint = stringResource(R.string.method_map_hint),
                selected = selected == MarkingMethod.MAP_TRACE,
                locked = false,
                recommended = recommended == MarkingMethod.MAP_TRACE,
                onClick = { chosen = MarkingMethod.MAP_TRACE },
            )
        }
        PrimaryPillButton(
            text = stringResource(
                if (selected == MarkingMethod.GPS_WALK) R.string.method_start_walk else R.string.method_start_trace,
            ),
            onClick = { onStart(selected) },
            trailingIcon = R.drawable.ic_arrow_forward,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
        )
    }

    if (offerSettings) {
        AlertDialog(
            onDismissRequest = { offerSettings = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            title = { Text(stringResource(R.string.location_settings_title), style = MaterialTheme.typography.headlineSmall) },
            text = { Text(stringResource(R.string.location_settings_body), style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(
                    onClick = {
                        offerSettings = false
                        permission.openSettings()
                    },
                ) { Text(stringResource(R.string.location_settings_open)) }
            },
            dismissButton = {
                TextButton(onClick = { offerSettings = false }) {
                    Text(stringResource(R.string.location_settings_dismiss), color = Neutral700)
                }
            },
            tonalElevation = 0.dp,
        )
    }
}

/**
 * One way of marking the plot. Selected, it is yellow with a dark outline and a check; not
 * selected it is green with an empty circle. [locked] swaps the icon for a padlock: the card
 * can be tapped to ask for what it needs, but it is never selected.
 */
@Composable
private fun MethodCard(
    @DrawableRes icon: Int,
    title: String,
    body: String,
    hint: String,
    selected: Boolean,
    locked: Boolean,
    recommended: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(32.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) Harvest100 else Green200)
            .then(if (selected) Modifier.border(BorderStroke(2.dp, Neutral900), shape) else Modifier)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { this.selected = selected; role = Role.RadioButton }
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(Neutral0), contentAlignment = Alignment.Center) {
                Icon(painter = painterResource(icon), contentDescription = null, tint = Neutral900)
            }
            if (recommended) {
                Text(
                    text = stringResource(R.string.method_recommended),
                    style = MaterialTheme.typography.labelLarge,
                    color = Neutral50,
                    modifier = Modifier.clip(CircleShape).background(Green900).padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
            Spacer(Modifier.weight(1f))
            SelectionMark(selected = selected)
        }
        Text(text = title, style = MaterialTheme.typography.headlineMedium, color = Neutral900)
        Text(text = body, style = MaterialTheme.typography.bodyLarge, color = Neutral700)
        Text(
            text = hint,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Harvest800 else if (locked) Terracotta700 else Green800,
        )
    }
}

/** A filled circle with a check when selected, an empty outlined circle when not. */
@Composable
private fun SelectionMark(selected: Boolean) {
    if (selected) {
        Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(Green800), contentAlignment = Alignment.Center) {
            Icon(painter = painterResource(R.drawable.ic_check), contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    } else {
        Box(modifier = Modifier.size(28.dp).border(1.5.dp, Neutral600.copy(alpha = 0.5f), CircleShape))
    }
}
