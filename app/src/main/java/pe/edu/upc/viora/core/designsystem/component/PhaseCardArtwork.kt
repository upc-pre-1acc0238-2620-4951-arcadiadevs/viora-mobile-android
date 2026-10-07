package pe.edu.upc.viora.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pe.edu.upc.viora.R

/**
 * The line drawing of each olive phase in the Home phase card (Figma P10 · Inicio, "Carrusel de
 * fases · Ahora"). [endInset] and [topOffset] place it as in the 296 dp wide Figma card, measured
 * from the card's top-end corner.
 */
enum class PhaseIllustration(
    @DrawableRes val drawable: Int,
    val width: Dp,
    val height: Dp,
    val endInset: Dp,
    val topOffset: Dp,
) {
    DORMANCY(R.drawable.ill_phase_dormancy, 130.dp, 115.56.dp, 6.dp, (-14).dp),
    BLOOM(R.drawable.ill_phase_bloom, 130.dp, 115.56.dp, 6.dp, (-14).dp),
    FRUIT_SET(R.drawable.ill_phase_fruit_set, 130.dp, 115.56.dp, 6.dp, (-14).dp),
    THINNING(R.drawable.ill_phase_thinning, 122.4.dp, 108.8.dp, 3.6.dp, (-22).dp),
    HARVEST(R.drawable.ill_phase_harvest, 130.dp, 115.56.dp, 6.dp, (-14).dp),
}

/**
 * The organic blob and the phase drawing in the top-end corner of a dark phase card. Call it first
 * inside the card's `Box` (the card must clip its shape) so the texts are drawn over it.
 */
@Composable
fun BoxScope.PhaseCardArtwork(illustration: PhaseIllustration) {
    Image(
        painter = painterResource(R.drawable.ill_phase_blob),
        contentDescription = null,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = BlobOverflowEnd, y = BlobTop)
            .size(width = 240.dp, height = 210.dp),
    )
    Image(
        painter = painterResource(illustration.drawable),
        contentDescription = null,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = -illustration.endInset, y = illustration.topOffset)
            .size(width = illustration.width, height = illustration.height),
    )
}

/** In Figma the blob starts at x 130 of the 296 dp card and is 240 dp wide: 74 dp past the end. */
private val BlobOverflowEnd = 74.dp
private val BlobTop = (-80).dp
