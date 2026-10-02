package pe.edu.upc.viora.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R

/**
 * Brand serif for expressive moments (Display and Headline roles). Newsreader ships as a
 * variable font, so each weight is declared as a variation of the same file (needs API 26+).
 */
@OptIn(ExperimentalTextApi::class)
val NewsreaderFamily = FontFamily(
    listOf(
        FontWeight.Normal,
        FontWeight.Medium,
        FontWeight.SemiBold,
    ).flatMap { weight ->
        listOf(
            Font(
                resId = R.font.newsreader_variable,
                weight = weight,
                style = FontStyle.Normal,
                variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
            ),
            Font(
                resId = R.font.newsreader_italic_variable,
                weight = weight,
                style = FontStyle.Italic,
                variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
            ),
        )
    },
)

/** Interface font for every functional text (Title, Body and Label roles). */
@OptIn(ExperimentalTextApi::class)
val RobotoFamily = FontFamily(
    listOf(
        FontWeight.Normal,
        FontWeight.Medium,
        FontWeight.Bold,
    ).map { weight ->
        Font(
            resId = R.font.roboto_variable,
            weight = weight,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(weight.weight),
                FontVariation.width(100f),
            ),
        )
    },
)

private val base = Typography()

/**
 * Material 3 scale adapted to Viora: Newsreader for Display/Headline, Roboto for the rest.
 * Display sizes are reduced (48/40/32) because Newsreader is wider than Roboto on compact
 * screens. Body Large (16 sp) is the default reading size; nothing goes below 11 sp.
 */
val AppTypography = Typography(
    displayLarge = base.displayLarge.copy(
        fontFamily = NewsreaderFamily, fontWeight = FontWeight.Normal,
        fontSize = 48.sp, lineHeight = 52.sp, letterSpacing = (-0.5).sp,
    ),
    displayMedium = base.displayMedium.copy(
        fontFamily = NewsreaderFamily, fontWeight = FontWeight.Normal,
        fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-0.25).sp,
    ),
    displaySmall = base.displaySmall.copy(
        fontFamily = NewsreaderFamily, fontWeight = FontWeight.Normal,
        fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = 0.sp,
    ),
    headlineLarge = base.headlineLarge.copy(
        fontFamily = NewsreaderFamily, fontWeight = FontWeight.Normal,
        fontSize = 28.sp, lineHeight = 34.sp,
    ),
    headlineMedium = base.headlineMedium.copy(
        fontFamily = NewsreaderFamily, fontWeight = FontWeight.Normal,
        fontSize = 24.sp, lineHeight = 30.sp,
    ),
    headlineSmall = base.headlineSmall.copy(
        fontFamily = NewsreaderFamily, fontWeight = FontWeight.Normal,
        fontSize = 20.sp, lineHeight = 26.sp,
    ),
    titleLarge = base.titleLarge.copy(
        fontFamily = RobotoFamily, fontWeight = FontWeight.Medium,
        fontSize = 20.sp, lineHeight = 26.sp,
    ),
    titleMedium = base.titleMedium.copy(
        fontFamily = RobotoFamily, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = 0.15.sp,
    ),
    titleSmall = base.titleSmall.copy(
        fontFamily = RobotoFamily, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
    ),
    bodyLarge = base.bodyLarge.copy(
        fontFamily = RobotoFamily, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.25.sp,
    ),
    bodyMedium = base.bodyMedium.copy(
        fontFamily = RobotoFamily, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp,
    ),
    bodySmall = base.bodySmall.copy(
        fontFamily = RobotoFamily, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp,
    ),
    labelLarge = base.labelLarge.copy(
        fontFamily = RobotoFamily, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp,
    ),
    labelMedium = base.labelMedium.copy(
        fontFamily = RobotoFamily, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp,
    ),
    labelSmall = base.labelSmall.copy(
        fontFamily = RobotoFamily, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp,
    ),
)

/**
 * Expressive display text with the brand's italic second line (e.g. "Hoy toca *aclarar.*").
 * Style for the emphasised part; combine with [AppTypography] display/headline styles.
 */
val NewsreaderItalic = TextStyle(fontFamily = NewsreaderFamily, fontStyle = FontStyle.Italic)
