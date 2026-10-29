package pe.edu.upc.viora.features.subscription.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.MorphingShapeIndicator
import pe.edu.upc.viora.core.designsystem.theme.*
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.presentation.LocalAppZone
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.PrimaryPillButton
import pe.edu.upc.viora.features.subscription.domain.entity.ExtensionAction
import pe.edu.upc.viora.features.subscription.presentation.state.PlanExtensionUiState
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.PaymentResultProfileViewModel
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.PlanExtensionViewModel

/**
 * P102 · Ampliar mi plan (Figma section 22) and the P04 results of its payment. A plan that does not
 * grow by payment is handed over at once: [onUseCode] for a plan activated with a code, [onRenew] in
 * the last 30 days of a paid plan and [onBuyPlan] without a plan in force. [returning] is set by the
 * Mercado Pago return link: the screen then shows the result of the payment the producer just made.
 */
@Composable
fun PlanExtensionScreen(
    onBack: () -> Unit,
    onUseCode: () -> Unit,
    onRenew: () -> Unit,
    onBuyPlan: () -> Unit,
    onRegisterPlot: () -> Unit,
    onViewPlan: () -> Unit,
    modifier: Modifier = Modifier,
    returning: Boolean = false,
    viewModel: PlanExtensionViewModel = hiltViewModel(),
    profileViewModel: PaymentResultProfileViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val producerName = profileViewModel.name.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    LaunchedEffect(viewModel) { viewModel.start(returning) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResumed() }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { viewModel.onPaused() }

    when (state) {
        is PlanExtensionUiState.Redirect -> LaunchedEffect(state) {
            when (state.action) {
                ExtensionAction.CODE -> onUseCode()
                ExtensionAction.RENEW -> onRenew()
                ExtensionAction.NONE, ExtensionAction.PAY -> onBuyPlan()
            }
        }
        is PlanExtensionUiState.Selecting -> state.checkout?.let { link ->
            LaunchedEffect(link) {
                val opened = runCatching {
                    context.startActivity(
                        android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(link.url))
                            .addCategory(android.content.Intent.CATEGORY_BROWSABLE),
                    )
                }.isSuccess
                if (opened) viewModel.onBrowserOpened() else viewModel.onBrowserFailed()
            }
        }
        else -> Unit
    }

    when (state) {
        PlanExtensionUiState.Verifying, PlanExtensionUiState.Rejected, PlanExtensionUiState.Unconfirmed,
        is PlanExtensionUiState.Applied -> ExtensionPaymentResult(
            state = state,
            producerName = producerName,
            onRegisterPlot = onRegisterPlot,
            onViewPlan = onViewPlan,
            onRetry = viewModel::retry,
            onCheckAgain = viewModel::checkAgain,
            onLeave = onBack,
            modifier = modifier,
        )
        else -> PlanExtensionContent(
            state = state,
            onBack = onBack,
            onIncrease = viewModel::increase,
            onDecrease = viewModel::decrease,
            onPay = viewModel::pay,
            onRetry = viewModel::load,
            modifier = modifier,
        )
    }
}

