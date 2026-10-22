package pe.edu.upc.viora.features.profiles.presentation.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import pe.edu.upc.viora.BuildConfig
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.EditorialHeadline
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral500
import pe.edu.upc.viora.core.locale.AppLanguage
import pe.edu.upc.viora.features.profiles.presentation.viewmodel.AccountViewModel

private const val LANGUAGE_NOTICE_MILLIS = 2_500L

/**
 * P95 · Mi cuenta (US03 · US04 · US42): personal details, security, preferences and session. Each
 * row opens its edition, as the prototype wires it.
 */
@Composable
fun AccountScreen(
    onBack: () -> Unit,
    onEditPersonal: () -> Unit,
    onChangePassword: () -> Unit,
    onReplayHomeTour: () -> Unit,
    onSignedOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current
    val language = AppLanguage.current(context)

    // Notifications can be switched off in the phone's settings; read them again on every return.
    var notificationsOn by remember { mutableStateOf(true) }
    LifecycleResumeEffect(Unit) {
        notificationsOn = NotificationManagerCompat.from(context).areNotificationsEnabled()
        onPauseOrDispose { }
    }

    LaunchedEffect(state.isSignedOut) { if (state.isSignedOut) onSignedOut() }
    LaunchedEffect(state.languageNotice) {
        if (state.languageNotice != null) {
            delay(LANGUAGE_NOTICE_MILLIS)
            viewModel.dismissLanguageNotice()
        }
    }

    Column(modifier = modifier.fillMaxSize().background(Neutral100).statusBarsPadding()) {
        AccountTopBar(
            title = stringResource(R.string.account_title),
            subtitle = stringResource(R.string.account_role_producer),
            onBack = onBack,
            notice = if (state.languageNotice != null) stringResource(R.string.account_language_notice) else null,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AccountGutter)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            EditorialHeadline(
                lead = stringResource(R.string.account_headline_lead),
                emphasis = stringResource(R.string.account_headline_emphasis),
            )

            state.profile?.let { profile -> ProfileHeroCard(profile = profile, isMember = state.isMember == true) }

            AccountSection(title = stringResource(R.string.account_section_personal)) {
                AccountRow(
                    label = stringResource(R.string.account_name_label),
                    value = state.profile?.fullName,
                    showDivider = true,
                    onClick = onEditPersonal,
                )
                AccountRow(
                    label = stringResource(R.string.account_phone_label),
                    value = state.profile?.formattedPhoneNumber,
                    showDivider = true,
                    onClick = onEditPersonal,
                )
                // Only verified accounts sign in, so the email always reads "Verificado".
                AccountRow(
                    label = stringResource(R.string.account_email_label),
                    value = stringResource(R.string.account_email_verified),
                    onClick = onEditPersonal,
                )
            }

            AccountSection(title = stringResource(R.string.account_section_security)) {
                AccountRow(label = stringResource(R.string.account_change_password), onClick = onChangePassword)
            }

            AccountSection(title = stringResource(R.string.account_section_preferences)) {
                AccountRow(
                    label = stringResource(R.string.account_language_label),
                    value = stringResource(
                        if (language == AppLanguage.SPANISH) R.string.account_language_es else R.string.account_language_en,
                    ),
                    showDivider = true,
                    onClick = viewModel::openLanguageSheet,
                )
                AccountRow(
                    label = stringResource(R.string.account_notifications_label),
                    value = stringResource(if (notificationsOn) R.string.account_notifications_on else R.string.account_notifications_off),
                    showDivider = true,
                    onClick = { context.openNotificationSettings() },
                )
                AccountRow(
                    label = stringResource(R.string.account_home_tour_label),
                    value = stringResource(R.string.account_home_tour_replay),
                    onClick = { viewModel.replayHomeTour(onReplayHomeTour) },
                )
            }

            AccountSection(title = stringResource(R.string.account_section_session)) {
                AccountRow(
                    label = stringResource(R.string.account_sign_out),
                    isDestructive = true,
                    onClick = viewModel::signOut,
                )
            }

            Text(
                text = stringResource(R.string.account_version_footer, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Neutral500,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            )
        }
    }

    if (state.showLanguageSheet) {
        LanguageBottomSheet(
            current = language,
            onConfirm = { chosen ->
                if (chosen == language) {
                    viewModel.closeLanguageSheet()
                } else {
                    viewModel.onLanguageChosen(chosen)
                    activity?.let { AppLanguage.apply(it, chosen) }
                }
            },
            onDismiss = viewModel::closeLanguageSheet,
        )
    }
}

/** The app's page in the phone's notification settings, where they are switched on or off. */
private fun Context.openNotificationSettings() {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}
