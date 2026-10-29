package pe.edu.upc.viora.features.subscription.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.math.BigDecimal
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.MorphingShapeIndicator
import pe.edu.upc.viora.core.designsystem.theme.*
import pe.edu.upc.viora.core.presentation.LocalAppZone
import pe.edu.upc.viora.core.presentation.formatMoney
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.core.presentation.vioraNumberFormat
import pe.edu.upc.viora.features.subscription.presentation.state.IndividualCheckoutUiState
import pe.edu.upc.viora.features.subscription.presentation.state.ProducerPlanUiState
import pe.edu.upc.viora.features.subscription.presentation.state.RenewalInfo
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.IndividualCheckoutViewModel
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.ProducerPlanViewModel

/**
 * P02 browsing/selection. Payment is deliberately supplied by the separate checkout feature. With
 * [renewal] it is "Renueva y amplía" (Figma P102 · Por vencer): the year after the plan that is ending.
 */
@Composable
fun ProducerPlanScreen(
    onBack: () -> Unit,
    onRedeemCode: () -> Unit,
    modifier: Modifier = Modifier,
    renewal: Boolean = false,
    viewModel: ProducerPlanViewModel = hiltViewModel(),
    checkoutViewModel: IndividualCheckoutViewModel = hiltViewModel(),
    onRegisterPlot: () -> Unit = onBack,
    onGoHome: () -> Unit = onBack,
    resultProfileViewModel: pe.edu.upc.viora.features.subscription.presentation.viewmodel.PaymentResultProfileViewModel = hiltViewModel(),
) {
    val offer = viewModel.uiState.collectAsStateWithLifecycle().value
    val checkout = checkoutViewModel.uiState.collectAsStateWithLifecycle().value
    val producerName = resultProfileViewModel.name.collectAsStateWithLifecycle().value
    val openBrowser = rememberCheckoutBrowser(
        onOpened = checkoutViewModel::onBrowserOpened,
        onFailed = checkoutViewModel::browserFailed,
    )

    // Set before the resume below runs: in renewal mode it must not recover the year that is ending.
    checkoutViewModel.renewal = renewal
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        checkoutViewModel.onAppResumed()
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        checkoutViewModel.onAppPaused()
    }
    DisposableEffect(checkoutViewModel) {
        onDispose { checkoutViewModel.onAppPaused() }
    }

    if (checkout is IndividualCheckoutUiState.Verifying || checkout is IndividualCheckoutUiState.Confirmed ||
        checkout is IndividualCheckoutUiState.Rejected || checkout is IndividualCheckoutUiState.Unconfirmed) {
        PaymentResultScreen(checkout, producerName, checkoutViewModel::checkStatus,
            checkoutViewModel::retryPayment,
            onRedeemCode = { checkoutViewModel.dismiss(); onRedeemCode() },
            onRegisterPlot = { checkoutViewModel.dismiss(); onRegisterPlot() },
            onGoHome = { checkoutViewModel.dismiss(); onGoHome() }, modifier = modifier)
        return
    }

    ProducerPlanContent(
        offer,
        onBack, onRedeemCode, viewModel::increase, viewModel::decrease, viewModel::selectOffer,
        viewModel::refresh, modifier,
        checkoutAction = { content ->
            IndividualCheckoutAction(
                offer = content,
                state = checkout,
                onStart = { checkoutViewModel.start(content.selectedOffer.code, content.capacity) },
                onPay = checkoutViewModel::pay,
                onDismiss = {
                    checkoutViewModel.dismiss()
                    if (checkout is IndividualCheckoutUiState.Confirmed) {
                        viewModel.refresh()
                    }
                },
                onCheckStatus = checkoutViewModel::checkStatus,
                onOpen = openBrowser,
            )
        }
    )
}