@Composable
internal fun PlanExtensionContent(
    state: PlanExtensionUiState,
    onBack: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onPay: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().background(Neutral50).statusBarsPadding().navigationBarsPadding()
        .padding(horizontal = Spacing.lg)) {
        IconButton(onBack, Modifier.size(Spacing.minTouchTarget)) {
            Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.action_back), tint = Neutral900)
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = Spacing.md, bottom = Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Box(Modifier.size(Spacing.xxs).background(Harvest300, CircleShape))
                Text(stringResource(R.string.plan_extension_eyebrow).uppercase(), style = EditorialEyebrow, color = Neutral700)
            }
            Text(buildAnnotatedString {
                append(stringResource(R.string.plan_extension_title_lead))
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(stringResource(R.string.plan_extension_title_emphasis)) }
            }, style = MaterialTheme.typography.displayMedium, color = Neutral900)
            Spacer(Modifier.height(Spacing.xl))
            HorizontalDivider(color = Neutral200)
            when (state) {
                is PlanExtensionUiState.Selecting -> {
                    AddedHectaresSelector(state, onIncrease, onDecrease)
                    QuotaGrowthBar(state)
                    HorizontalDivider(color = Neutral200, modifier = Modifier.padding(top = Spacing.lg))
                    ExtensionSummary(state)
                    HorizontalDivider(color = Neutral200)
                    ExtensionPrice(state)
                    state.error?.let { error ->
                        Text(stringResource(extensionErrorMessage(error)), Modifier.padding(top = Spacing.sm),
                            style = MaterialTheme.typography.bodyMedium, color = Terracotta600)
                    }
                }
                is PlanExtensionUiState.Failed -> {
                    Text(stringResource(state.error.messageRes()), Modifier.padding(vertical = Spacing.lg),
                        style = MaterialTheme.typography.bodyLarge, color = Terracotta600)
                    TextButton(onRetry) { Text(stringResource(R.string.producer_plan_retry)) }
                }
                else -> {
                    Spacer(Modifier.height(Spacing.xl))
                    MorphingShapeIndicator(Green900, Modifier.size(Spacing.xxl).align(Alignment.CenterHorizontally))
                    Text(stringResource(R.string.plan_extension_loading), Modifier.padding(vertical = Spacing.lg),
                        style = MaterialTheme.typography.bodyLarge, color = Neutral700)
                }
            }
        }
        if (state is PlanExtensionUiState.Selecting) {
            Column(Modifier.padding(top = Spacing.md, bottom = Spacing.xs), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                PrimaryPillButton(
                    text = stringResource(if (state.isWorking) R.string.purchase_working else R.string.purchase_pay),
                    onClick = onPay,
                    isLoading = state.isWorking,
                    modifier = Modifier.testTag("plan-extension-pay"),
                )
                TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.xxl + Spacing.xs)) {
                    Text(stringResource(R.string.plan_extension_later), style = MaterialTheme.typography.titleMedium, color = Neutral900)
                }
            }
        }
    }
}

@Composable
private fun AddedHectaresSelector(state: PlanExtensionUiState.Selecting, onIncrease: () -> Unit, onDecrease: () -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val step = formatPlanCapacity(state.pricing.increment, locale)
    Text(stringResource(R.string.plan_extension_hectares), Modifier.padding(top = Spacing.lg),
        style = EditorialEyebrow, color = Neutral600)
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(buildAnnotatedString {
            append(stringResource(R.string.plan_extension_added, formatPlanCapacity(state.addedHectares, locale)))
            withStyle(SpanStyle(fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                fontStyle = FontStyle.Italic, color = Neutral600)) { append(stringResource(R.string.producer_plan_ha_suffix)) }
        }, Modifier.weight(1f).testTag("plan-extension-added"), style = EditorialMeasure, color = Neutral900)
        val decreaseLabel = stringResource(R.string.producer_plan_decrease, step)
        IconButton(onDecrease, enabled = state.canDecrease && !state.isWorking,
            modifier = Modifier.size(Spacing.minTouchTarget).border(Dp.Hairline, Neutral300, CircleShape)
                .semantics { contentDescription = decreaseLabel }) {
            Text(stringResource(R.string.producer_plan_minus), style = EditorialAction,
                color = if (state.canDecrease) Neutral900 else Neutral300)
        }
        val increaseLabel = stringResource(R.string.producer_plan_increase, step)
        IconButton(onIncrease, enabled = !state.isWorking,
            modifier = Modifier.size(Spacing.minTouchTarget).background(Green900, CircleShape)
                .semantics { contentDescription = increaseLabel }) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = null, tint = Neutral50)
        }
    }
}

/** The quota today in Forest and the new hectares in Harvest, on one track (Figma "Cupo · 12 + 3 ha"). */
@Composable
private fun QuotaGrowthBar(state: PlanExtensionUiState.Selecting) {
    val locale = LocalConfiguration.current.locales[0]
    val share = state.quotaHectares.toFloat() / state.newQuota.toFloat().coerceAtLeast(0.01f)
    Column(Modifier.padding(top = Spacing.md), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Row(Modifier.fillMaxWidth().height(8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.weight(share.coerceIn(0.01f, 0.99f)).fillMaxHeight().clip(CircleShape).background(Green900))
            Box(Modifier.weight((1f - share).coerceIn(0.01f, 0.99f)).fillMaxHeight().clip(CircleShape).background(Harvest300))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.plan_extension_bar_current, formatPlanCapacity(state.quotaHectares, locale)),
                style = MaterialTheme.typography.bodySmall, color = Neutral700)
            Text(stringResource(R.string.plan_extension_bar_new, formatPlanCapacity(state.addedHectares, locale),
                formatPlanCapacity(state.newQuota, locale)), style = MaterialTheme.typography.bodySmall, color = Neutral700)
        }
    }
}

