package pe.edu.upc.viora.features.iam.presentation.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.VioraVoice
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.features.iam.presentation.state.CodeError
import pe.edu.upc.viora.features.iam.presentation.state.IamError
import pe.edu.upc.viora.features.iam.presentation.state.IamStep
import pe.edu.upc.viora.features.iam.presentation.state.IamUiState
import pe.edu.upc.viora.features.iam.presentation.viewmodel.IamViewModel

/**
 * Entry point of the IAM flow (Figma frames 03 and 05). The route decides the first screen (sign in
 * or create account) and the recovery link `https://<domain>/password-resets/<token>` opens it
 * straight at the new-password check. [onExit] runs on back from the first screen and
 * [onStartOnboarding] on "¿Aún no tienes cuenta? Empecemos". [onAuthenticated] gets the cooperative
 * code saved in the onboarding when the producer just verified the email with one, so it can be redeemed;
 * without a code [onSignedUpForPlan] gets the hectares of T02d instead, to pay the Plan Productor.
 */
@Composable
fun IamScreen(
    onAuthenticated: (cooperativeCode: String?) -> Unit,
    onExit: () -> Unit,
    onStartOnboarding: () -> Unit,
    onSignedUpForPlan: (hectares: Int?) -> Unit = { onAuthenticated(null) },
    viewModel: IamViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val onBack = { if (!viewModel.back()) onExit() }

    LaunchedEffect(state.authenticated) {
        if (!state.authenticated) return@LaunchedEffect
        if (state.buysPlan) onSignedUpForPlan(state.planHectares) else onAuthenticated(state.cooperativeCodeToRedeem)
    }
    BackHandler(onBack = onBack)

    when (state.step) {
        IamStep.SIGN_IN -> SignInStep(state, viewModel, onBack, onStartOnboarding)
        IamStep.CREATE_ACCOUNT -> CreateAccountStep(state, viewModel, onBack)
        IamStep.VERIFY -> VerifyStep(state, viewModel, onBack)
        IamStep.FORGOT -> ForgotStep(state, viewModel, onBack)
        IamStep.LINK_SENT -> LinkSentStep(state, viewModel, onBack)
        IamStep.CHECKING_LINK -> CheckingLinkStep(state, viewModel)
        IamStep.NEW_PASSWORD -> NewPasswordStep(state, viewModel, onBack)
        IamStep.RESET_DONE -> ResetDoneStep(viewModel)
        IamStep.LINK_EXPIRED -> LinkExpiredStep(viewModel, onBack)
    }
}

// ---- Create account (T03) ------------------------------------------------------------------

@Composable
private fun CreateAccountStep(state: IamUiState, vm: IamViewModel, onBack: () -> Unit) {
    val typed = state.password.isNotEmpty()
    val passwordError = if (state.passwordWeak || (typed && !state.passwordOk)) stringResource(R.string.iam_password_weak) else null
    val emailTakenContent: (@Composable () -> Unit)? =
        if (state.emailTaken) { { EmailTakenMessage(onSignIn = vm::goToSignInWithEmail) } } else null
    IamFrame(
        onBack = onBack,
        footer = {
            PillButton(
                text = stringResource(R.string.iam_create_action),
                onClick = vm::signUp,
                enabled = state.canCreateAccount,
                loading = state.loading,
            )
            Text(
                text = stringResource(R.string.iam_create_footnote),
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
                textAlign = TextAlign.Center,
            )
        },
    ) {
        IamTitle(stringResource(R.string.iam_create_lead) + "\n*" + stringResource(R.string.iam_create_emphasis) + "*")
        VioraVoice(stringResource(R.string.iam_create_voice, state.firstName))
        UnderlineField(
            label = stringResource(R.string.iam_field_email),
            value = state.email,
            onValueChange = vm::onEmail,
            isError = state.emailTaken,
            keyboardType = KeyboardType.Email,
            errorContent = emailTakenContent,
        )
        UnderlineField(
            label = stringResource(R.string.iam_field_password),
            value = state.password,
            onValueChange = vm::onPassword,
            secret = true,
            imeAction = ImeAction.Done,
            onImeAction = vm::signUp,
            error = passwordError,
        )
        RuleRow(
            listOf(
                Rule(stringResource(R.string.iam_rule_length), state.passwordLongEnough, touched = typed),
                Rule(stringResource(R.string.iam_rule_letters_digits), state.passwordHasLettersAndDigits, touched = typed),
            ),
        )
        ErrorLine(state.error)
    }
}

