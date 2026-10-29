package pe.edu.upc.viora.features.subscription.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import java.time.Instant
import java.time.format.DateTimeFormatter
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.*
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.presentation.LocalAppZone
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.SubscriptionAccessViewModel

/**
 * Home status block (Figma P10 "Sin acceso activo"). Only the server's effective access decides:
 * without `entitlementActive` the yellow card is shown; with access the Home stays as designed, with nothing added.
 * While loading or on a network failure nothing is shown, so Home never claims a state it has not confirmed.
 */
@Composable
fun SubscriptionHomeSection(
    onRenew: () -> Unit,
    onRedeemCode: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SubscriptionAccessViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(owner) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) { viewModel.refresh() }
    }
    SubscriptionHomeContent(
        result = state,
        onRenew = onRenew,
        onRedeemCode = onRedeemCode,
        modifier = modifier,
    )
}

@Composable
fun SubscriptionHomeContent(
    result: AppResult<pe.edu.upc.viora.features.subscription.domain.entity.ProducerSubscription?>?,
    onRenew: () -> Unit,
    onRedeemCode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (result) {
        is AppResult.Success -> {
            val sub = result.value
            if (sub?.entitlementActive != true) {
                InactiveAccessCard(
                    endsAt = sub?.endsAt,
                    onRenew = onRenew,
                    onRedeemCode = onRedeemCode,
                    modifier = modifier,
                )
            }
        }
        null -> {
            // While loading or uninitialized, nothing is displayed so Home never claims an unconfirmed state
            Box(modifier = modifier.testTag("home_subscription_loading"))
        }
        is AppResult.Failure -> {
            // On network failure or error, nothing is displayed to avoid false claims
            Box(modifier = modifier.testTag("home_subscription_error"))
        }
    }
}

@Composable
fun InactiveAccessCard(
    endsAt: Instant?,
    onRenew: () -> Unit,
    onRedeemCode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = if (endsAt != null) {
        val date = DateTimeFormatter.ofPattern(stringResource(R.string.home_access_date_pattern), androidx.compose.ui.platform.LocalConfiguration.current.locales[0])
            .withZone(LocalAppZone.current).format(endsAt)
        stringResource(R.string.home_access_expired_title, date)
    } else {
        stringResource(R.string.home_access_none_title)
    }
    Column(
        modifier
            .fillMaxWidth()
            .testTag("home_inactive_access_card")
            .clip(RoundedCornerShape(32.dp))
            .background(Harvest100)
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(Neutral0), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_priority_high), contentDescription = null, tint = Neutral900)
            }
            Box(Modifier.clip(RoundedCornerShape(999.dp)).background(Harvest300).padding(horizontal = 12.dp, vertical = 6.dp)) {
                Text(stringResource(R.string.home_access_inactive_chip), color = Neutral900,
                    style = MaterialTheme.typography.labelMedium)
            }
        }
        Text(title, color = Neutral900, style = TextStyle(fontFamily = NewsreaderFamily, fontWeight = FontWeight.Normal,
            fontSize = 30.sp, lineHeight = 34.sp, letterSpacing = (-0.015).em))
        Text(stringResource(R.string.home_access_inactive_body), color = Neutral700,
            style = TextStyle(fontSize = 14.sp, lineHeight = 20.sp))
        Spacer(Modifier.height(4.dp))
        Row(
            Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(Green900)
                .clickable(onClick = onRenew)
                .testTag("home_renew_plan")
                .padding(start = 20.dp, top = 6.dp, end = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Figma: "Renovar" when a plan ended, "Activar" when there never was one.
            Text(stringResource(if (endsAt != null) R.string.home_access_renew else R.string.home_access_activate), color = Neutral50, style = MaterialTheme.typography.labelLarge)
            Box(Modifier.size(36.dp).clip(CircleShape).background(Harvest300), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_arrow_forward), contentDescription = null, tint = Neutral900)
            }
        }
        Text(
            stringResource(R.string.home_access_redeem_code), color = Green800,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.clickable(onClick = onRedeemCode).padding(vertical = 8.dp).testTag("home_redeem_code"),
        )
    }
}
