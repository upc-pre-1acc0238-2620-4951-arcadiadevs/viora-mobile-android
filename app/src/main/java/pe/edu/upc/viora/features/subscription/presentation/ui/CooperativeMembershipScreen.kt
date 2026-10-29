package pe.edu.upc.viora.features.subscription.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.PrimaryPillButton
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.presentation.formatDecimal
import pe.edu.upc.viora.core.presentation.formatWhole

/** P06 · Membresía activa: what the redeemed code gives the producer, then on to the first plot. */
@Composable
fun CooperativeMembershipScreen(
    cooperativeName: String,
    quotaHectares: Double,
    onRegisterPlot: () -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            Box(
                modifier = Modifier.size(184.dp).clip(CircleShape).background(Green200),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_isotype),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(104.dp),
                )
            }
            Spacer(Modifier.height(40.dp))
            StatusHeading(
                eyebrow = stringResource(R.string.membership_badge),
                lead = stringResource(R.string.membership_title_lead),
                emphasis = stringResource(R.string.membership_title_emphasis),
                body = stringResource(R.string.membership_body),
            )
            Spacer(Modifier.height(28.dp))
            HorizontalDivider(color = Neutral200)
            MembershipRow(stringResource(R.string.membership_row_cooperative), cooperativeName)
            MembershipRow(stringResource(R.string.membership_row_quota), stringResource(R.string.membership_quota_value, quotaHectares.asHectares()))
            MembershipRow(stringResource(R.string.membership_row_validity), stringResource(R.string.membership_validity_value))
        }
        Column(
            modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            PrimaryPillButton(text = stringResource(R.string.membership_action_register_plot), onClick = onRegisterPlot)
            Text(
                text = stringResource(R.string.membership_action_home),
                style = MaterialTheme.typography.titleMedium,
                color = Neutral900,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onGoHome)
                    .padding(horizontal = 24.dp, vertical = 14.dp),
            )
        }
    }
}

@Composable
internal fun MembershipRow(label: String, value: String) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Neutral600)
            Text(text = value, style = MaterialTheme.typography.titleSmall, color = Neutral900, textAlign = TextAlign.End)
        }
        HorizontalDivider(color = Neutral200)
    }
}

/** 12.0 is shown as "12" and 12.5 as "12,5" (or "12.5"), as the design writes the quota. */
private fun Double.asHectares(): String = if (this % 1.0 == 0.0) formatWhole(this.toLong()) else formatDecimal(this, 1)

/**
 * The centered head of P06 and of P05 · Validando: the eyebrow with its dot, the two-line editorial
 * title and the body under it.
 */
@Composable
internal fun StatusHeading(eyebrow: String, lead: String, emphasis: String, body: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(Green900))
            Text(
                text = eyebrow.uppercase(),
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.4.sp),
                color = Neutral700,
            )
        }
        Spacer(Modifier.height(12.dp))
        val headline = MaterialTheme.typography.displayMedium.copy(fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-1).sp)
        Text(lead, style = headline, color = Neutral900, textAlign = TextAlign.Center)
        Text(
            emphasis,
            style = headline,
            fontStyle = FontStyle.Italic,
            color = Neutral900,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = Neutral700,
            textAlign = TextAlign.Center,
        )
    }
}
