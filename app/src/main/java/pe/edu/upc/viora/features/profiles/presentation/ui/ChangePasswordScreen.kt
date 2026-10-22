package pe.edu.upc.viora.features.profiles.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.EditorialHeadline
import pe.edu.upc.viora.core.designsystem.theme.Green700
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral500
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.profiles.presentation.state.ChangePasswordError
import pe.edu.upc.viora.features.profiles.presentation.state.ChangePasswordUiState
import pe.edu.upc.viora.features.profiles.presentation.viewmodel.ChangePasswordViewModel

/**
 * P97 · Cambiar contraseña (US04): checks the current password and asks for a new one of 8 or
 * more characters, with letters and numbers, different from the current one.
 */
@Composable
fun ChangePasswordScreen(
    onClose: () -> Unit,
    onChanged: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChangePasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isChanged) { if (state.isChanged) onChanged() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral100)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        AccountTopBar(
            title = stringResource(R.string.security_title),
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
                lead = stringResource(R.string.security_headline_lead),
                emphasis = stringResource(R.string.security_headline_emphasis),
            )
            Spacer(Modifier.height(4.dp))

            ValidatedFieldCard(
                label = stringResource(R.string.security_current_password_label),
                value = state.currentPassword,
                onValueChange = viewModel::onCurrentPasswordChange,
                isSecret = true,
                errorText = if (state.currentPasswordIncorrect) stringResource(R.string.security_error_current_wrong) else null,
            )
            ValidatedFieldCard(
                label = stringResource(R.string.security_new_password_label),
                value = state.newPassword,
                onValueChange = viewModel::onNewPasswordChange,
                isSecret = true,
                errorText = newPasswordError(state),
            )
            PasswordRequirements(state)
            ValidatedFieldCard(
                label = stringResource(R.string.security_confirm_password_label),
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                isSecret = true,
                errorText = if (state.confirmMismatch) stringResource(R.string.security_error_confirm) else null,
                imeAction = ImeAction.Done,
                onImeAction = viewModel::submit,
            )
        }

        Column(modifier = Modifier.padding(horizontal = AccountGutter, vertical = 16.dp)) {
            FieldNote(
                text = when (state.error) {
                    ChangePasswordError.OFFLINE -> stringResource(R.string.security_error_offline)
                    ChangePasswordError.GENERIC -> stringResource(R.string.security_error_generic)
                    null -> null
                },
                isError = true,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            CheckmarkPillButton(
                text = stringResource(R.string.security_action_change),
                onClick = viewModel::submit,
                enabled = state.canSubmit,
                loading = state.isLoading,
            )
        }
    }
}

/** The reason under the new password: the server's, else the broken rules (P97 · No cumple). */
@Composable
private fun newPasswordError(state: ChangePasswordUiState): String? {
    state.newPasswordRejection?.let { return it }
    if (!state.showRuleErrors || state.newPasswordValid) return null
    return when {
        !state.ruleDifferentMet && !state.ruleAlphanumericMet -> stringResource(R.string.security_error_same_and_no_numbers)
        !state.ruleDifferentMet -> stringResource(R.string.security_error_same)
        !state.ruleLengthMet -> stringResource(R.string.security_error_length)
        else -> stringResource(R.string.security_error_alphanumeric)
    }
}

/** The white card with the four-segment strength bar and the three rules. */
@Composable
private fun PasswordRequirements(state: ChangePasswordUiState) {
    val failing = state.showRuleErrors && !state.newPasswordValid
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Neutral0)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(4) { index ->
                val color = when {
                    index >= state.strength -> Neutral100
                    failing -> Terracotta500
                    else -> Green800
                }
                Spacer(Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)).background(color))
            }
        }
        RuleLine(stringResource(R.string.security_rule_length), met = state.ruleLengthMet, failing = failing)
        RuleLine(stringResource(R.string.security_rule_alphanumeric), met = state.ruleAlphanumericMet, failing = failing)
        RuleLine(stringResource(R.string.security_rule_different), met = state.ruleDifferentMet, failing = failing)
    }
}

/** A met rule shows the check; a broken one turns into a terracotta cross once the rules are judged. */
@Composable
private fun RuleLine(text: String, met: Boolean, failing: Boolean) {
    val broken = failing && !met
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(if (broken) R.drawable.ic_close else R.drawable.ic_task_alt),
            contentDescription = null,
            tint = when {
                broken -> Terracotta500
                met -> Green700
                else -> Neutral500
            },
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = if (broken) Terracotta700 else Neutral900,
        )
    }
}
