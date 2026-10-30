package pe.edu.upc.viora.features.subscription.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.math.BigDecimal
import java.time.Instant
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.EditorialHeadline
import pe.edu.upc.viora.core.designsystem.component.OutlinedPillButton
import pe.edu.upc.viora.core.designsystem.component.PrimaryPillButton
import pe.edu.upc.viora.core.designsystem.component.VioraVoice
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.subscription.domain.entity.QuotaExtension
import pe.edu.upc.viora.features.subscription.presentation.state.ExtensionCodeUiState
import pe.edu.upc.viora.features.subscription.presentation.state.RedeemFailure
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.ExtensionCodeViewModel

/**
 * P103 · Ampliar con código (Figma section 22), with its Validando and "Este código no suma"
 * variants. Opens P104 through [onApplied] once the backend adds the hectares.
 */
@Composable
fun ExtensionCodeScreen(
    onBack: () -> Unit,
    onApplied: (QuotaExtension, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExtensionCodeViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    LaunchedEffect(state.applied) { state.applied?.let { onApplied(it, state.code) } }
    ExtensionCodeContent(
        state = state,
        onBack = onBack,
        onCodeChange = viewModel::setCode,
        onRedeem = viewModel::redeem,
        onTryAnother = viewModel::tryAnotherCode,
        modifier = modifier,
    )
}

@Composable
internal fun ExtensionCodeContent(
    state: ExtensionCodeUiState,
    onBack: () -> Unit,
    onCodeChange: (String) -> Unit,
    onRedeem: () -> Unit,
    onTryAnother: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isRedeeming) {
        ValidatingCodeContent(
            eyebrow = stringResource(R.string.redeem_code_validating_eyebrow, state.code),
            lead = stringResource(R.string.extension_code_validating_lead),
            emphasis = stringResource(R.string.extension_code_validating_emphasis),
            body = stringResource(R.string.extension_code_validating_body),
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
            Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.action_back), tint = Neutral900)
        }
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EditorialHeadline(
                lead = stringResource(if (rejected) R.string.extension_code_rejected_title_lead else R.string.extension_code_title_lead),
                emphasis = stringResource(
                    if (rejected) R.string.extension_code_rejected_title_emphasis else R.string.extension_code_title_emphasis,
                ),
            )
            VioraVoice(text = stringResource(if (rejected) R.string.extension_code_rejected_voice else R.string.extension_code_voice))
            Spacer(Modifier.height(4.dp))
            CodeField(
                code = state.code,
                label = stringResource(R.string.extension_code_label),
                hint = stringResource(R.string.extension_code_hint),
                error = state.failure?.let { stringResource(it.extensionMessageRes()) },
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
                    text = stringResource(R.string.my_plan_expand),
                    onClick = onRedeem,
                    enabled = state.canRedeem,
                )
            }
            OutlinedPillButton(text = stringResource(R.string.extension_code_back), onClick = onBack)
        }
    }
}

/**
 * P104 · Cupo ampliado: what the code added, with the plan's end date untouched, then on to a new
 * plot or back to P99.
 */
@Composable
fun ExtensionAppliedScreen(
    addedHectares: BigDecimal,
    quotaHectares: BigDecimal,
    endsAt: Instant?,
    code: String,
    onRegisterPlot: () -> Unit,
    onViewPlan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral100)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(Modifier.size(184.dp).clip(CircleShape).background(Green200), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_isotype), null, tint = Color.Unspecified, modifier = Modifier.size(104.dp))
            }
            Spacer(Modifier.height(40.dp))
            StatusHeading(
                eyebrow = stringResource(R.string.extension_applied_badge),
                lead = stringResource(R.string.extension_applied_lead),
                emphasis = stringResource(R.string.extension_applied_emphasis),
                body = stringResource(R.string.extension_applied_body, formatPlanCapacity(addedHectares, locale)),
            )
            Spacer(Modifier.height(28.dp))
            HorizontalDivider(color = Neutral200)
            MembershipRow(stringResource(R.string.membership_row_quota), stringResource(R.string.plan_extension_quota_change,
                formatPlanCapacity(quotaHectares.subtract(addedHectares), locale), formatPlanCapacity(quotaHectares, locale)))
            MembershipRow(stringResource(R.string.membership_row_validity), planDate(endsAt)
                ?.let { stringResource(R.string.payment_result_until, it) } ?: stringResource(R.string.membership_validity_value))
            MembershipRow(stringResource(R.string.my_plan_detail_activation), stringResource(R.string.extension_applied_activation, code))
        }
        Column(
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            PrimaryPillButton(text = stringResource(R.string.my_plan_register_plot), onClick = onRegisterPlot)
            Text(
                text = stringResource(R.string.plan_extension_view_plan),
                style = MaterialTheme.typography.titleMedium,
                color = Neutral900,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onViewPlan)
                    .padding(horizontal = 24.dp, vertical = 14.dp),
            )
        }
    }
}

private fun RedeemFailure.extensionMessageRes(): Int = when (this) {
    RedeemFailure.NotRedeemable -> R.string.extension_code_error_not_redeemable
    RedeemFailure.AlreadySubscribed -> R.string.extension_code_error_not_redeemable
    RedeemFailure.TooManyAttempts -> R.string.redeem_code_error_too_many_attempts
    is RedeemFailure.Other -> error.messageRes()
}
