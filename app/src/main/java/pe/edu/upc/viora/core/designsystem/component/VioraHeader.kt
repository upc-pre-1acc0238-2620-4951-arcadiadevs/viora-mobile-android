package pe.edu.upc.viora.core.designsystem.component

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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500

/**
 * Greeting row of the Home and the other top-level screens: avatar, "Hola, name" with the date and sync state, and the alerts
 * bell. [displayName] is the full name (initials of two names, greeting with the first); null until the profile exists (then the greeting has no name and the
 * avatar shows the Viora isotype). [hasUnreadAlerts] draws the red dot on the bell.
 */
@Composable
fun VioraGreetingHeader(
    date: LocalDate,
    isOffline: Boolean,
    lastRefresh: Instant?,
    onOpenAlerts: () -> Unit,
    modifier: Modifier = Modifier,
    displayName: String? = null,
    hasUnreadAlerts: Boolean = false,
    onOpenAccount: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Green200)
                .clickable(onClick = onOpenAccount),
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
                text = if (displayName != null) stringResource(R.string.home_greeting, displayName.trim().substringBefore(' ')) else stringResource(R.string.home_greeting_anonymous),
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
    // Read from the configuration so the texts follow the language when it changes.
    val locale = LocalConfiguration.current.locales[0]
    if (isOffline) {
        // Android writes "Hace 4 días"; it goes mid-sentence ("actualizado hace 4 días").
        val updated = lastRefresh?.let {
            DateUtils.getRelativeTimeSpanString(it.toEpochMilli(), System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
                .toString()
                .replaceFirstChar { char -> char.lowercase(locale) }
        }
        if (updated != null) return stringResource(R.string.home_subtitle_offline, updated)
    }
    val pattern = stringResource(R.string.home_date_pattern)
    val text = DateTimeFormatter.ofPattern(pattern, locale).format(date)
        .replaceFirstChar { it.titlecase(locale) }
    return stringResource(R.string.home_subtitle_synced, text)
}

private fun initialsOf(name: String): String =
    name.trim().split(" ").filter { it.isNotEmpty() }.take(2).joinToString("") { it.first().uppercase() }

/** The two-line editorial headline: a lead and an italic emphasis ("Hoy toca *aclarar.*"). */
@Composable
fun VioraHeadline(lead: String, emphasis: String, modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.displayMedium.copy(fontSize = 44.sp, lineHeight = 46.sp, letterSpacing = (-1.1).sp)
    Column(modifier = modifier) {
        Text(text = lead, style = style)
        Text(text = emphasis, style = style, fontStyle = FontStyle.Italic)
    }
}