@Composable
private fun ExtensionSummary(state: PlanExtensionUiState.Selecting) {
    val locale = LocalConfiguration.current.locales[0]
    val months = state.pricing.billableMonths
    val days = state.pricing.billableDays
    val items = listOf(
        stringResource(R.string.plan_extension_item_quota, formatPlanCapacity(state.quotaHectares, locale),
            formatPlanCapacity(state.newQuota, locale)),
        stringResource(R.string.plan_extension_item_end, planDate(state.endsAt) ?: "—"),
        if (days != null) pluralStringResource(R.plurals.plan_extension_item_days, days, days)
        else pluralStringResource(R.plurals.plan_extension_item_months, months, months),
        stringResource(R.string.plan_extension_item_history),
    )
    Column(Modifier.padding(vertical = Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(stringResource(R.string.plan_extension_includes), style = EditorialEyebrow, color = Neutral600)
        items.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.width(Spacing.md), color = Neutral700)
                Text(item, style = MaterialTheme.typography.bodyLarge, color = Neutral700)
            }
        }
    }
}

@Composable
private fun ExtensionPrice(state: PlanExtensionUiState.Selecting) {
    val locale = LocalConfiguration.current.locales[0]
    val currency = state.pricing.currency
    val months = state.pricing.billableMonths
    val days = state.pricing.billableDays
    Text(stringResource(R.string.plan_extension_total), Modifier.padding(top = Spacing.lg),
        style = EditorialEyebrow, color = Neutral600)
    Text(formatPlanMoney(state.total, currency, locale), Modifier.testTag("plan-extension-total").drawBehind {
        drawLine(Harvest300.copy(alpha = 0.55f), Offset(0f, size.height * 0.82f),
            Offset(size.width, size.height * 0.82f), strokeWidth = size.height * 0.16f)
    }, style = EditorialPrice, color = Neutral900)
    val breakdown = if (days != null) {
        pluralStringResource(R.plurals.plan_extension_breakdown_days, days, days,
            formatPlanCapacity(state.addedHectares, locale), formatPlanMoney(state.pricing.ratePerHa, currency, locale))
    } else {
        pluralStringResource(R.plurals.plan_extension_breakdown, months, months,
            formatPlanCapacity(state.addedHectares, locale), formatPlanMoney(state.pricing.monthlyRatePerHa, currency, locale))
    }
    Text(breakdown, style = MaterialTheme.typography.bodySmall, color = Neutral700)
}