/** "Este correo ya tiene una cuenta. *Inicia sesión* o usa otro correo." with a real link (US01 esc. 2). */
@Composable
private fun EmailTakenMessage(onSignIn: () -> Unit) {
    val message = stringResource(R.string.iam_email_taken)
    val link = stringResource(R.string.iam_email_taken_link)
    val start = message.indexOf(link)
    val styled = buildAnnotatedString {
        if (start < 0) {
            append(message)
        } else {
            append(message.substring(0, start))
            withLink(
                LinkAnnotation.Clickable(
                    tag = "sign-in",
                    styles = TextLinkStyles(SpanStyle(textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Medium)),
                    linkInteractionListener = { onSignIn() },
                ),
            ) { append(link) }
            append(message.substring(start + link.length))
        }
    }
    Text(styled, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
}

// ---- Verify email (T04a) -------------------------------------------------------------------

@Composable
private fun VerifyStep(state: IamUiState, vm: IamViewModel, onBack: () -> Unit) {
    val codeError = state.codeError
    IamFrame(onBack = onBack) {
        IamTitle(stringResource(R.string.iam_verify_lead) + "\n*" + stringResource(R.string.iam_verify_emphasis) + "*")
        VioraVoice(stringResource(R.string.iam_verify_voice, state.pendingEmail))
        Spacer(Modifier.height(4.dp))
        OtpInput(
            code = state.code,
            onCodeChange = vm::onCode,
            isError = codeError != null,
            enabled = !state.loading,
        )
        if (codeError != null) {
            Text(codeErrorText(codeError), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
        if (state.notice == IamUiState.Notice.CODE_RESENT && codeError == null) {
            Text(stringResource(R.string.iam_code_resent), style = MaterialTheme.typography.bodySmall, color = Green800)
        }
        ErrorLine(state.error)
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            if (state.resendSeconds > 0) {
                Text(
                    text = stringResource(R.string.iam_resend_in, formatCountdown(state.resendSeconds)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Neutral600,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                UnderlinedLink(stringResource(R.string.iam_resend), onClick = vm::resend)
            }
            UnderlinedLink(stringResource(R.string.iam_change_email), onClick = { vm.goTo(IamStep.CREATE_ACCOUNT) })
        }
        if (state.loading) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.5.dp, color = Neutral900)
    }
}

@Composable
private fun codeErrorText(error: CodeError): String = when (error) {
    is CodeError.Wrong -> error.remainingAttempts
        ?.let { pluralStringResource(R.plurals.iam_code_wrong, it, it) }
        ?: stringResource(R.string.iam_code_wrong_plain)
    CodeError.Blocked -> stringResource(R.string.iam_code_blocked)
    CodeError.Expired -> stringResource(R.string.iam_code_expired)
    CodeError.Missing -> stringResource(R.string.iam_code_missing)
}

// ---- Sign in (T03 sign-in / US02) ----------------------------------------------------------

@Composable
private fun SignInStep(state: IamUiState, vm: IamViewModel, onBack: () -> Unit, onStartOnboarding: () -> Unit) {
    IamFrame(
        onBack = onBack,
        footer = {
            PillButton(
                text = stringResource(R.string.iam_sign_in_action),
                onClick = vm::signIn,
                enabled = state.canSignIn,
                loading = state.loading,
            )
            Row(
                modifier = Modifier.clickable(role = Role.Button, onClick = onStartOnboarding).padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.iam_no_account) + " ", style = MaterialTheme.typography.bodyMedium, color = Neutral600)
                Text(stringResource(R.string.iam_no_account_link), style = MaterialTheme.typography.titleSmall, color = Neutral900)
            }
        },
    ) {
        IamTitle(stringResource(R.string.iam_sign_in_lead) + "\n*" + stringResource(R.string.iam_sign_in_emphasis) + "*")
        VioraVoice(stringResource(R.string.iam_sign_in_voice))
        UnderlineField(
            label = stringResource(R.string.iam_field_email),
            value = state.email,
            onValueChange = vm::onEmail,
            isError = state.credentialsError,
            keyboardType = KeyboardType.Email,
        )
        // The message is generic on purpose: it never reveals which of the two data failed (US02 esc. 3).
        UnderlineField(
            label = stringResource(R.string.iam_field_password),
            value = state.password,
            onValueChange = vm::onPassword,
            secret = true,
            imeAction = ImeAction.Done,
            onImeAction = vm::signIn,
            error = if (state.credentialsError) stringResource(R.string.iam_invalid_credentials) else null,
        )
        UnderlinedLink(stringResource(R.string.iam_forgot_link), onClick = { vm.goTo(IamStep.FORGOT) })
        ErrorLine(state.error)
    }
}

// ---- Password recovery (T06-T09) -----------------------------------------------------------

@Composable
private fun ForgotStep(state: IamUiState, vm: IamViewModel, onBack: () -> Unit) {
    IamFrame(
        onBack = onBack,
        footer = {
            PillButton(
                text = stringResource(R.string.iam_send_link),
                onClick = { vm.requestResetLink() },
                enabled = state.emailValid,
                loading = state.loading,
            )
        },
    ) {
        IamTitle(stringResource(R.string.iam_forgot_lead) + "\n*" + stringResource(R.string.iam_forgot_emphasis) + "*")
        VioraVoice(stringResource(R.string.iam_forgot_voice))
        UnderlineField(
            label = stringResource(R.string.iam_field_email),
            value = state.email,
            onValueChange = vm::onEmail,
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done,
            onImeAction = { vm.requestResetLink() },
        )
        ErrorLine(state.error)
    }
}

@Composable
private fun LinkSentStep(state: IamUiState, vm: IamViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val openMail = {
        val intent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_EMAIL)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
        Unit
    }
    val email = state.email.trim()
    val body = stringResource(R.string.iam_link_sent_body, email)
    val emailStart = body.indexOf(email)
    val styledBody = buildAnnotatedString {
        if (emailStart < 0) {
            append(body)
        } else {
            append(body.substring(0, emailStart))
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Neutral900)) { append(email) }
            append(body.substring(emailStart + email.length))
        }
    }
    IamFrame(
        onBack = onBack,
        footer = {
            if (state.notice == IamUiState.Notice.LINK_RESENT) {
                Text(stringResource(R.string.iam_link_resent), style = MaterialTheme.typography.bodySmall, color = Green800)
            }
            ErrorLine(state.error)
            PillButton(text = stringResource(R.string.iam_open_mail), onClick = { openMail() })
            UnderlinedLink(
                text = stringResource(R.string.iam_resend_link),
                onClick = { vm.requestResetLink(isResend = true) },
                enabled = !state.loading,
            )
        },
    ) {
        Spacer(Modifier.height(8.dp))
        EnvelopeIllustration(Modifier.align(Alignment.CenterHorizontally))
        StatusLabel(stringResource(R.string.iam_link_sent_label), Modifier.align(Alignment.CenterHorizontally))
        CenteredHeadline(
            lead = stringResource(R.string.iam_verify_lead),
            emphasis = stringResource(R.string.iam_verify_emphasis),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Text(
            text = styledBody,
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral600,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        )
    }
}

