package pe.edu.upc.viora.features.croploadregulation.presentation.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pe.edu.upc.viora.core.designsystem.theme.Harvest100

/**
 * Segmented progress bar showing the progress towards statistical representativeness (e.g. 3 of 5 trees).
 */
@Composable
fun SamplingSegmentedBar(
    completedCount: Int,
    totalCount: Int = 5,
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    activeColor: Color = Harvest100,
    inactiveColor: Color = Color(0x3DF9F6F1),
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val total = totalCount.coerceAtLeast(1)
        val active = completedCount.coerceIn(0, total)
        for (i in 0 until total) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(height)
                    .clip(RoundedCornerShape(height / 2))
                    .background(if (i < active) activeColor else inactiveColor),
            )
        }
    }
}
