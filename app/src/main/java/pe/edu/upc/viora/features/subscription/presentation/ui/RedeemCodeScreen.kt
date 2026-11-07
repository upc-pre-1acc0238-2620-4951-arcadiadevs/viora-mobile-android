package pe.edu.upc.viora.features.subscription.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import pe.edu.upc.viora.core.designsystem.component.OutlinedPillButton
import pe.edu.upc.viora.core.designsystem.component.PrimaryPillButton
import pe.edu.upc.viora.core.designsystem.component.VioraVoice
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta600
import pe.edu.upc.viora.core.presentation.LocalAppZone
import pe.edu.upc.viora.core.presentation.formatDayMonthYear
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.subscription.domain.entity.CodePreview
import pe.edu.upc.viora.features.subscription.domain.entity.CooperativeMembership
import pe.edu.upc.viora.features.subscription.presentation.state.RedeemCodeUiState
import pe.edu.upc.viora.features.subscription.presentation.state.RedeemFailure
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.RedeemCodeViewModel

/**
 * P05 · Canjear código and P05b · Aceptar y compartir. Opens P06 through [onRedeemed] once the producer accepted to
 * share their data and the backend redeemed the code; [onOpenMyPlan] is P05 · Ya tienes un plan "Volver a Mi plan".
 */
@Composable
fun RedeemCodeScreen(
    onBack: () -> Unit,
    onRedeemed: (CooperativeMembership) -> Unit,
    onPayPlan: () -> Unit,
    onOpenMyPlan: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RedeemCodeViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    LaunchedEffect(state.redeemed) { state.redeemed?.let(onRedeemed) }
    val preview = state.preview
    if (preview != null) {
        // P05b back arrow, system back and "Ahora no" all return to P05: nothing was redeemed.
        BackHandler(onBack = viewModel::tryAnotherCode)
        AcceptSharingContent(
            preview = preview,
            isAccepting = state.isAccepting,
            failure = state.failure?.let { stringResource(it.messageRes()) },
            onBack = viewModel::tryAnotherCode,
            onAccept = viewModel::accept,
            onNotNow = viewModel::tryAnotherCode,
            modifier = modifier,
        )
        return
    }
    RedeemCodeContent(
        state = state,
        onBack = onBack,
        onCodeChange = viewModel::setCode,
        onValidate = viewModel::validate,
        onTryAnother = viewModel::tryAnotherCode,
        onPayPlan = onPayPlan,
        onOpenMyPlan = onOpenMyPlan,
        modifier = modifier,
    )
}

@Composable
internal fun RedeemCodeContent(
    state: RedeemCodeUiState,
    onBack: () -> Unit,
    onCodeChange: (String) -> Unit,
    onValidate: () -> Unit,
    onTryAnother: () -> Unit,
    onPayPlan: () -> Unit,
    onOpenMyPlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isValidating) {
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
    val hasPlan = state.hasActivePlan
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral100)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp),
    ) {
        BackArrow(onBack)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EditorialHeadline(
                lead = stringResource(
                    when {
                        hasPlan -> R.string.redeem_code_has_plan_title_lead
                        rejected -> R.string.redeem_code_rejected_title_lead
                        else -> R.string.redeem_code_title_lead
                    },
                ),
                emphasis = stringResource(
                    when {
                        hasPlan -> R.string.redeem_code_has_plan_title_emphasis
                        rejected -> R.string.redeem_code_rejected_title_emphasis
                        else -> R.string.redeem_code_title_emphasis
                    },
                ),
            )
            VioraVoice(
                text = stringResource(
                    when {
                        hasPlan -> R.string.redeem_code_has_plan_voice
                        rejected -> R.string.redeem_code_rejected_voice
                        else -> R.string.redeem_code_voice
                    },
                ),
            )
            Spacer(Modifier.height(4.dp))
            CodeField(
                code = state.code,
                label = stringResource(R.string.redeem_code_label),
                hint = stringResource(R.string.redeem_code_hint),
                error = state.failure?.let { stringResource(it.messageRes()) },
                enabled = !state.isValidating,
                onCodeChange = onCodeChange,
                onDone = onValidate,
            )
        }
        Column(
            modifier = Modifier.padding(top = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when {
                // Plans do not combine: paying another one makes no sense either, so only the way back stays.
                hasPlan -> PrimaryPillButton(text = stringResource(R.string.redeem_code_action_my_plan), onClick = onOpenMyPlan)
                rejected -> PrimaryPillButton(text = stringResource(R.string.redeem_code_action_try_another), onClick = onTryAnother)
                else -> PrimaryPillButton(
                    text = stringResource(R.string.redeem_code_action_redeem),
                    onClick = onValidate,
                    enabled = state.canValidate,
                )
            }
            if (!hasPlan) OutlinedPillButton(text = stringResource(R.string.redeem_code_action_pay_plan), onClick = onPayPlan)
        }
    }
}

