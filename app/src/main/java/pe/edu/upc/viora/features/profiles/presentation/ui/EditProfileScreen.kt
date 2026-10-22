package pe.edu.upc.viora.features.profiles.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.features.profiles.presentation.viewmodel.EditProfileViewModel

/**
 * P16 · Datos personales (US03 / US43).
 * Edit full name and mobile number in E.164 format for the user's country.
 * Real-time validation checks for non-empty name and valid 9-digit Peruvian mobile starting with 9.
 */
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onSaved()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral50)
            .systemBarsPadding()
            .imePadding(),
    ) {
        // Top bar with close (X) button
        AccountTopBar(
            title = stringResource(R.string.profile_edit_title),
            subtitle = stringResource(R.string.account_title),
            onBack = onBack,
            isClose = true,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // Headline "Tus\n*datos.*"
            AccountEditorialTitle(
                lead = stringResource(R.string.profile_edit_headline_lead),
                emphasis = stringResource(R.string.profile_edit_headline_emphasis),
            )

            Spacer(Modifier.height(4.dp))

            // 1. Full name field
            ValidatedFieldCard(
                label = stringResource(R.string.profile_fullname_label),
                value = state.fullName,
                onValueChange = viewModel::onFullNameChange,
                placeholder = stringResource(R.string.profile_fullname_placeholder),
                errorText = if (state.showNameError) stringResource(R.string.profile_name_required_error) else null,
                imeAction = ImeAction.Next,
            )

            // 2. Mobile field (E.164)
            ValidatedFieldCard(
                label = stringResource(R.string.profile_phone_label),
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                helperText = stringResource(R.string.profile_phone_helper),
                errorText = if (state.showPhoneError) stringResource(R.string.profile_phone_invalid_error) else null,
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done,
                onImeAction = viewModel::saveChanges,
            )

            // 3. Email field (read-only)
            ValidatedFieldCard(
                label = stringResource(R.string.account_email_label),
                value = state.email,
                onValueChange = {},
                enabled = false,
                helperText = stringResource(R.string.profile_email_helper),
            )
        }

        // Pinned Bottom Button: "Guardar cambios"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            CheckmarkPillButton(
                text = stringResource(R.string.profile_save_changes),
                onClick = viewModel::saveChanges,
                enabled = state.canSave,
                loading = state.isLoading,
            )
        }
    }
}
