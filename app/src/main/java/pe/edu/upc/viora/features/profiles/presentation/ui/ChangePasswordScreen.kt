package pe.edu.upc.viora.features.profiles.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
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
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral500
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.features.profiles.presentation.viewmodel.ChangePasswordViewModel

/**
 * P17 · Cambiar contraseña (US04).
 * Validates current password and enforces new password requirements:
 * 8+ chars, alphanumeric, different from current password.
 */
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChangePasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onSuccess()
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
            title = stringResource(R.string.security_title),
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
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // Headline "Nueva\n*contraseña.*"
            AccountEditorialTitle(
                lead = stringResource(R.string.security_headline_lead),
                emphasis = stringResource(R.string.security_headline_emphasis),
            )

            Spacer(Modifier.height(4.dp))

            // 1. Current password
            ValidatedFieldCard(
                label = stringResource(R.string.security_current_password_label),
                value = state.currentPassword,
                onValueChange = viewModel::onCurrentPasswordChange,
                isSecret = true,
                errorText = if (state.currentPasswordIncorrect) {
                    stringResource(R.string.security_error_current_wrong)
                } else null,
                imeAction = ImeAction.Next,
            )

            // 2. New password
            ValidatedFieldCard(
                label = stringResource(R.string.security_new_password_label),
                value = state.newPassword,
                onValueChange = viewModel::onNewPasswordChange,
                isSecret = true,
                errorText = if (state.showNewPasswordError) {
                    stringResource(R.string.security_error_not_different_or_numbers)
                } else null,
                imeAction = ImeAction.Next,
            )

            // Password strength bar (4 segments)
            PasswordStrengthBar(
                lengthMet = state.ruleLengthMet,
                alphanumericMet = state.ruleAlphanumericMet,
                differentMet = state.ruleDifferentMet,
            )

            // Password requirements checklist
            PasswordChecklist(
                lengthMet = state.ruleLengthMet,
                alphanumericMet = state.ruleAlphanumericMet,
                differentMet = state.ruleDifferentMet,
                touched = state.newPasswordTouched,
            )

            // 3. Confirm new password
            ValidatedFieldCard(
                label = stringResource(R.string.security_confirm_password_label),
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                isSecret = true,
                imeAction = ImeAction.Done,
                onImeAction = viewModel::submit,
            )
        }

        // Pinned Bottom Button: "Cambiar contraseña"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            CheckmarkPillButton(
                text = stringResource(R.string.security_action_change),
                onClick = viewModel::submit,
                enabled = state.canSubmit,
                loading = state.isLoading,
            )
        }
    }
}

@Composable
private fun PasswordStrengthBar(
    lengthMet: Boolean,
    alphanumericMet: Boolean,
    differentMet: Boolean,
    modifier: Modifier = Modifier,
) {
    var metCount = 0
    if (lengthMet) metCount++
    if (alphanumericMet) metCount++
    if (differentMet) metCount++
    val totalSegments = 4
    val activeSegments = if (metCount == 3) 4 else metCount

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(totalSegments) { index ->
            val active = index < activeSegments
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (active) Green800 else Neutral200),
            )
        }
    }
}

@Composable
private fun PasswordChecklist(
    lengthMet: Boolean,
    alphanumericMet: Boolean,
    differentMet: Boolean,
    touched: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PasswordRuleItem(
            text = stringResource(R.string.security_rule_length),
            isMet = lengthMet,
            touched = touched,
        )
        PasswordRuleItem(
            text = stringResource(R.string.security_rule_alphanumeric),
            isMet = alphanumericMet,
            touched = touched,
        )
        PasswordRuleItem(
            text = stringResource(R.string.security_rule_different),
            isMet = differentMet,
            touched = touched,
        )
    }
}

@Composable
private fun PasswordRuleItem(
    text: String,
    isMet: Boolean,
    touched: Boolean,
    modifier: Modifier = Modifier,
) {
    val isError = touched && !isMet
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(if (isError) R.drawable.ic_close else R.drawable.ic_check),
            contentDescription = null,
            tint = when {
                isMet -> Green800
                isError -> Terracotta500
                else -> Neutral500
            },
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = when {
                isMet -> Green900
                isError -> Terracotta500
                else -> Neutral600
            },
        )
    }
}