/**
 * P05b · Aceptar y compartir (and its Cupo menor variant): who pays, what the manager will see and what never, before
 * the code is spent. "Aceptar y activar" redeems it and records the consent; "Ahora no" leaves no link.
 */
@Composable
internal fun AcceptSharingContent(
    preview: CodePreview,
    isAccepting: Boolean,
    failure: String?,
    onBack: () -> Unit,
    onAccept: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val zone = LocalAppZone.current
    val locale = LocalConfiguration.current.locales[0]
    val endsOn = preview.planEndsAt?.let { formatDayMonthYear(it.atZone(zone).toLocalDate(), locale) }
    val quota = preview.quotaHectares.asHectares()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral100)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
    ) {
        BackArrow(onBack)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            EditorialHeadline(
                lead = stringResource(R.string.consent_title_lead),
                emphasis = stringResource(R.string.consent_title_emphasis),
            )
            Spacer(Modifier.height(4.dp))
            VioraVoice(
                text = if (endsOn != null) {
                    stringResource(R.string.consent_voice, preview.organizationName, quota, endsOn)
                } else {
                    stringResource(R.string.consent_voice_no_end, preview.organizationName, quota)
                },
            )
            Spacer(Modifier.height(8.dp))
            SharingCard(
                title = preview.managerName?.let { stringResource(R.string.consent_will_see, it) }
                    ?: stringResource(R.string.consent_will_see_no_manager),
                icon = R.drawable.ic_check,
                lines = listOf(
                    stringResource(R.string.consent_sees_contact),
                    stringResource(R.string.consent_sees_plots),
                    stringResource(R.string.consent_sees_records),
                ),
            )
            SharingCard(
                title = stringResource(R.string.consent_wont_see),
                icon = R.drawable.ic_close,
                lines = listOf(stringResource(R.string.consent_never_sees)),
            )
            val warning = failure ?: if (preview.exceedsQuota) {
                stringResource(R.string.consent_quota_below, preview.registeredHectares.asHectares(), quota)
            } else {
                null
            }
            Text(
                text = warning ?: stringResource(R.string.consent_note),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = if (warning != null) Terracotta600 else Neutral700,
            )
        }
        Column(
            modifier = Modifier.padding(top = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PrimaryPillButton(
                text = stringResource(R.string.consent_action_accept),
                onClick = onAccept,
                enabled = !isAccepting,
                isLoading = isAccepting,
            )
            OutlinedPillButton(text = stringResource(R.string.consent_action_not_now), onClick = onNotNow)
        }
    }
}

/** A white card of P05b: what the manager will see (checks) or never sees (crosses). */
@Composable
private fun SharingCard(title: String, @DrawableRes icon: Int, lines: List<String>) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Neutral0)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp), color = Neutral700)
        lines.forEach { line ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(painterResource(icon), contentDescription = null, tint = Neutral900, modifier = Modifier.size(18.dp))
                Text(line, style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp), color = Neutral900)
            }
        }
    }
}

@Composable
private fun BackArrow(onBack: () -> Unit) {
    IconButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp).size(44.dp)) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_back),
            contentDescription = stringResource(R.string.action_back),
            tint = Neutral900,
        )
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

private fun RedeemFailure.messageRes(): Int = when (this) {
    RedeemFailure.NotRedeemable -> R.string.redeem_code_error_not_redeemable
    RedeemFailure.AlreadySubscribed -> R.string.redeem_code_error_already_subscribed
    RedeemFailure.TooManyAttempts -> R.string.redeem_code_error_too_many_attempts
    is RedeemFailure.Other -> error.messageRes()
}