@Composable
internal fun ProducerPlanContent(
    state: ProducerPlanUiState,
    onBack: () -> Unit,
    onRedeemCode: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onSelectOffer: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    checkoutAction: @Composable (ProducerPlanUiState.Content) -> Unit = {},
) {
    Column(modifier.fillMaxSize().background(Neutral50).statusBarsPadding().navigationBarsPadding()
        .padding(horizontal = Spacing.lg)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBack, Modifier.size(Spacing.minTouchTarget)) {
                Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.action_back), tint = Neutral900)
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = Spacing.md, bottom = Spacing.lg)) {
            val renewal = (state as? ProducerPlanUiState.Content)?.renewal
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Box(Modifier.size(Spacing.xxs).background(if (renewal != null) Terracotta500 else Harvest300, CircleShape))
                Text(if (renewal != null) {
                    pluralStringResource(R.plurals.producer_plan_renew_eyebrow, renewal.daysLeft, renewal.daysLeft).uppercase()
                } else stringResource(R.string.producer_plan_eyebrow), style = EditorialEyebrow, color = Neutral700)
            }
            Text(buildAnnotatedString {
                append(stringResource(if (renewal != null) R.string.producer_plan_renew_title_lead else R.string.producer_plan_title_lead))
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(stringResource(if (renewal != null) R.string.producer_plan_renew_title_emphasis else R.string.producer_plan_title_emphasis))
                }
            }, style = MaterialTheme.typography.displayMedium, color = Neutral900)
            Spacer(Modifier.height(Spacing.xl))
            HorizontalDivider(color = Neutral200)
            when (state) {
                ProducerPlanUiState.Loading -> {
                    Spacer(Modifier.height(Spacing.xl))
                    MorphingShapeIndicator(Green900, Modifier.size(Spacing.xxl).align(Alignment.CenterHorizontally))
                    Text(stringResource(R.string.producer_plan_loading), Modifier.padding(vertical = Spacing.lg),
                        style = MaterialTheme.typography.bodyLarge, color = Neutral700)
                }
                is ProducerPlanUiState.Failed -> {
                    Text(stringResource(state.error.messageRes()), Modifier.padding(vertical = Spacing.lg),
                        style = MaterialTheme.typography.bodyLarge, color = Terracotta600)
                    TextButton(onRefresh) { Text(stringResource(R.string.common_retry)) }
                }
                is ProducerPlanUiState.Empty -> {
                    Text(stringResource(R.string.producer_plan_empty), Modifier.padding(vertical = Spacing.lg),
                        style = MaterialTheme.typography.bodyLarge, color = Neutral700)
                    state.error?.let { Text(stringResource(it.messageRes()), color = Terracotta600) }
                    TextButton(onRefresh, enabled = !state.isRefreshing) { Text(stringResource(R.string.common_retry)) }
                }
                is ProducerPlanUiState.Content -> {
                    if (state.offers.size > 1) {
                        state.offers.forEach { offer ->
                            FilterChip(selected = offer.code == state.selectedOffer.code,
                                onClick = { onSelectOffer(offer.code.value) }, label = { Text(offer.code.value) })
                        }
                    }
                    PlanCapacitySelector(state, onIncrease, onDecrease)
                    HorizontalDivider(color = Neutral200)
                    if (state.renewal != null) RenewalBenefits(state, state.renewal) else PlanBenefits(state)
                    HorizontalDivider(color = Neutral200)
                    PlanEstimateSummary(state)
                    PlanFreshness(state, onRefresh)
                    checkoutAction(state)
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            if ((state as? ProducerPlanUiState.Content)?.renewal == null) {
                TextButton(onRedeemCode, Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget)) {
                    Text(stringResource(R.string.producer_plan_cooperative), style = MaterialTheme.typography.titleMedium,
                        color = Neutral900)
                }
            }
        }
    }
}

@Composable
private fun PlanCapacitySelector(state: ProducerPlanUiState.Content, onIncrease: () -> Unit, onDecrease: () -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    val capacity = formatPlanCapacity(state.capacity.hectares, locale)
    val step = formatPlanCapacity(state.selectedOffer.quotaIncrement.hectares, locale)
    Text(stringResource(R.string.producer_plan_hectares), Modifier.padding(top = Spacing.lg),
        style = EditorialEyebrow, color = Neutral600)
    Row(Modifier.fillMaxWidth().padding(bottom = Spacing.lg), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(buildAnnotatedString {
            append(capacity)
            withStyle(SpanStyle(fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                fontStyle = FontStyle.Italic, color = Neutral600)) { append(stringResource(R.string.producer_plan_ha_suffix)) }
        }, Modifier.weight(1f).testTag("producer-plan-capacity"), style = EditorialMeasure, color = Neutral900)
        val decreaseLabel = stringResource(R.string.producer_plan_decrease, step)
        IconButton(onDecrease, enabled = state.canDecrease,
            modifier = Modifier.size(Spacing.minTouchTarget).border(Dp.Hairline,
                Neutral300, CircleShape).semantics { contentDescription = decreaseLabel }) {
            Text(stringResource(R.string.producer_plan_minus), style = EditorialAction,
                color = if (state.canDecrease) Neutral900 else Neutral300)
        }
        val increaseLabel = stringResource(R.string.producer_plan_increase, step)
        IconButton(onIncrease, modifier = Modifier.size(Spacing.minTouchTarget).background(Green900, CircleShape)
            .semantics { contentDescription = increaseLabel }) {
            Icon(painterResource(R.drawable.ic_add), contentDescription = null, tint = Neutral50)
        }
    }
}

