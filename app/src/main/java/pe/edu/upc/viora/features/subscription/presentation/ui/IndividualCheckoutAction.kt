package pe.edu.upc.viora.features.subscription.presentation.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.*
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.subscription.domain.entity.IndividualCheckout
import pe.edu.upc.viora.features.subscription.presentation.state.IndividualCheckoutUiState
import pe.edu.upc.viora.features.subscription.presentation.state.ProducerPlanUiState

@Composable
internal fun IndividualCheckoutAction(
    offer: ProducerPlanUiState.Content,
    state: IndividualCheckoutUiState,
    onStart: () -> Unit,
    onPay: () -> Unit,
    onDismiss: () -> Unit,
    onCheckStatus: () -> Unit,
    onOpen: (IndividualCheckout, String) -> Unit,
) {
    Button(
        onStart,
        enabled = state !is IndividualCheckoutUiState.Working && state !is IndividualCheckoutUiState.Verifying,
        modifier = Modifier.fillMaxWidth().padding(top = Spacing.xl).heightIn(min = Spacing.xxl + Spacing.xs),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = Green900, contentColor = Neutral50)
    ) {
        Text(
            stringResource(if (state is IndividualCheckoutUiState.Working || state is IndividualCheckoutUiState.Verifying) R.string.purchase_working else R.string.purchase_pay),
            style = MaterialTheme.typography.titleMedium
        )
    }
    when (state) {
        is IndividualCheckoutUiState.Confirm -> {
            val locale = LocalConfiguration.current.locales[0]
            val purchase = state.purchase
            val title = stringResource(R.string.purchase_confirm_title)
            val terms = stringResource(
                R.string.purchase_confirm_terms,
                formatPlanCapacity(purchase.capacity.hectares, locale),
                formatPlanMoney(purchase.total, purchase.currency, locale)
            )
            val changed = stringResource(R.string.purchase_confirm_changed)
            val continueLabel = stringResource(R.string.purchase_continue)
            val laterLabel = stringResource(R.string.purchase_later)
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(title) },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Text(terms)
                        if (purchase.planCode != offer.selectedOffer.code || purchase.capacity != offer.capacity ||
                            purchase.currency != offer.selectedOffer.currency || purchase.total.compareTo(offer.estimate.total) != 0) {
                            Text(changed)
                        }
                    }
                },
                confirmButton = { TextButton(onPay) { Text(continueLabel) } },
                dismissButton = { TextButton(onDismiss) { Text(laterLabel) } }
            )
        }
        is IndividualCheckoutUiState.Ready -> LaunchedEffect(state.link) {
            onOpen(state.link, state.subscriptionId)
        }
        is IndividualCheckoutUiState.Verifying -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text(stringResource(R.string.purchase_verifying)) },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        CircularProgressIndicator(
                            color = Green900,
                            modifier = Modifier.size(Spacing.xl)
                        )
                        Text(stringResource(R.string.purchase_verifying_hint), style = MaterialTheme.typography.bodyMedium)
                    }
                },
                confirmButton = {},
            )
        }
        is IndividualCheckoutUiState.Confirmed -> {
            val locale = LocalConfiguration.current.locales[0]
            val capacityFormatted = formatPlanCapacity(state.subscription.capacity.hectares, locale)
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.purchase_confirmed_title)) },
                text = {
                    Text(
                        stringResource(R.string.purchase_confirmed_message, capacityFormatted),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.purchase_close))
                    }
                }
            )
        }
        is IndividualCheckoutUiState.Unconfirmed -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text(stringResource(R.string.purchase_unconfirmed_title)) },
                text = {
                    Text(
                        stringResource(R.string.purchase_unconfirmed_message),
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(onClick = onCheckStatus) {
                        Text(stringResource(R.string.purchase_check_status))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.purchase_close))
                    }
                }
            )
        }
        is IndividualCheckoutUiState.Failed -> Text(
            stringResource(state.error.messageRes()),
            Modifier.padding(top = Spacing.sm),
            color = Terracotta600,
            style = MaterialTheme.typography.bodyMedium
        )
        IndividualCheckoutUiState.NotPending -> Text(
            stringResource(R.string.purchase_not_pending),
            Modifier.padding(top = Spacing.sm),
            style = MaterialTheme.typography.bodyMedium
        )
        else -> Unit
    }
}

@Composable
internal fun rememberCheckoutBrowser(
    onOpened: (String) -> Unit,
    onFailed: () -> Unit,
): (IndividualCheckout, String) -> Unit {
    val context = LocalContext.current
    return { checkout, subscriptionId ->
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(checkout.url)).addCategory(Intent.CATEGORY_BROWSABLE))
            onOpened(subscriptionId)
        } catch (_: ActivityNotFoundException) {
            onFailed()
        } catch (_: SecurityException) {
            onFailed()
        }
    }
}
