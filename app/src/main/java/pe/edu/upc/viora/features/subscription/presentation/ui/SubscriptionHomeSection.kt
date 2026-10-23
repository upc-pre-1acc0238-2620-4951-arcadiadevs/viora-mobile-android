package pe.edu.upc.viora.features.subscription.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.*
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.subscription.presentation.viewmodel.SubscriptionAccessViewModel

/**
 * Home status block (Figma P10 "Acceso inactivo"). Only the server's effective access decides:
 * without `entitlementActive` the yellow card is shown; with access, a discreet link to "Mi suscripción".
 * While loading or on a network failure nothing is shown, so Home never claims a state it has not confirmed.
 */
@Composable
fun SubscriptionHomeSection(
    onRenew: () -> Unit,
    onRedeemCode: () -> Unit,
    onOpenSubscription: () -> Unit,
    viewModel: SubscriptionAccessViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val owner = LocalLifecycleOwner.current
    LaunchedEffect(owner) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) { viewModel.refresh() }
    }
    val result = state as? AppResult.Success ?: return
    val sub = result.value
    if (sub?.entitlementActive == true) {
        TextButton(onClick = onOpenSubscription, modifier = Modifier.testTag("home_subscription_link")) {
            Text(stringResource(R.string.subscription_access_title), color = Green800)
        }
    } else {
        InactiveAccessCard(endsAt = sub?.endsAt, onRenew = onRenew, onRedeemCode = onRedeemCode)
    }
}

@Composable
fun InactiveAccessCard(endsAt: Instant?, onRenew: () -> Unit, onRedeemCode: () -> Unit) {
    val title = if (endsAt != null) {
        val date = DateTimeFormatter.ofPattern(stringResource(R.string.home_access_date_pattern), Locale.getDefault())
            .withZone(ZoneId.systemDefault()).format(endsAt)
        stringResource(R.string.home_access_expired_title, date)
    } else {
        stringResource(R.string.home_access_none_title)
    }
    Column(
        Modifier
            .fillMaxWidth()
            .testTag("home_inactive_access_card")
            .clip(RoundedCornerShape(32.dp))
            .background(Harvest100)
            .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(Neutral0), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_priority_high), contentDescription = null, tint = Terracotta600)
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
            Text(stringResource(R.string.home_access_renew), color = Neutral50, style = MaterialTheme.typography.labelLarge)
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
