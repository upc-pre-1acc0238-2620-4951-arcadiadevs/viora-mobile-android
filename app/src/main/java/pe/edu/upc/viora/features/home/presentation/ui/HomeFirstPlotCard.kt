package pe.edu.upc.viora.features.home.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.VioraTheme

/**
 * Empty state of the Home (Figma "Estado vacío · primer lote"): invites the producer to draw
 * the first plot on the map.
 */
@Composable
fun HomeFirstPlotCard(onRegisterPlot: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Green200)
            .padding(start = 22.dp, end = 22.dp, bottom = 22.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        FirstPlotIllustration(modifier = Modifier.fillMaxWidth().aspectRatio(ILLUSTRATION_WIDTH / ILLUSTRATION_HEIGHT))
        Text(
            text = stringResource(R.string.home_first_step),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.54.sp),
            color = Green800,
        )
        Column {
            val title = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp, lineHeight = 36.sp, letterSpacing = (-0.64).sp)
            Text(text = stringResource(R.string.home_first_plot_title_lead), style = title, color = Neutral900)
            Text(text = stringResource(R.string.home_first_plot_title_emphasis), style = title, fontStyle = FontStyle.Italic, color = Neutral900)
        }
        Text(
            text = stringResource(R.string.home_first_plot_body),
            style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp),
            color = Neutral700,
        )
        Row(
            modifier = Modifier
                .padding(top = 6.dp)
                .clip(CircleShape)
                .background(Green900)
                .clickable(role = Role.Button, onClick = onRegisterPlot)
                .padding(start = 20.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.home_first_plot_action),
                style = MaterialTheme.typography.labelLarge,
                color = Neutral50,
            )
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(Harvest300),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painter = painterResource(R.drawable.ic_arrow_forward), contentDescription = null, tint = Neutral900)
            }
        }
    }
}

private const val ILLUSTRATION_WIDTH = 310f
private const val ILLUSTRATION_HEIGHT = 170f

/** One vector of the Figma illustration: its path data placed at ([x], [y]) of the 310 x 170 frame. */
private class IllustrationPiece(
    val pathData: String,
    val x: Float,
    val y: Float,
    val draw: DrawScope.(Path) -> Unit,
)

private val ContourColor = Color(0xFFB7C0BA)
private val LotFill = Color(0xFFF4F8F5)

// The geometry below is the Figma illustration (frame 474:212523), path data and offsets copied
// from its exported vectors. They are drawn here instead of as a VectorDrawable because the
// plot outline is dashed and vector drawables cannot dash a stroke.
private val Pieces = listOf(
    IllustrationPiece(
        "M0.126276 15.9125C61.4257 -0.0875275 113.968 31.9125 175.268 15.9125C236.567 -0.0875275 271.595 -6.08753 332.895 9.91247",
        x = -8.74f, y = 30.08f,
    ) { drawPath(it, ContourColor, style = Stroke(width = 1f)) },
    IllustrationPiece(
        "M0.097982 12.4683C70.1545 -1.5317 122.697 28.4683 192.753 14.4683C262.81 0.468298 280.324 -5.5317 332.866 8.4683",
        x = -8.74f, y = 119.54f,
    ) { drawPath(it, ContourColor, style = Stroke(width = 1f)) },
    IllustrationPiece(
        "M0.900004 12.9001L116.493 0.900054L135.759 94.9001L13.1599 110.9L0.900004 12.9001Z",
        x = 90.18f, y = 27.1f,
    ) {
        drawPath(it, LotFill)
        drawPath(it, Green900, style = Stroke(width = 1.8f, join = StrokeJoin.Round, pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 6f))))
    },
    IllustrationPiece(
        "M6.15424 12.9C9.05607 12.9 11.4085 10.2137 11.4085 6.9C11.4085 3.58629 9.05607 0.9 6.15424 0.9C3.2524 0.9 0.9 3.58629 0.9 6.9C0.9 10.2137 3.2524 12.9 6.15424 12.9Z",
        x = 84.91f, y = 33.1f,
    ) {
        drawPath(it, Neutral50)
        drawPath(it, Green900, style = Stroke(width = 1.8f))
    },
    IllustrationPiece(
        "M6.15424 12.9C9.05607 12.9 11.4085 10.2137 11.4085 6.9C11.4085 3.58629 9.05607 0.9 6.15424 0.9C3.2524 0.9 0.9 3.58629 0.9 6.9C0.9 10.2137 3.2524 12.9 6.15424 12.9Z",
        x = 200.5f, y = 21.1f,
    ) {
        drawPath(it, Neutral50)
        drawPath(it, Green900, style = Stroke(width = 1.8f))
    },
    IllustrationPiece(
        "M5.25424 12C8.15607 12 10.5085 9.31371 10.5085 6C10.5085 2.68629 8.15607 0 5.25424 0C2.3524 0 0 2.68629 0 6C0 9.31371 2.3524 12 5.25424 12Z",
        x = 220.69f, y = 116f,
    ) { drawPath(it, Green900) },
    IllustrationPiece(
        "M19.2655 44C29.9056 44 38.5311 34.1503 38.5311 22C38.5311 9.84974 29.9056 0 19.2655 0C8.62547 0 0 9.84974 0 22C0 34.1503 8.62547 44 19.2655 44Z",
        x = 138.35f, y = 60f,
    ) { drawPath(it, Harvest300) },
    IllustrationPiece(
        "M9.85706 1.1V21.1M1.1 11.1H18.6141",
        x = 147.76f, y = 70.9f,
    ) { drawPath(it, Neutral900, style = Stroke(width = 2.2f, cap = StrokeCap.Round)) },
)

@Composable
private fun FirstPlotIllustration(modifier: Modifier = Modifier) {
    val paths = remember { Pieces.map { PathParser().parsePathString(it.pathData).toPath() } }
    Canvas(modifier = modifier.clipToBounds()) {
        val scale = size.width / ILLUSTRATION_WIDTH
        Pieces.forEachIndexed { index, piece ->
            withTransform({
                scale(scale, scale, pivot = Offset.Zero)
                translate(piece.x, piece.y)
            }) { piece.draw(this, paths[index]) }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFFF9F6F1, widthDp = 402)
@Composable
private fun HomeFirstPlotCardPreview() {
    VioraTheme { HomeFirstPlotCard(onRegisterPlot = {}, modifier = Modifier.padding(24.dp)) }
}
