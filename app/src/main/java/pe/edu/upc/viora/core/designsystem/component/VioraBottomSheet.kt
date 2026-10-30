package pe.edu.upc.viora.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pe.edu.upc.viora.core.designsystem.theme.Neutral300

/**
 * The app's bottom sheet (Figma "Hoja"): 32 dp top corners, the sand surface and a 36 × 4 dp handle.
 * It opens fully by default; pass a [sheetState] that allows the half-open stop where the design has one.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VioraBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        containerColor = containerColor,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { SheetHandle() },
        content = content,
    )
}

@Composable
private fun SheetHandle() {
    Box(Modifier.padding(top = 12.dp).size(width = 36.dp, height = 4.dp).clip(CircleShape).background(Neutral300))
}