@Composable
private fun CheckingLinkStep(state: IamUiState, vm: IamViewModel) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (state.error == null) {
                CircularProgressIndicator(color = Neutral900)
                Text(stringResource(R.string.iam_checking_link), style = MaterialTheme.typography.bodyMedium, color = Neutral600)
            } else {
                ErrorLine(state.error)
                PillButton(text = stringResource(R.string.iam_retry), onClick = vm::checkResetLink)
                UnderlinedLink(stringResource(R.string.iam_back_to_sign_in), onClick = { vm.goTo(IamStep.SIGN_IN) })
            }
        }
    }
}

@Composable
private fun NewPasswordStep(state: IamUiState, vm: IamViewModel, onBack: () -> Unit) {
    val typed = state.password.isNotEmpty()
    val passwordError = when {
        state.passwordReused -> stringResource(R.string.iam_password_reused)
        state.passwordWeak || (typed && !state.passwordOk) -> stringResource(R.string.iam_password_weak)
        else -> null
    }
    IamFrame(
        onBack = onBack,
        footer = {
            PillButton(
                text = stringResource(R.string.iam_save_password),
                onClick = vm::saveNewPassword,
                enabled = state.newPasswordOk,
                loading = state.loading,
            )
        },
    ) {
        IamTitle(stringResource(R.string.iam_new_password_lead) + "\n*" + stringResource(R.string.iam_new_password_emphasis) + "*")
        VioraVoice(stringResource(R.string.iam_new_password_voice))
        UnderlineField(
            label = stringResource(R.string.iam_field_new_password),
            value = state.password,
            onValueChange = vm::onPassword,
            secret = true,
            error = passwordError,
        )
        UnderlineField(
            label = stringResource(R.string.iam_field_confirm_password),
            value = state.confirmPassword,
            onValueChange = vm::onConfirmPassword,
            secret = true,
            imeAction = ImeAction.Done,
            onImeAction = vm::saveNewPassword,
        )
        RuleRow(
            listOf(
                Rule(stringResource(R.string.iam_rule_length), state.passwordLongEnough, touched = typed),
                Rule(stringResource(R.string.iam_rule_letters_digits), state.passwordHasLettersAndDigits, touched = typed),
                Rule(stringResource(R.string.iam_rule_match), state.confirmOk, touched = state.confirmPassword.isNotEmpty()),
            ),
        )
        ErrorLine(state.error)
    }
}

