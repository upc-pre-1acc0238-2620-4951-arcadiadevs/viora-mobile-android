package pe.edu.upc.viora.features.profiles.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import pe.edu.upc.viora.core.designsystem.component.EditorialHeadline
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.features.profiles.presentation.state.SaveProfileError
import pe.edu.upc.viora.features.profiles.presentation.viewmodel.EditProfileViewModel

/**
 * P96 · Datos personales (US03): full name and mobile in E.164 for the profile's country; the
 * email is read-only. Saving goes back to Mi cuenta, which shows the new values.
 */
@Composable
fun EditProfileScreen(
    onClose: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) { if (state.isSaved) onSaved() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral100)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        AccountTopBar(
            title = stringResource(R.string.profile_edit_title),
            subtitle = stringResource(R.string.account_title),
            onBack = onClose,
            isClose = true,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AccountGutter),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            EditorialHeadline(
                lead = stringResource(R.string.profile_edit_headline_lead),
                emphasis = stringResource(R.string.profile_edit_headline_emphasis),
            )
            Spacer(Modifier.height(4.dp))

            ValidatedFieldCard(
                label = stringResource(R.string.profile_fullname_label),
                value = state.fullName,
                onValueChange = viewModel::onFullNameChange,
                placeholder = stringResource(R.string.profile_fullname_placeholder),
                errorText = if (state.showNameError) stringResource(R.string.profile_name_required_error) else null,
            )
            ValidatedFieldCard(
                label = stringResource(R.string.profile_phone_label, stringResource(state.country.nameRes), state.country.dialCode),
                value = state.phone,
                onValueChange = viewModel::onPhoneChange,
                helperText = stringResource(R.string.profile_phone_helper, state.internationalPhone),
                errorText = if (state.showPhoneError) stringResource(state.country.invalidHintRes) else null,
                visualTransformation = DigitGroupsTransformation,
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done,
                onImeAction = viewModel::saveChanges,
            )
            ValidatedFieldCard(
                label = stringResource(R.string.account_email_label),
                value = state.email,
                onValueChange = {},
                readOnly = true,
                helperText = stringResource(R.string.profile_email_helper),
            )
        }

        Column(modifier = Modifier.padding(horizontal = AccountGutter, vertical = 16.dp)) {
            FieldNote(
                text = when (state.error) {
                    SaveProfileError.OFFLINE -> stringResource(R.string.profile_error_offline)
                    SaveProfileError.GENERIC -> stringResource(R.string.profile_error_generic)
                    null -> null
                },
                isError = true,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            CheckmarkPillButton(
                text = stringResource(R.string.profile_save_changes),
                onClick = viewModel::saveChanges,
                enabled = state.canSave,
                loading = state.isLoading,
            )
        }
    }
}
