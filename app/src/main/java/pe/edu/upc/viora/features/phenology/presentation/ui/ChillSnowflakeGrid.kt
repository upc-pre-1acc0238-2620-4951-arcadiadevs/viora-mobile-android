package pe.edu.upc.viora.features.phenology.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.features.phenology.domain.entity.WinterSeasonState

/**
 * 30-snowflake grid representing the 30 required Erez chill portions.
 * Adapts colors to the winter season state (normal, halted, completed).
 */
@Composable
fun ChillSnowflakeGrid(
    accumulated: Int,
    threshold: Int = 30,
    state: WinterSeasonState = WinterSeasonState.ACCUMULATING,
    modifier: Modifier = Modifier,
) {
    val activeColor = when (state) {
        WinterSeasonState.CHILL_HALTED -> Terracotta500
        WinterSeasonState.COMPLETED -> Green800
        else -> Harvest800
    }

    val activeBg = when (state) {
        WinterSeasonState.CHILL_HALTED -> Terracotta100
        WinterSeasonState.COMPLETED -> Harvest100
        else -> Harvest100
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Neutral0)
            .border(1.dp, Neutral200, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.winter_chill_grid_title),
                style = MaterialTheme.typography.titleMedium,
                color = Neutral700,
            )
            Text(
                text = stringResource(R.string.winter_chill_grid_counter, accumulated, threshold),
                style = MaterialTheme.typography.labelLarge,
                color = activeColor,
            )
        }

        // 3 rows of 10 snowflakes = 30 portions
        val totalSnowflakes = threshold.coerceAtLeast(30)
        val columnsPerRow = 10
        val rows = (totalSnowflakes + columnsPerRow - 1) / columnsPerRow

        for (rowIndex in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                for (colIndex in 0 until columnsPerRow) {
                    val portionIndex = rowIndex * columnsPerRow + colIndex
                    if (portionIndex < totalSnowflakes) {
                        val isFilled = portionIndex < accumulated
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) activeBg else Neutral0)
                                .border(
                                    width = 1.dp,
                                    color = if (isFilled) activeColor.copy(alpha = 0.3f) else Neutral200,
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_ac_unit),
                                contentDescription = null,
                                tint = if (isFilled) activeColor else Neutral300,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
