package pe.edu.upc.viora.features.subscription.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Currency
import java.util.Locale
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.MorphingShapeIndicator
import pe.edu.upc.viora.core.designsystem.theme.*
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.subscription.presentation.state.ProducerPlanUiState
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.ProducerPlanViewModel

/** P02 browsing/selection. Payment is deliberately supplied by the separate checkout feature. */
@Composable
fun ProducerPlanScreen(
    onBack: () -> Unit,
    onRedeemCode: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProducerPlanViewModel = hiltViewModel(),
) {
    ProducerPlanContent(viewModel.uiState.collectAsStateWithLifecycle().value,
        onBack, onRedeemCode, viewModel::increase, viewModel::decrease, viewModel::selectOffer,
        viewModel::refresh, modifier)
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
) {
    Column(modifier.fillMaxSize().background(Neutral50).statusBarsPadding().navigationBarsPadding()
        .padding(horizontal = Spacing.lg)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onBack, Modifier.size(Spacing.minTouchTarget)) {
                Icon(painterResource(R.drawable.ic_arrow_back), stringResource(R.string.action_back), tint = Neutral900)
            }
            TextButton(onRefresh, enabled = state !is ProducerPlanUiState.Loading &&
                (state as? ProducerPlanUiState.Content)?.isRefreshing != true &&
                (state as? ProducerPlanUiState.Empty)?.isRefreshing != true) {
                Text(stringResource(R.string.producer_plan_refresh))
            }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = Spacing.md, bottom = Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Box(Modifier.size(Spacing.xxs).background(Harvest300, CircleShape))
                Text(stringResource(R.string.producer_plan_eyebrow), style = EditorialEyebrow, color = Neutral700)
            }
            Text(buildAnnotatedString {
                append(stringResource(R.string.producer_plan_title_lead))
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(stringResource(R.string.producer_plan_title_emphasis)) }
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
                    TextButton(onRefresh) { Text(stringResource(R.string.producer_plan_retry)) }
                }
                is ProducerPlanUiState.Empty -> {
                    Text(stringResource(R.string.producer_plan_empty), Modifier.padding(vertical = Spacing.lg),
                        style = MaterialTheme.typography.bodyLarge, color = Neutral700)
                    state.error?.let { Text(stringResource(it.messageRes()), color = Terracotta600) }
                    CatalogUpdatedAt(state.fetchedAt)
                    TextButton(onRefresh, enabled = !state.isRefreshing) { Text(stringResource(R.string.producer_plan_retry)) }
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
                    PlanBenefits(state)
                    HorizontalDivider(color = Neutral200)
                    PlanEstimateSummary(state)
                    PlanFreshness(state)
                }
            }
            Spacer(Modifier.height(Spacing.lg))
            TextButton(onRedeemCode, Modifier.fillMaxWidth().heightIn(min = Spacing.minTouchTarget)) {
                Text(stringResource(R.string.producer_plan_cooperative), style = MaterialTheme.typography.titleMedium,
                    color = Neutral900)
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

@Composable
private fun PlanEstimateSummary(state: ProducerPlanUiState.Content) {
    val locale = LocalConfiguration.current.locales[0]
    val estimate = state.estimate
    val currency = state.selectedOffer.currency
    Text(stringResource(R.string.producer_plan_total), Modifier.padding(top = Spacing.lg),
        style = EditorialEyebrow, color = Neutral600)
    Text(formatPlanMoney(estimate.total, currency, locale), Modifier.testTag("producer-plan-total").drawBehind {
        drawLine(Harvest300.copy(alpha = 0.55f), Offset(0f, size.height * 0.82f),
            Offset(size.width, size.height * 0.82f), strokeWidth = size.height * 0.16f)
    }, style = EditorialPrice, color = Neutral900)
    Column(Modifier.padding(top = Spacing.sm), verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
        Text(stringResource(R.string.producer_plan_base, state.selectedOffer.billingMonths,
            formatPlanMoney(estimate.base, currency, locale)), style = MaterialTheme.typography.bodyMedium, color = Neutral700)
        val percent = NumberFormat.getPercentInstance(locale).apply { maximumFractionDigits = 2 }
            .format(state.selectedOffer.taxRate)
        Text(stringResource(R.string.producer_plan_tax, percent, formatPlanMoney(estimate.tax, currency, locale)),
            style = MaterialTheme.typography.bodyMedium, color = Neutral700)
        Text(stringResource(R.string.producer_plan_monthly, formatPlanMoney(estimate.monthlyBase, currency, locale)),
            style = MaterialTheme.typography.bodySmall, color = Neutral700)
        Text(stringResource(R.string.producer_plan_estimate_notice), Modifier.padding(top = Spacing.xs),
            style = MaterialTheme.typography.bodySmall, color = Neutral600)
    }
}

@Composable
private fun PlanFreshness(state: ProducerPlanUiState.Content) {
    CatalogUpdatedAt(state.fetchedAt)
    if (state.isSavedCopy) Text(stringResource(R.string.producer_plan_saved_copy),
        style = MaterialTheme.typography.bodySmall, color = Neutral600)
    if (state.isRefreshing) Text(stringResource(R.string.producer_plan_loading),
        style = MaterialTheme.typography.bodySmall, color = Neutral600)
    state.error?.let { Text(stringResource(it.messageRes()), Modifier.padding(top = Spacing.xs),
        style = MaterialTheme.typography.bodySmall, color = Terracotta600) }
}

@Composable
private fun CatalogUpdatedAt(at: java.time.Instant) {
    val locale = LocalConfiguration.current.locales[0]
    val timestamp = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(locale)
        .withZone(ZoneId.systemDefault()).format(at)
    Text(stringResource(R.string.producer_plan_updated, timestamp), Modifier.padding(top = Spacing.lg),
        style = MaterialTheme.typography.bodySmall, color = Neutral600)
}

internal fun formatPlanCapacity(value: BigDecimal, locale: Locale): String = NumberFormat.getNumberInstance(locale)
    .apply { maximumFractionDigits = value.scale().coerceAtLeast(0) }.format(value)

internal fun formatPlanMoney(value: BigDecimal, currency: String, locale: Locale): String =
    NumberFormat.getCurrencyInstance(locale).apply { this.currency = Currency.getInstance(currency) }.format(value)
