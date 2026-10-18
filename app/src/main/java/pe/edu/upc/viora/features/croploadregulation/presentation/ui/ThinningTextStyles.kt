package pe.edu.upc.viora.features.croploadregulation.presentation.ui

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.RobotoFamily

/** The exact Figma sizes of the thinning screens (P61–P64), which the Material type scale does not cover. */
internal fun roboto(size: TextUnit, lineHeight: TextUnit) =
    TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Normal, fontSize = size, lineHeight = lineHeight)

internal fun robotoMedium(size: TextUnit, lineHeight: TextUnit) =
    TextStyle(fontFamily = RobotoFamily, fontWeight = FontWeight.Medium, fontSize = size, lineHeight = lineHeight)

internal fun newsreader(size: TextUnit, lineHeight: TextUnit) =
    TextStyle(fontFamily = NewsreaderFamily, fontWeight = FontWeight.Normal, fontSize = size, lineHeight = lineHeight)
