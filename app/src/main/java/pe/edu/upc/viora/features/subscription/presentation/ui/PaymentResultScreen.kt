package pe.edu.upc.viora.features.subscription.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.*
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.PrimaryPillButton
import pe.edu.upc.viora.features.subscription.presentation.state.IndividualCheckoutUiState

/** P04: native full-screen results; entitlement and rejection come only from server evidence. */
@Composable
internal fun PaymentResultScreen(
    state: IndividualCheckoutUiState,
    producerName: String?,
    onCheckStatus: () -> Unit,
    onRetry: () -> Unit,
    onRedeemCode: () -> Unit,
    onRegisterPlot: () -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val confirmed = state as? IndividualCheckoutUiState.Confirmed
    val rejected = state is IndividualCheckoutUiState.Rejected
    val unconfirmed = state is IndividualCheckoutUiState.Unconfirmed
    val locale = LocalConfiguration.current.locales[0]
    BackHandler(onBack = onGoHome)
    Column(modifier.fillMaxSize().background(Neutral50).statusBarsPadding().navigationBarsPadding()
        .padding(horizontal = Spacing.lg).testTag("payment-result")) {
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(top = Spacing.xxxl + Spacing.xs), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(width = Spacing.xxxl * 3 + Spacing.lg + Spacing.xxs,
                height = Spacing.xxxl * 3 + Spacing.xs), contentAlignment = Alignment.Center) {
                Image(painterResource(if (confirmed != null || rejected) R.drawable.payment_result_shape
                    else R.drawable.payment_pending_shape), stringResource(R.string.payment_result_illustration),
                    Modifier.fillMaxSize())
                Icon(painterResource(R.drawable.ic_isotype), null, tint = Color.Unspecified,
                    modifier = Modifier.size(Spacing.xxxl + Spacing.xxl + Spacing.xs))
            }
            Spacer(Modifier.height(Spacing.xl))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Box(Modifier.size(Spacing.xxs).background(if (rejected) Terracotta500 else Harvest300, CircleShape))
                Text(stringResource(when {
                    confirmed != null -> R.string.payment_result_active_badge
                    rejected -> R.string.payment_result_rejected_badge
                    else -> R.string.payment_result_provider_badge
                }), style = PaymentResultTypography.eyebrow, color = Neutral700)
            }
            Spacer(Modifier.height(Spacing.sm))
            val lead = stringResource(when {
                confirmed != null -> R.string.payment_result_active_lead
                rejected -> R.string.payment_result_rejected_lead
                unconfirmed -> R.string.payment_result_unconfirmed_lead
                else -> R.string.payment_result_verifying_lead
            })
            val emphasis = when {
                confirmed != null -> producerName?.takeIf { it.isNotBlank() }?.let {
                    stringResource(R.string.payment_result_name, it)
                }.orEmpty()
                rejected -> stringResource(R.string.payment_result_rejected_emphasis)
                unconfirmed -> stringResource(R.string.payment_result_unconfirmed_emphasis)
                else -> stringResource(R.string.payment_result_verifying_emphasis)
            }
            Text(lead, style = PaymentResultTypography.headline, color = Neutral900, textAlign = TextAlign.Center)
            if (emphasis.isNotEmpty()) Text(emphasis, style = PaymentResultTypography.headline,
                fontStyle = FontStyle.Italic, color = Neutral900, textAlign = TextAlign.Center)
            Spacer(Modifier.height(Spacing.sm))
            Text(stringResource(when {
                confirmed != null -> R.string.payment_result_active_body
                rejected -> R.string.payment_result_rejected_body
                unconfirmed -> R.string.purchase_unconfirmed_message
                else -> R.string.payment_result_verifying_body
            }), style = PaymentResultTypography.body, color = Neutral700, textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = Spacing.xxxl * 5))
            if (confirmed != null) {
                Spacer(Modifier.height(Spacing.lg))
                HorizontalDivider(color = Neutral200)
                PaymentResultRow(stringResource(R.string.payment_result_plan),
                    stringResource(R.string.payment_result_quota, formatPlanCapacity(confirmed.subscription.capacity.hectares, locale)))
                val end = confirmed.subscription.endsAt?.atZone(ZoneId.systemDefault())?.toLocalDate()
                PaymentResultRow(stringResource(R.string.payment_result_validity), end?.let {
                    val pattern = android.text.format.DateFormat.getBestDateTimePattern(locale, "ddMMyyyy")
                    stringResource(R.string.payment_result_until, it.format(DateTimeFormatter.ofPattern(pattern, locale)))
                } ?: stringResource(R.string.payment_result_unavailable))
                PaymentResultRow(stringResource(R.string.payment_result_receipt), stringResource(R.string.payment_result_unavailable))
            }
            Spacer(Modifier.height(Spacing.lg))
        }
        if (confirmed != null || rejected || unconfirmed) {
            Column(Modifier.padding(top = Spacing.md, bottom = Spacing.xs), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                PrimaryPillButton(stringResource(when {
                    confirmed != null -> R.string.membership_action_register_plot
                    rejected -> R.string.payment_result_retry
                    else -> R.string.purchase_check_status
                }), when {
                    confirmed != null -> onRegisterPlot
                    rejected -> onRetry
                    else -> onCheckStatus
                }, modifier = Modifier.height(Spacing.xxl + Spacing.xs))
                TextButton(onClick = if (rejected) onRedeemCode else onGoHome,
                    modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.xxl + Spacing.xs)) {
                    Text(stringResource(if (rejected) R.string.producer_plan_cooperative else R.string.membership_action_home),
                        style = MaterialTheme.typography.titleMedium, color = Neutral900)
                }
            }
        }
    }
}

@Composable
private fun PaymentResultRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.Top) {
        Text(label, style = PaymentResultTypography.rowLabel, color = Neutral600, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleSmall, color = Neutral900,
            textAlign = TextAlign.End, modifier = Modifier.weight(2f))
    }
    HorizontalDivider(color = Neutral200)
}
