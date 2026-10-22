package pe.edu.upc.viora.features.profiles.presentation.ui

import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import java.util.Locale
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageBottomSheet(
    currentLanguage: String,
    onSelectLanguage: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedLang by remember { mutableStateOf(currentLanguage) }
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Neutral50,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header with title and close icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.language_sheet_title),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = NewsreaderFamily,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = Neutral900,
                    )
                    Text(
                        text = stringResource(R.string.language_sheet_subtitle),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = Neutral600,
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.action_close),
                        tint = Neutral900,
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Option 1: Spanish
            LanguageOptionRow(
                title = stringResource(R.string.language_es_title),
                example = stringResource(R.string.language_es_example),
                isSelected = selectedLang == "es",
                onClick = { selectedLang = "es" },
            )

            // Option 2: English
            LanguageOptionRow(
                title = stringResource(R.string.language_en_title),
                example = stringResource(R.string.language_en_example),
                isSelected = selectedLang == "en",
                onClick = { selectedLang = "en" },
            )

            Spacer(Modifier.height(4.dp))

            // Explanation note
            Text(
                text = stringResource(R.string.language_sheet_note),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                color = Neutral600,
            )

            Spacer(Modifier.height(8.dp))

            // Action button
            val buttonText = if (selectedLang == "en") {
                stringResource(R.string.language_switch_to_en)
            } else {
                stringResource(R.string.language_switch_to_es)
            }

            CheckmarkPillButton(
                text = buttonText,
                onClick = {
                    // Switch locale en caliente (US42)
                    applyAppLocale(context, selectedLang)
                    onSelectLanguage(selectedLang)
                },
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LanguageOptionRow(
    title: String,
    example: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Neutral0)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                color = Neutral900,
            )
            Text(
                text = example,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = Neutral600,
            )
        }
        // Radio button circle
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(2.dp, if (isSelected) Green900 else Neutral200, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Green900),
                )
            }
        }
    }
}

/** Switches application locale in-process without losing session (US42). */
private fun applyAppLocale(context: android.content.Context, langCode: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context.getSystemService(LocaleManager::class.java).applicationLocales =
            LocaleList.forLanguageTags(langCode)
    } else {
        val locale = Locale.forLanguageTag(langCode)
        Locale.setDefault(locale)
        val resources = context.resources
        val config = resources.configuration
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
