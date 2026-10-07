package pe.edu.upc.viora.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.core.designsystem.theme.Neutral900

/**
 * The two-line editorial title of every screen: a lead and an italic emphasis ("Tus *sensores.*",
 * "El clima de *tu lote.*"), Newsreader 44/46 as in Figma.
 */
@Composable
fun EditorialHeadline(lead: String, emphasis: String, modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.displayMedium.copy(fontSize = 44.sp, lineHeight = 46.sp, letterSpacing = (-1.1).sp)
    Column(modifier = modifier) {
        Text(text = lead, style = style, color = Neutral900)
        Text(text = emphasis, style = style, fontStyle = FontStyle.Italic, color = Neutral900)
    }
}