/** P04 for an extension: confirming, approved ("Tu plan creció"), rejected or not confirmed yet. */
@Composable
private fun ExtensionPaymentResult(
    state: PlanExtensionUiState,
    producerName: String?,
    onRegisterPlot: () -> Unit,
    onViewPlan: () -> Unit,
    onRetry: () -> Unit,
    onCheckAgain: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val applied = state as? PlanExtensionUiState.Applied
    val rejected = state is PlanExtensionUiState.Rejected
    val unconfirmed = state is PlanExtensionUiState.Unconfirmed
    val locale = LocalConfiguration.current.locales[0]
    BackHandler(onBack = if (applied != null) onViewPlan else onLeave)
    Column(modifier.fillMaxSize().background(Neutral50).statusBarsPadding().navigationBarsPadding()
        .padding(horizontal = Spacing.lg).testTag("plan-extension-result")) {
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(top = Spacing.xxxl + Spacing.xs), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(width = Spacing.xxxl * 3 + Spacing.lg + Spacing.xxs, height = Spacing.xxxl * 3 + Spacing.xs),
                contentAlignment = Alignment.Center) {
                Image(painterResource(if (applied != null || rejected) R.drawable.payment_result_shape
                    else R.drawable.payment_pending_shape), stringResource(R.string.payment_result_illustration),
                    Modifier.fillMaxSize())
                Icon(painterResource(R.drawable.ic_isotype), null, tint = Color.Unspecified,
                    modifier = Modifier.size(Spacing.xxxl + Spacing.xxl + Spacing.xs))
            }
            Spacer(Modifier.height(Spacing.xl))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Box(Modifier.size(Spacing.xxs).background(if (rejected) Terracotta500 else Harvest300, CircleShape))
                Text(when {
                    applied != null -> stringResource(R.string.plan_extension_applied_badge).uppercase()
                    rejected -> stringResource(R.string.payment_result_rejected_badge)
                    else -> stringResource(R.string.payment_result_provider_badge)
                }, style = PaymentResultTypography.eyebrow, color = Neutral700)
            }
            Spacer(Modifier.height(Spacing.sm))
            val name = producerName?.takeIf { it.isNotBlank() }
            val lead = stringResource(when {
                applied != null -> if (name != null) R.string.plan_extension_applied_lead else R.string.plan_extension_applied_lead_alone
                rejected -> R.string.payment_result_rejected_lead
                unconfirmed -> R.string.payment_result_unconfirmed_lead
                else -> R.string.payment_result_verifying_lead
            })
            val emphasis = when {
                applied != null -> name?.let { stringResource(R.string.payment_result_name, it) }.orEmpty()
                rejected -> stringResource(R.string.payment_result_rejected_emphasis)
                unconfirmed -> stringResource(R.string.payment_result_unconfirmed_emphasis)
                else -> stringResource(R.string.payment_result_verifying_emphasis)
            }
            Text(lead, style = PaymentResultTypography.headline, color = Neutral900, textAlign = TextAlign.Center)
            if (emphasis.isNotEmpty()) Text(emphasis, style = PaymentResultTypography.headline,
                fontStyle = FontStyle.Italic, color = Neutral900, textAlign = TextAlign.Center)
            Spacer(Modifier.height(Spacing.sm))
            Text(when {
                applied != null -> stringResource(R.string.plan_extension_applied_body,
                    formatPlanCapacity(applied.extension.addedHectares, locale))
                rejected -> stringResource(R.string.payment_result_rejected_body)
                unconfirmed -> stringResource(R.string.purchase_unconfirmed_message)
                else -> stringResource(R.string.payment_result_verifying_body)
            }, style = PaymentResultTypography.body, color = Neutral700, textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = Spacing.xxxl * 5))
            if (applied != null) {
                Spacer(Modifier.height(Spacing.lg))
                HorizontalDivider(color = Neutral200)
                ResultRow(stringResource(R.string.membership_row_quota), stringResource(R.string.plan_extension_quota_change,
                    formatPlanCapacity(applied.previousQuota, locale), formatPlanCapacity(applied.extension.quotaHectares, locale)))
                ResultRow(stringResource(R.string.payment_result_validity), planDate(applied.extension.endsAt)
                    ?.let { stringResource(R.string.payment_result_until, it) } ?: stringResource(R.string.payment_result_unavailable))
                ResultRow(stringResource(R.string.payment_result_receipt), stringResource(R.string.my_plan_detail_receipt_sent))
            }
        }
        if (state !is PlanExtensionUiState.Verifying) {
            Column(Modifier.padding(top = Spacing.md, bottom = Spacing.xs), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                PrimaryPillButton(stringResource(when {
                    applied != null -> R.string.my_plan_register_plot
                    rejected -> R.string.plan_extension_retry
                    else -> R.string.purchase_check_status
                }), when {
                    applied != null -> onRegisterPlot
                    rejected -> onRetry
                    else -> onCheckAgain
                })
                TextButton(onClick = if (applied != null) onViewPlan else onLeave,
                    modifier = Modifier.fillMaxWidth().heightIn(min = Spacing.xxl + Spacing.xs)) {
                    Text(stringResource(if (applied != null) R.string.plan_extension_view_plan else R.string.plan_extension_later),
                        style = MaterialTheme.typography.titleMedium, color = Neutral900)
                }
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md), verticalAlignment = Alignment.Top) {
        Text(label, style = PaymentResultTypography.rowLabel, color = Neutral600, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleSmall, color = Neutral900,
            textAlign = TextAlign.End, modifier = Modifier.weight(2f))
    }
    HorizontalDivider(color = Neutral200)
}

/** "21/10/2027", as P99 writes the dates of the plan. */
@Composable
internal fun planDate(at: Instant?): String? {
    val pattern = stringResource(R.string.my_plan_date_pattern)
    val locale: Locale = LocalConfiguration.current.locales[0]
    return at?.atZone(LocalAppZone.current)?.toLocalDate()?.format(DateTimeFormatter.ofPattern(pattern, locale))
}

/** The renewal window has its own message; anything else uses the app's generic one. */
private fun extensionErrorMessage(error: AppError): Int =
    if (error is AppError.Conflict && error.detail == "subscription.extension.renewal_window") {
        R.string.plan_extension_error_renewal
    } else error.messageRes()
