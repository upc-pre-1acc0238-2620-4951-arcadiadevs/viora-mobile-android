package pe.edu.upc.viora.features.profiles.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.CircleIconButton
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest400
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.locale.AppLanguage
import pe.edu.upc.viora.core.presentation.vioraNumberFormat

/**
 * P98 · Idioma (US42): Spanish of Peru or English, each with a sample of its numbers and dates.
 * The button names the language it switches to; choosing the current one just closes the sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageBottomSheet(
    current: AppLanguage,
    onConfirm: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember { mutableStateOf(current) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Neutral100,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Neutral300),
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = AccountGutter).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val titleStyle = MaterialTheme.typography.displaySmall.copy(fontFamily = NewsreaderFamily, fontSize = 34.sp, lineHeight = 38.sp)
                    Text(text = stringResource(R.string.language_sheet_title_lead), style = titleStyle, color = Neutral900)
                    Text(
                        text = stringResource(R.string.language_sheet_title_emphasis),
                        style = titleStyle,
                        fontStyle = FontStyle.Italic,
                        color = Neutral900,
                    )
                    Text(
                        text = stringResource(R.string.language_sheet_subtitle),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = Neutral600,
                    )
                }
                CircleIconButton(
                    icon = R.drawable.ic_close,
                    contentDescription = stringResource(R.string.action_close),
                    onClick = onDismiss,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Neutral0)
                    .padding(horizontal = 20.dp, vertical = 4.dp),
            ) {
                LanguageOption(
                    title = stringResource(R.string.language_es_title),
                    sample = formatSample(AppLanguage.SPANISH),
                    selected = selected == AppLanguage.SPANISH,
                    onClick = { selected = AppLanguage.SPANISH },
                )
                Spacer(Modifier.fillMaxWidth().height(1.dp).background(Neutral200))
                LanguageOption(
                    title = stringResource(R.string.language_en_title),
                    sample = formatSample(AppLanguage.ENGLISH),
                    selected = selected == AppLanguage.ENGLISH,
                    onClick = { selected = AppLanguage.ENGLISH },
                )
            }

            Text(
                text = stringResource(R.string.language_sheet_note),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 14.sp),
                color = Neutral600,
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            CheckmarkPillButton(
                text = stringResource(
                    if (selected == AppLanguage.ENGLISH) R.string.language_switch_to_en else R.string.language_switch_to_es,
                ),
                onClick = { onConfirm(selected) },
                checkColor = Harvest400,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
    }
}

@Composable
private fun LanguageOption(title: String, sample: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, fontWeight = FontWeight.Medium),
                color = Neutral900,
            )
            Text(text = sample, style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp), color = Neutral600)
        }
        if (selected) {
            Box(
                modifier = Modifier.size(24.dp).clip(CircleShape).background(Green900),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(Neutral0))
            }
        } else {
            Box(Modifier.size(24.dp).border(2.dp, Neutral200, CircleShape))
        }
    }
}

/** "12 345,6 kg · 14/05/2026" or "12,345.6 kg · 05/14/2026": how that language writes a weight and a date. */
private fun formatSample(language: AppLanguage): String {
    val locale = Locale.forLanguageTag(language.tag)
    val number = vioraNumberFormat(minFractionDigits = 1, locale = locale).format(12_345.6)
    val pattern = if (language == AppLanguage.SPANISH) "dd/MM/yyyy" else "MM/dd/yyyy"
    return "$number kg · ${LocalDate.of(2026, 5, 14).format(DateTimeFormatter.ofPattern(pattern, locale))}"
}
