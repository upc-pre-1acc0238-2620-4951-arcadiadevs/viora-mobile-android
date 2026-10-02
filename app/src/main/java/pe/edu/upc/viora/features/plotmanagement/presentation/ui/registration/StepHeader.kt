package pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Neutral900

private const val TOTAL_STEPS = 3

/**
 * Header shared by the wizard steps: back, "Paso N de 3" with one capsule per step, close.
 * Finished steps are dark, the current one is yellow and wider, the rest are faint.
 */
@Composable
fun StepHeader(step: Int, onBack: () -> Unit, onClose: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleIconButton(
            icon = R.drawable.ic_arrow_back,
            contentDescription = stringResource(R.string.action_back),
            onClick = onBack,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = stringResource(R.string.register_step_counter, step),
                style = MaterialTheme.typography.labelLarge.copy(color = Neutral900),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (1..TOTAL_STEPS).forEach { index ->
                    val color: Color = when {
                        index < step -> Green900
                        index == step -> Harvest300
                        else -> Neutral900.copy(alpha = 0.14f)
                    }
                    Row(
                        modifier = Modifier
                            .width(if (index == step) 40.dp else 22.dp)
                            .height(6.dp)
                            .background(color, RoundedCornerShape(3.dp)),
                    ) {}
                }
            }
        }
        CircleIconButton(
            icon = R.drawable.ic_close,
            contentDescription = stringResource(R.string.action_close),
            onClick = onClose,
        )
    }
}
