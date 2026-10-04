package pe.edu.upc.viora.features.home.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900

/**
 * The current week, Monday to Sunday (Figma "Semana"): today in yellow, past days dimmed.
 * The terracotta dot under a day that has a task is not drawn yet: the plan of the season
 * (US27) will say which days have one.
 */
@Composable
fun HomeWeekStrip(today: LocalDate, modifier: Modifier = Modifier) {
    val monday = today.with(DayOfWeek.MONDAY)
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        repeat(7) { offset ->
            val day = monday.plusDays(offset.toLong())
            WeekDay(day = day, isToday = day == today, isPast = day.isBefore(today))
        }
    }
}

@Composable
private fun WeekDay(day: LocalDate, isToday: Boolean, isPast: Boolean) {
    val locale = LocalConfiguration.current.locales[0]
    val label = day.dayOfWeek.getDisplayName(TextStyle.SHORT, locale)
        .trimEnd('.')
        .replaceFirstChar { it.titlecase(locale) }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = if (isToday) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall,
            color = if (isToday) Neutral900 else Neutral600,
        )
        Box(
            modifier = Modifier
                .size(44.dp)
                .alpha(if (isPast) 0.55f else 1f)
                .clip(CircleShape)
                .background(if (isToday) Harvest300 else Neutral0),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.sp),
                color = Neutral900,
            )
        }
    }
}