@Composable
private fun PlanBenefits(state: ProducerPlanUiState.Content) {
    val capacity = formatPlanCapacity(state.capacity.hectares, LocalConfiguration.current.locales[0])
    Column(Modifier.padding(vertical = Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(stringResource(R.string.producer_plan_includes), style = EditorialEyebrow, color = Neutral600)
        listOf(stringResource(R.string.producer_plan_benefit_bbi), stringResource(R.string.producer_plan_benefit_climate),
            stringResource(R.string.producer_plan_benefit_sampling), stringResource(R.string.producer_plan_benefit_capacity, capacity))
            .forEach { benefit ->
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(Modifier.width(Spacing.md), color = Neutral700)
                    Text(benefit, style = MaterialTheme.typography.bodyLarge, color = Neutral700)
                }
            }
    }
}

/** "Tu nuevo año": what the renewal keeps and the dates of the year it buys. */
@Composable
private fun RenewalBenefits(state: ProducerPlanUiState.Content, renewal: RenewalInfo) {
    val locale = LocalConfiguration.current.locales[0]
    val newEnd = renewal.endsAt.atZone(java.time.ZoneOffset.UTC).plusYears(1).toInstant()
    val items = listOf(
        stringResource(R.string.producer_plan_renew_item_capacity, formatPlanCapacity(state.capacity.hectares, locale),
            formatPlanCapacity(renewal.currentQuota, locale)),
        stringResource(R.string.producer_plan_renew_item_period, planDate(renewal.endsAt).orEmpty(), planDate(newEnd).orEmpty()),
        stringResource(R.string.producer_plan_renew_item_history),
        stringResource(R.string.producer_plan_renew_item_alerts),
    )
    Column(Modifier.padding(vertical = Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(stringResource(R.string.producer_plan_renew_includes), style = EditorialEyebrow, color = Neutral600)
        items.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.width(Spacing.md), color = Neutral700)
                Text(item, style = MaterialTheme.typography.bodyLarge, color = Neutral700)
            }
        }
    }
}

@Composable
private fun PlanEstimateSummary(state: ProducerPlanUiState.Content) {
    val locale = LocalConfiguration.current.locales[0]
    val estimate = state.estimate
    val currency = state.selectedOffer.currency
    Text(stringResource(if (state.renewal != null) R.string.producer_plan_renew_total else R.string.producer_plan_total), Modifier.padding(top = Spacing.lg),
        style = EditorialEyebrow, color = Neutral600)
    Text(formatMoney(estimate.total, currency, locale), Modifier.testTag("producer-plan-total").drawBehind {
        drawLine(Harvest300.copy(alpha = 0.55f), Offset(0f, size.height * 0.82f),
            Offset(size.width, size.height * 0.82f), strokeWidth = size.height * 0.16f)
    }, style = EditorialPrice, color = Neutral900)
    Text(stringResource(R.string.producer_plan_tax_included),
        style = MaterialTheme.typography.bodySmall, color = Neutral700)
    Column(Modifier.padding(top = Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        Text(stringResource(R.string.producer_plan_monthly, formatMoney(estimate.monthlyTotal, currency, locale)),
            style = MaterialTheme.typography.bodySmall, color = Neutral700)
    }
}

@Composable
private fun PlanFreshness(state: ProducerPlanUiState.Content, onRefresh: () -> Unit) {
    if (state.isSavedCopy && !state.isRefreshing) {
        Text(stringResource(R.string.producer_plan_saved_copy),
            Modifier.padding(top = Spacing.lg), style = MaterialTheme.typography.bodySmall, color = Neutral600)
        CatalogUpdatedAt(state.fetchedAt)
    }
    if (state.isRefreshing) Text(stringResource(R.string.producer_plan_loading),
        style = MaterialTheme.typography.bodySmall, color = Neutral600)
    state.error?.let { Text(stringResource(it.messageRes()), Modifier.padding(top = Spacing.xs),
        style = MaterialTheme.typography.bodySmall, color = Terracotta600) }
    if (state.error != null) {
        TextButton(onRefresh, enabled = !state.isRefreshing) { Text(stringResource(R.string.common_retry)) }
    }
}

@Composable
private fun CatalogUpdatedAt(at: java.time.Instant) {
    val locale = LocalConfiguration.current.locales[0]
    val timestamp = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(locale)
        .withZone(LocalAppZone.current).format(at)
    Text(stringResource(R.string.producer_plan_updated, timestamp), Modifier.padding(top = Spacing.lg),
        style = MaterialTheme.typography.bodySmall, color = Neutral600)
}

/** Hectares as the rest of the app writes them ("0,5 ha" in Spanish, see [vioraNumberFormat]). */
internal fun formatPlanCapacity(value: BigDecimal, locale: Locale): String =
    vioraNumberFormat(0, value.scale().coerceAtLeast(0), locale).format(value)

