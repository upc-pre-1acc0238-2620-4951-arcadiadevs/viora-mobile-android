package pe.edu.upc.viora.features.subscription.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.EditorialHeadline
import pe.edu.upc.viora.core.designsystem.component.MorphingShapeIndicator
import pe.edu.upc.viora.core.designsystem.component.VioraVoice
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta600
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.PrimaryPillButton
import pe.edu.upc.viora.features.subscription.domain.entity.CooperativeMembership
import pe.edu.upc.viora.features.subscription.presentation.state.RedeemCodeUiState
import pe.edu.upc.viora.features.subscription.presentation.state.RedeemFailure
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.RedeemCodeViewModel

/** P05 · Canjear código. Opens P06 through [onRedeemed] once the backend accepts the code. */
@Composable
fun RedeemCodeScreen(
    onBack: () -> Unit,
    onRedeemed: (CooperativeMembership) -> Unit,
    onPayPlan: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RedeemCodeViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    LaunchedEffect(state.redeemed) { state.redeemed?.let(onRedeemed) }
    RedeemCodeContent(
        state = state,
        onBack = onBack,
        onCodeChange = viewModel::setCode,
        onRedeem = viewModel::redeem,
        onTryAnother = viewModel::tryAnotherCode,
        onPayPlan = onPayPlan,
        modifier = modifier,
    )
}

@Composable
internal fun RedeemCodeContent(
    state: RedeemCodeUiState,
    onBack: () -> Unit,
    onCodeChange: (String) -> Unit,
    onRedeem: () -> Unit,
    onTryAnother: () -> Unit,
    onPayPlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isRedeeming) {
        ValidatingCodeContent(
            eyebrow = stringResource(R.string.redeem_code_validating_eyebrow, state.code),
            lead = stringResource(R.string.redeem_code_validating_title_lead),
            emphasis = stringResource(R.string.redeem_code_validating_title_emphasis),
            body = stringResource(R.string.redeem_code_validating_body),
            modifier = modifier,
        )
        return
    }
    val rejected = state.isCodeRejected
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral100)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp).size(44.dp)) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.action_back),
                tint = Neutral900,
            )
        }
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EditorialHeadline(
                lead = stringResource(if (rejected) R.string.redeem_code_rejected_title_lead else R.string.redeem_code_title_lead),
                emphasis = stringResource(
                    if (rejected) R.string.redeem_code_rejected_title_emphasis else R.string.redeem_code_title_emphasis,
                ),
            )
            VioraVoice(text = stringResource(if (rejected) R.string.redeem_code_rejected_voice else R.string.redeem_code_voice))
            Spacer(Modifier.height(4.dp))
            CodeField(
                code = state.code,
                label = stringResource(R.string.redeem_code_label),
                hint = stringResource(R.string.redeem_code_hint),
                error = state.failure?.let { stringResource(it.messageRes()) },
                enabled = !state.isRedeeming,
                onCodeChange = onCodeChange,
                onDone = onRedeem,
            )
        }
        Column(
            modifier = Modifier.padding(top = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (rejected) {
                PrimaryPillButton(text = stringResource(R.string.redeem_code_action_try_another), onClick = onTryAnother)
            } else {
                PrimaryPillButton(
                    text = stringResource(R.string.redeem_code_action_redeem),
                    onClick = onRedeem,
                    enabled = state.canRedeem || state.isRedeeming,
                    isLoading = state.isRedeeming,
                )
            }
            OutlinedPillButton(text = stringResource(R.string.redeem_code_action_pay_plan), onClick = onPayPlan)
        }
    }
}

/**
 * P05 · Validando and P103 · Validando: the code is with the backend; it ends in the membership (or the
 * applied extension) or in the rejected variant.
 */
@Composable
internal fun ValidatingCodeContent(eyebrow: String, lead: String, emphasis: String, body: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral100)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .padding(top = 76.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
            MorphingShapeIndicator(color = Green200, modifier = Modifier.fillMaxSize())
            Icon(
                painter = painterResource(R.drawable.ic_isotype),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(104.dp),
            )
        }
        Spacer(Modifier.height(32.dp))
        StatusHeading(eyebrow = eyebrow, lead = lead, emphasis = emphasis, body = body)
    }
}

/** Figma "Editorial/Campo": a label, the typed value on an underline, and a hint that turns into the error. */
@Composable
internal fun CodeField(
    code: String,
    label: String,
    hint: String,
    error: String?,
    enabled: Boolean,
    onCodeChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    val accent = if (error != null) Terracotta600 else Neutral900
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (error != null) Terracotta600 else Neutral600,
        )
        BasicTextField(
            value = code,
            onValueChange = onCodeChange,
            enabled = enabled,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, color = Neutral900),
            cursorBrush = SolidColor(Neutral900),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                keyboardType = KeyboardType.Ascii,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val stroke = 1.5.dp.toPx()
                    val y = size.height - stroke / 2
                    drawLine(accent, Offset(0f, y), Offset(size.width, y), stroke)
                }
                .padding(bottom = 10.dp),
        )
        Text(
            text = error ?: hint,
            style = MaterialTheme.typography.bodySmall,
            color = if (error != null) Terracotta600 else Neutral700,
        )
    }
}

/** The light pill under the main button (Figma "Botón", secondary). */
@Composable
internal fun OutlinedPillButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(CircleShape)
            .border(1.dp, Neutral300, CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = text, style = MaterialTheme.typography.titleMedium, color = Neutral900)
    }
}

private fun RedeemFailure.messageRes(): Int = when (this) {
    RedeemFailure.NotRedeemable -> R.string.redeem_code_error_not_redeemable
    RedeemFailure.AlreadySubscribed -> R.string.redeem_code_error_already_subscribed
    RedeemFailure.TooManyAttempts -> R.string.redeem_code_error_too_many_attempts
    is RedeemFailure.Other -> error.messageRes()
}