@Composable
private fun ResetDoneStep(vm: IamViewModel) {
    IamFrame(
        onBack = null,
        footer = { PillButton(text = stringResource(R.string.iam_sign_in_action), onClick = { vm.goTo(IamStep.SIGN_IN) }) },
    ) {
        Spacer(Modifier.height(24.dp))
        IsotypeBadge(Modifier.align(Alignment.CenterHorizontally))
        StatusLabel(stringResource(R.string.iam_done_label), Modifier.align(Alignment.CenterHorizontally))
        CenteredHeadline(
            lead = stringResource(R.string.iam_done_lead),
            emphasis = stringResource(R.string.iam_done_emphasis),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Text(
            text = stringResource(R.string.iam_done_body),
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral600,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        )
    }
}

@Composable
private fun LinkExpiredStep(vm: IamViewModel, onBack: () -> Unit) {
    IamFrame(
        onBack = onBack,
        footer = {
            PillButton(text = stringResource(R.string.iam_request_new_link), onClick = { vm.goTo(IamStep.FORGOT) })
            UnderlinedLink(stringResource(R.string.iam_back_to_sign_in), onClick = { vm.goTo(IamStep.SIGN_IN) })
        },
    ) {
        Spacer(Modifier.height(8.dp))
        EnvelopeIllustration(Modifier.align(Alignment.CenterHorizontally))
        StatusLabel(stringResource(R.string.iam_expired_label), Modifier.align(Alignment.CenterHorizontally))
        CenteredHeadline(
            lead = stringResource(R.string.iam_expired_lead),
            emphasis = stringResource(R.string.iam_expired_emphasis),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Text(
            text = stringResource(R.string.iam_expired_body),
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral600,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        )
    }
}

// ---- Helpers -------------------------------------------------------------------------------

@Composable
private fun ErrorLine(error: IamError?) {
    if (error == null) return
    val text = when (error) {
        IamError.OFFLINE -> stringResource(R.string.iam_error_offline)
        IamError.WAIT -> stringResource(R.string.iam_error_wait)
        IamError.GENERIC -> stringResource(R.string.iam_error_generic)
        IamError.PHONE_REJECTED -> stringResource(R.string.iam_error_phone_rejected)
    }
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
}

private fun formatCountdown(totalSeconds: Int): String = "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
