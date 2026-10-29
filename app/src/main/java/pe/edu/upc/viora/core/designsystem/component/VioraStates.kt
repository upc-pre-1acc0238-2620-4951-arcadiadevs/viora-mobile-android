package pe.edu.upc.viora.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import pe.edu.upc.viora.core.designsystem.theme.Green900

/**
 * A screen or section that is still loading: the spinner centred in the space [modifier] gives it.
 * The error and empty states stay with each screen, whose designs differ in Figma.
 */
@Composable
fun VioraLoadingState(modifier: Modifier = Modifier, color: Color = Green900) {
    Box(modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = color)
    }
}
