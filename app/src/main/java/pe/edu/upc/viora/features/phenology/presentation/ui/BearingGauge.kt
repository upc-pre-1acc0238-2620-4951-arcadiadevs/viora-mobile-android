package pe.edu.upc.viora.features.phenology.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.features.phenology.domain.entity.BbiClass

/** Colour of a class in the gauge, the legend and the explainer. */
fun BbiClass.color(): Color = when (this) {
    BbiClass.LOW -> Green200
    BbiClass.MODERATE -> Harvest300
    BbiClass.SEVERE -> Terracotta500
}

private const val LOW_END = 0.20f
private const val MODERATE_END = 0.40f

/**
 * The dark index card of the alternation screen (Figma P40): a semicircle from 0 to 1 filled up
 * to [index], the index itself, its class and the scale with the three classes.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BearingGaugeCard(
    index: Double,
    bbiClass: BbiClass,
    onWhatIs: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val indexText = formatIndex(index)
    val classText = stringResource(bbiClass.sentenceRes())
    val description = stringResource(R.string.harvest_gauge_description, indexText, classText)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Green900)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.harvest_bbi_eyebrow),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.88.sp),
                color = Green200,
                modifier = Modifier.weight(1f, fill = false),
            )
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Neutral0.copy(alpha = 0.12f))
                    .clickable(role = Role.Button, onClick = onWhatIs)
                    .padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_info), contentDescription = null, tint = Neutral50, modifier = Modifier.size(14.dp))
                Text(stringResource(R.string.harvest_what_is), style = MaterialTheme.typography.labelSmall, color = Neutral50)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(GAUGE_HEIGHT)
                .semantics(mergeDescendants = true) { contentDescription = description },
        ) {
            Canvas(Modifier.fillMaxWidth().height(GAUGE_HEIGHT)) { drawGauge(index.toFloat().coerceIn(0f, 1f), bbiClass) }
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = indexText,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp, lineHeight = 66.sp, letterSpacing = (-2).sp),
                    color = Neutral0,
                )
                Text(
                    text = classText,
                    style = MaterialTheme.typography.headlineSmall.copy(fontStyle = FontStyle.Italic),
                    color = Terracotta100,
                )
            }
            Text(
                text = "0",
                style = MaterialTheme.typography.labelSmall,
                color = Green200,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 12.dp),
            )
            Text(
                text = "1",
                style = MaterialTheme.typography.labelSmall,
                color = Green200,
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 12.dp),
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            BbiClass.entries.forEach { entry -> ScaleChip(entry, active = entry == bbiClass) }
        }
    }
}

@Composable
private fun ScaleChip(bbiClass: BbiClass, active: Boolean) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (active) Neutral0 else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(bbiClass.color()))
        Text(
            text = stringResource(bbiClass.labelRes()),
            style = MaterialTheme.typography.labelSmall,
            color = if (active) Neutral900 else Neutral0.copy(alpha = 0.8f),
        )
        Text(
            text = stringResource(bbiClass.rangeRes()),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = if (active) Neutral900.copy(alpha = 0.6f) else Neutral0.copy(alpha = 0.55f),
        )
    }
}

private val GAUGE_HEIGHT = 184.dp

private fun DrawScope.drawGauge(value: Float, bbiClass: BbiClass) {
    val strokeWidth = 14.dp.toPx()
    val tickGap = 6.dp.toPx()
    val tickLength = 6.dp.toPx()
    val radius = size.width / 2 - strokeWidth / 2 - tickGap - tickLength - 4.dp.toPx()
    val center = Offset(size.width / 2, radius + strokeWidth / 2 + tickGap + tickLength + 3.dp.toPx())
    val topLeft = Offset(center.x - radius, center.y - radius)
    val arcSize = Size(radius * 2, radius * 2)

    // Scale marks every 0.05, longer at 0 and 1.
    for (step in 0..20) {
        val angle = Math.PI * (1 + step / 20.0)
        val inner = radius + strokeWidth / 2 + tickGap
        val outer = inner + if (step % 4 == 0) tickLength else tickLength * 0.6f
        drawLine(
            color = Neutral0.copy(alpha = 0.3f),
            start = Offset(center.x + (inner * cos(angle)).toFloat(), center.y + (inner * sin(angle)).toFloat()),
            end = Offset(center.x + (outer * cos(angle)).toFloat(), center.y + (outer * sin(angle)).toFloat()),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }

    drawArc(
        color = Neutral0.copy(alpha = 0.14f),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = topLeft,
        size = arcSize,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
    )
    val segments = listOf(
        Triple(0f, LOW_END, BbiClass.LOW),
        Triple(LOW_END, MODERATE_END, BbiClass.MODERATE),
        Triple(MODERATE_END, 1f, BbiClass.SEVERE),
    )
    segments.forEach { (from, to, segmentClass) ->
        val end = minOf(value, to)
        if (end > from) {
            drawArc(
                color = segmentClass.color(),
                startAngle = 180f + 180f * from,
                sweepAngle = 180f * (end - from),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth),
            )
        }
    }

    val angle = Math.PI * (1 + value)
    val dot = Offset(center.x + (radius * cos(angle)).toFloat(), center.y + (radius * sin(angle)).toFloat())
    drawCircle(Neutral0, radius = 11.dp.toPx(), center = dot)
    drawCircle(bbiClass.color(), radius = 5.dp.toPx(), center = dot)
}
