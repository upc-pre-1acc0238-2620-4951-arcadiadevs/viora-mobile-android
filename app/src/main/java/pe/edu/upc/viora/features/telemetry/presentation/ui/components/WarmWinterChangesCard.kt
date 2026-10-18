package pe.edu.upc.viora.features.telemetry.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral500
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.RobotoFamily
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.features.telemetry.presentation.state.ChillFact

/**
 * "Qué cambia" of T15 · Invierno cálido: what the warm spell means for the chill, the bloom and the thinning plan.
 * Facts, not tasks: the warm winter alert has nothing to tick, its way out is "Ver mi frío".
 */
@Composable
fun WarmWinterChangesCard(chillFact: ChillFact, warmThreshold: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Neutral0)
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = stringResource(R.string.warm_winter_changes_title),
            fontFamily = NewsreaderFamily,
            fontSize = 24.sp,
            lineHeight = 30.sp,
            letterSpacing = (-0.24).sp,
            color = Neutral900,
        )
        val (chillLead, chillBody) = when (chillFact) {
            is ChillFact.Halted -> stringResource(R.string.warm_winter_change_chill_halted_lead) to
                pluralStringResource(R.plurals.warm_winter_change_chill_halted, chillFact.days, chillFact.days, chillFact.portions, chillFact.threshold)
            is ChillFact.AtRisk -> stringResource(R.string.warm_winter_change_chill_risk_lead) to
                pluralStringResource(
                    R.plurals.warm_winter_change_chill_risk, chillFact.days, chillFact.days, warmThreshold, chillFact.portions, chillFact.threshold,
                )
            is ChillFact.Unknown -> stringResource(R.string.warm_winter_change_chill_risk_lead) to if (chillFact.days > 0) {
                pluralStringResource(R.plurals.warm_winter_change_chill_unknown, chillFact.days, chillFact.days, warmThreshold)
            } else {
                stringResource(R.string.warm_winter_change_chill_generic)
            }
        }
        Change(Terracotta500, chillLead, chillBody)
        Change(Terracotta500, stringResource(R.string.warm_winter_change_bloom_lead), stringResource(R.string.warm_winter_change_bloom))
        Change(Neutral500, stringResource(R.string.warm_winter_change_thinning_lead), stringResource(R.string.warm_winter_change_thinning))
    }
}

/** One line: the dot, the bold lead and the sentence after it. */
@Composable
private fun Change(dot: Color, lead: String, body: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        // Centred on the first 20 sp line of text.
        Box(Modifier.padding(top = 6.dp).size(8.dp).clip(CircleShape).background(dot))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Medium)) { append(lead) }
                append(" ")
                append(body)
            },
            fontFamily = RobotoFamily,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            color = Neutral900,
            modifier = Modifier.weight(1f),
        )
    }
}
