package pe.edu.upc.viora.features.profiles.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral500
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.features.profiles.presentation.viewmodel.AccountViewModel

/**
 * P15 · Mi cuenta (US03 - US04 - US42).
 * Overview of personal data, security, preferences and session.
 * Each row navigates to its corresponding edit screen.
 */
@Composable
fun AccountScreen(
    onBack: () -> Unit,
    onEditPersonal: () -> Unit,
    onChangePassword: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSignedOut) {
        if (state.isSignedOut) {
            onSignOut()
        }
    }

    val profile = state.profile
    val roleSubtitle = when {
        profile?.role != null && profile.organization != null ->
            "${profile.role} · ${profile.organization}"
        profile?.role != null -> profile.role
        else -> stringResource(R.string.account_role_manager_org)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral50)
            .systemBarsPadding(),
    ) {
        // Top bar with back arrow
        AccountTopBar(
            title = stringResource(R.string.account_title),
            subtitle = roleSubtitle,
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // Headline "Tu\n*cuenta.*"
            AccountEditorialTitle(
                lead = stringResource(R.string.account_headline_lead),
                emphasis = stringResource(R.string.account_headline_emphasis),
            )

            // Profile Hero Card (warm pale salmon card with initials and badge)
            if (profile != null) {
                ProfileHeroCard(profile = profile)
            }

            // Section 1: DATOS PERSONALES
            AccountSection(title = stringResource(R.string.account_section_personal)) {
                AccountRow(
                    label = stringResource(R.string.account_name_label),
                    value = profile?.fullName.orEmpty(),
                    onClick = onEditPersonal,
                )
                AccountDivider()
                AccountRow(
                    label = stringResource(R.string.account_phone_label),
                    value = profile?.formattedPhoneNumber.orEmpty(),
                    onClick = onEditPersonal,
                )
                AccountDivider()
                AccountRow(
                    label = stringResource(R.string.account_email_label),
                    value = stringResource(R.string.account_email_verified),
                    onClick = onEditPersonal,
                )
            }

            // Section 2: SEGURIDAD
            AccountSection(title = stringResource(R.string.account_section_security)) {
                AccountRow(
                    label = stringResource(R.string.account_change_password),
                    onClick = onChangePassword,
                )
            }

            // Section 3: PREFERENCIAS
            AccountSection(title = stringResource(R.string.account_section_preferences)) {
                val languageDisplay = if (state.currentLanguage == "en") {
                    stringResource(R.string.account_language_en)
                } else {
                    stringResource(R.string.account_language_es)
                }
                AccountRow(
                    label = stringResource(R.string.account_language_label),
                    value = languageDisplay,
                    onClick = viewModel::openLanguageSheet,
                )
                AccountDivider()
                AccountRow(
                    label = stringResource(R.string.account_notifications_label),
                    value = stringResource(R.string.account_notifications_enabled),
                )
            }

            // Section 4: SESIÓN
            AccountSection(title = stringResource(R.string.account_section_session)) {
                AccountRow(
                    label = stringResource(R.string.account_sign_out),
                    isDestructive = true,
                    onClick = viewModel::openSignOutDialog,
                )
            }

            Spacer(Modifier.height(8.dp))

            // App version footer
            Text(
                text = stringResource(R.string.account_version_footer),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Neutral500,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            )
        }
    }

    // Modal Bottom Sheet: Language selector (US42)
    if (state.showLanguageSheet) {
        LanguageBottomSheet(
            currentLanguage = state.currentLanguage,
            onSelectLanguage = viewModel::selectLanguage,
            onDismiss = viewModel::closeLanguageSheet,
        )
    }

    // Confirmation dialog: Sign out
    if (state.showSignOutDialog) {
        AlertDialog(
            onDismissRequest = viewModel::closeSignOutDialog,
            title = {
                Text(
                    text = stringResource(R.string.account_sign_out_confirm_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = Neutral900,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.account_sign_out_confirm_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral500,
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmSignOut) {
                    Text(
                        text = stringResource(R.string.account_sign_out_confirm_action),
                        color = Terracotta500,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::closeSignOutDialog) {
                    Text(
                        text = stringResource(R.string.account_cancel),
                        color = Neutral900,
                    )
                }
            },
            containerColor = Neutral50,
        )
    }
}
