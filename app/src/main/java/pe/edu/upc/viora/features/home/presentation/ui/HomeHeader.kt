package pe.edu.upc.viora.features.home.presentation.ui

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.formatHectares

/**
 * Greeting row of the Home: avatar, "Hola, name" with the date and sync state, and the alerts
 * bell. [displayName] is null until the profile exists (then the greeting has no name and the
 * avatar shows the Viora isotype). [hasUnreadAlerts] draws the red dot on the bell.
 */
@Composable
fun HomeHeader(
    date: LocalDate,
    isOffline: Boolean,
    lastRefresh: Instant?,
    onOpenAlerts: () -> Unit,
    modifier: Modifier = Modifier,
    displayName: String? = null,
    hasUnreadAlerts: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(Green200),
            contentAlignment = Alignment.Center,
        ) {
            if (displayName != null) {
                Text(
                    text = initialsOf(displayName),
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp, lineHeight = 22.sp),
                    color = Green900,
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.ic_isotype),
                    contentDescription = null,
                    tint = Green900,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = if (displayName != null) stringResource(R.string.home_greeting, displayName) else stringResource(R.string.home_greeting_anonymous),
                style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 0.sp),
                color = Neutral900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = headerSubtitle(date, isOffline, lastRefresh),
                style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.sp),
                color = Neutral600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        AlertsBell(hasUnread = hasUnreadAlerts, onClick = onOpenAlerts)
    }
}

@Composable
private fun AlertsBell(hasUnread: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Neutral0)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_notifications),
            contentDescription = stringResource(R.string.home_alerts),
            tint = Neutral900,
            modifier = Modifier.size(24.dp),
        )
        if (hasUnread) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 7.dp, end = 7.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Terracotta500),
            )
        }
    }
}

@Composable
private fun headerSubtitle(date: LocalDate, isOffline: Boolean, lastRefresh: Instant?): String {
    if (isOffline) {
        val updated = lastRefresh?.let {
            DateUtils.getRelativeTimeSpanString(it.toEpochMilli(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()
        }
        if (updated != null) return stringResource(R.string.home_subtitle_offline, updated)
    }
    val pattern = stringResource(R.string.home_date_pattern)
    // Read from the configuration so the date follows the language when it changes.
    val locale = LocalConfiguration.current.locales[0]
    val text = DateTimeFormatter.ofPattern(pattern, locale).format(date)
        .replaceFirstChar { it.titlecase(locale) }
    return stringResource(R.string.home_subtitle_synced, text)
}

private fun initialsOf(name: String): String =
    name.trim().split(" ").filter { it.isNotEmpty() }.take(2).joinToString("") { it.first().uppercase() }

/** The two-line editorial headline: a lead and an italic emphasis ("Hoy toca *aclarar.*"). */
@Composable
fun HomeHeadline(lead: String, emphasis: String, modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.displayMedium.copy(fontSize = 44.sp, lineHeight = 46.sp, letterSpacing = (-1.1).sp)
    Column(modifier = modifier) {
        Text(text = lead, style = style)
        Text(text = emphasis, style = style, fontStyle = FontStyle.Italic)
    }
}

/**
 * Campaign chip (dark) plus, when there are plots, the plots-and-hectares chip (Figma "Contexto").
 * With several plots and an [onChangeFocus] the second chip names the plot in focus ([focusedName])
 * and opens the picker, so the producer sees which plot the cards of the Home talk about.
 */
@Composable
fun HomeContextChips(
    campaignYear: Int,
    plotCount: Int,
    totalHectares: Double,
    modifier: Modifier = Modifier,
    focusedName: String? = null,
    onChangeFocus: (() -> Unit)? = null,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ContextChip(
            text = stringResource(R.string.home_campaign, campaignYear),
            background = Green900,
            textColor = Neutral50,
        )
        if (plotCount > 1 && focusedName != null && onChangeFocus != null) {
            ContextChip(
                text = focusedName,
                background = Neutral0,
                textColor = Neutral900,
                dot = Green800,
                onClick = onChangeFocus,
                clickLabel = stringResource(R.string.home_focus_change),
                dropdown = true,
                modifier = Modifier.weight(1f, fill = false),
            )
        } else if (plotCount > 0) {
            ContextChip(
                text = pluralStringResource(R.plurals.home_plots_chip, plotCount, plotCount, formatHectares(totalHectares)),
                background = Neutral0,
                textColor = Neutral900,
                dot = Green800,
            )
        }
    }
}

@Composable
private fun ContextChip(
    text: String,
    background: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    dot: Color? = null,
    onClick: (() -> Unit)? = null,
    clickLabel: String? = null,
    dropdown: Boolean = false,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClickLabel = clickLabel, onClick = onClick) else Modifier)
            .padding(start = 12.dp, end = if (dropdown) 8.dp else 14.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (dot != null) Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(dot))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.sp),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (dropdown) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(16.dp).rotate(90f),
            )
        }
    }
}

/** "No signal" notice (Figma "Aviso · sin conexión"): what is recorded stays on the phone. */
@Composable
fun HomeOfflineNotice(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(Terracotta100)
            .padding(start = 10.dp, end = 18.dp, top = 10.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Neutral0),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_cloud_sync),
                contentDescription = null,
                tint = Terracotta700,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = stringResource(R.string.home_offline_notice),
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.sp),
            color = Terracotta700,
            modifier = Modifier.weight(1f),
        )
    }
}
