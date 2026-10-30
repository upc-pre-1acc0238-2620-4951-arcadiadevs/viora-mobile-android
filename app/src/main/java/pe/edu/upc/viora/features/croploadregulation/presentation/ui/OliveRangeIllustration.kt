package pe.edu.upc.viora.features.croploadregulation.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest700
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.newsreader
import pe.edu.upc.viora.core.designsystem.theme.robotoMedium

/**
 * Figma "Ilustración · aceitunas (rango)" (P63, 310 × 96): a small white olive, a dashed arrow and, on its right,
 * the big dark olive of the projection or, without a figure, a dashed outline with a "?" in [unknownColor].
 * [arrowLabel] sits above the arrow ("rango 80 %", "aprendiendo", "sin datos"). It scales with the card width.
 */
@Composable
internal fun OliveRangeIllustration(arrowLabel: String, labelColor: Color, unknownColor: Color?, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxWidth().height(ILLUSTRATION_HEIGHT)) {
        val scale = maxWidth / ILLUSTRATION_WIDTH
        Canvas(Modifier.width(ILLUSTRATION_WIDTH * scale).height(ILLUSTRATION_HEIGHT).align(Alignment.Center)) {
            val unit = size.width / ILLUSTRATION_WIDTH.value
            val dy = (size.height - ILLUSTRATION_HEIGHT.value * unit) / 2
            fun p(x: Float, y: Float) = Offset(x * unit, y * unit + dy)
            drawSmallOlive(::p, unit)
            drawDashedArrow(::p, unit)
            if (unknownColor == null) drawBigOlive(::p, unit) else drawUnknownOlive(::p, unit, unknownColor)
        }
        // The label is centred over the arrow (x 100–172), 14 sp, 30 dp from the top.
        CenteredLabel(arrowLabel, labelColor, centerX = 136.dp * scale, top = 30.dp, width = 120.dp)
        if (unknownColor != null) {
            Text(
                "?",
                style = newsreader(36.sp, 44.sp),
                color = unknownColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(40.dp).offset(x = 240.dp * scale - 20.dp, y = 28.dp),
            )
        }
    }
}

@Composable
private fun CenteredLabel(text: String, color: Color, centerX: Dp, top: Dp, width: Dp) {
    Box(Modifier.width(width).offset(x = centerX - width / 2, y = top), contentAlignment = Alignment.Center) {
        Text(text, style = robotoMedium(11.sp, 14.sp), color = color, maxLines = 1)
    }
}

private fun DrawScope.drawSmallOlive(p: (Float, Float) -> Offset, unit: Float) {
    val topLeft = p(30f, 31f)
    val size = Size(32f * unit, 42f * unit)
    drawOval(Neutral0, topLeft, size)
    drawOval(Harvest700, topLeft, size, style = Stroke(width = 1.6f * unit))
    val highlight = Path().apply {
        moveTo(p(40f, 38f).x, p(40f, 38f).y)
        cubicTo(p(42f, 35.33f).x, p(42f, 35.33f).y, p(44.67f, 34f).x, p(44.67f, 34f).y, p(48f, 34f).x, p(48f, 34f).y)
    }
    drawPath(highlight, Harvest700, style = Stroke(width = 1.4f * unit, cap = StrokeCap.Round))
}

/** The dashed line ends in a chevron, as in the Figma vectors (dash 3.9, gap 4.9). */
private fun DrawScope.drawDashedArrow(p: (Float, Float) -> Offset, unit: Float) {
    drawLine(
        Green900,
        p(101f, 52f),
        p(169f, 52f),
        strokeWidth = 2f * unit,
        cap = StrokeCap.Round,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.9f * unit, 4.86f * unit)),
    )
    val head = Path().apply {
        moveTo(p(164f, 46f).x, p(164f, 46f).y)
        lineTo(p(172f, 52f).x, p(172f, 52f).y)
        lineTo(p(164f, 58f).x, p(164f, 58f).y)
    }
    drawPath(head, Green900, style = Stroke(width = 2f * unit, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private fun DrawScope.drawBigOlive(p: (Float, Float) -> Offset, unit: Float) {
    drawOval(Green800, p(213f, 15f), Size(54f * unit, 70f * unit))
    val highlight = Path().apply {
        moveTo(p(230f, 26f).x, p(230f, 26f).y)
        cubicTo(p(233.33f, 22f).x, p(233.33f, 22f).y, p(237.67f, 20f).x, p(237.67f, 20f).y, p(243f, 20f).x, p(243f, 20f).y)
    }
    drawPath(highlight, Neutral50, style = Stroke(width = 1.8f * unit, cap = StrokeCap.Round))
}

/** The olive that has no figure yet: only its dashed outline. */
private fun DrawScope.drawUnknownOlive(p: (Float, Float) -> Offset, unit: Float, color: Color) {
    drawOval(
        color,
        p(213f, 15f),
        Size(54f * unit, 70f * unit),
        style = Stroke(width = 1.6f * unit, pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f * unit, 4f * unit))),
    )
}

private val ILLUSTRATION_WIDTH = 310.dp
private val ILLUSTRATION_HEIGHT = 96.dp
