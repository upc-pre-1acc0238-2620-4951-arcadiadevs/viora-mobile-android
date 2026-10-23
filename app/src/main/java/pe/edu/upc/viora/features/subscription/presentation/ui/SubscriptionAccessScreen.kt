package pe.edu.upc.viora.features.subscription.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Spacing
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.SubscriptionAccessViewModel

@kotlinx.serialization.Serializable
data object SubscriptionAccessRoute

@Composable
fun SubscriptionAccessScreen(onBack: () -> Unit, onPlan: () -> Unit,
    viewModel: SubscriptionAccessViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val owner = LocalLifecycleOwner.current
    var retry by remember { mutableIntStateOf(0) }
    LaunchedEffect(owner, retry) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) { viewModel.refresh() }
    }
    Column(Modifier.fillMaxSize().systemBarsPadding().padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        TextButton(onBack) { Text(stringResource(R.string.action_back)) }
        Text(stringResource(R.string.subscription_access_title), style = MaterialTheme.typography.headlineMedium)
        when (val result = state) {
            null -> CircularProgressIndicator()
            is AppResult.Failure -> {
                Text(stringResource(result.error.messageRes()))
                TextButton({ retry++ }) { Text(stringResource(R.string.purchase_check_status)) }
            }
            is AppResult.Success -> {
                val sub = result.value
                Text(stringResource(if (sub?.entitlementActive == true) R.string.purchase_confirmed_title
                    else R.string.subscription_access_inactive))
                if (sub != null) {
                    Text(stringResource(R.string.subscription_access_quota, sub.capacity.hectares.toPlainString()))
                    Text(stringResource(R.string.subscription_access_mode, stringResource(when (sub.mode) {
                        pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionMode.INDIVIDUAL -> R.string.subscription_mode_individual
                        pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionMode.COOPERATIVE_SPONSORED -> R.string.subscription_mode_cooperative
                        else -> R.string.subscription_mode_unknown
                    })))
                    sub.endsAt?.let { Text(stringResource(R.string.subscription_access_until,
                        java.time.format.DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM)
                            .withZone(java.time.ZoneId.systemDefault()).format(it))) }
                }
                if (sub == null || sub.status == pe.edu.upc.viora.features.subscription.domain.valueobject.SubscriptionStatus.PENDING_PAYMENT) {
                    Button(onPlan) { Text(stringResource(R.string.subscription_access_plan)) }
                }
                TextButton({ retry++ }) { Text(stringResource(R.string.purchase_check_status)) }
            }
        }
    }
}
