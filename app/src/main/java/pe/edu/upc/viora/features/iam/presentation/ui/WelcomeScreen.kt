package pe.edu.upc.viora.features.iam.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Forest
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900

/**
 * T02 · Bienvenida (Figma "01 · Bienvenida"): two plates that introduce Viora before the
 * onboarding questions. "Comenzar" opens the producer plate; "Continuar" and "Omitir" on it start
 * the onboarding, and "Ya tengo una cuenta" goes to sign in. Back on the second plate returns to
 * the first one.
 */
@Composable
fun WelcomeScreen(
    onStartOnboarding: () -> Unit,
    onSignIn: () -> Unit,
) {
    var page by rememberSaveable { mutableIntStateOf(INTRO_PLATE) }
    BackHandler(enabled = page == PRODUCER_PLATE) { page = INTRO_PLATE }

    AnimatedContent(
        targetState = page,
        transitionSpec = {
            val forward = targetState > initialState
            (slideInHorizontally(tween(PAGE_MILLIS)) { if (forward) it / 4 else -it / 4 } + fadeIn(tween(PAGE_MILLIS)))
                .togetherWith(slideOutHorizontally(tween(PAGE_MILLIS)) { if (forward) -it / 4 else it / 4 } + fadeOut(tween(PAGE_MILLIS / 2)))
        },
        modifier = Modifier.fillMaxSize().background(Neutral50),
        label = "welcome plate",
    ) { plate ->
        if (plate == INTRO_PLATE) {
            WelcomePlate(
                eyebrow = R.string.welcome_intro_eyebrow,
                titleLead = R.string.welcome_intro_title_lead,
                titleSecond = R.string.welcome_intro_title_second,
                titleEmphasis = R.string.welcome_intro_title_emphasis,
                description = R.string.welcome_intro_description,
                descriptionGap = 42.dp,
                primaryLabel = R.string.welcome_start,
                onPrimary = { page = PRODUCER_PLATE },
                secondaryLabel = R.string.iam_have_account,
                onSecondary = onSignIn,
                illustration = { OliveBranchIllustration() },
            )
        } else {
            WelcomePlate(
                eyebrow = R.string.welcome_producer_eyebrow,
                titleLead = R.string.welcome_producer_title_lead,
                titleSecond = R.string.welcome_producer_title_second,
                titleEmphasis = R.string.welcome_producer_title_emphasis,
                description = R.string.welcome_producer_description,
                descriptionGap = 74.dp,
                primaryLabel = R.string.iam_continue,
                onPrimary = onStartOnboarding,
                secondaryLabel = R.string.welcome_skip,
                onSecondary = onStartOnboarding,
                illustration = { OliveTreeIllustration() },
            )
        }
    }
}

/**
 * One plate laid out as the 402 × 874 Figma frames: eyebrow and centred serif title on top, the
 * 300 dp drawing in the free space, then the description and the two 56 dp buttons at the bottom.
 */
@Composable
private fun WelcomePlate(
    @StringRes eyebrow: Int,
    @StringRes titleLead: Int,
    @StringRes titleSecond: Int,
    @StringRes titleEmphasis: Int,
    @StringRes description: Int,
    descriptionGap: Dp,
    @StringRes primaryLabel: Int,
    onPrimary: () -> Unit,
    @StringRes secondaryLabel: Int,
    onSecondary: () -> Unit,
    illustration: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))
        Eyebrow(stringResource(eyebrow))
        Spacer(Modifier.height(13.dp))
        val lead = stringResource(titleLead)
        val second = stringResource(titleSecond)
        val emphasis = stringResource(titleEmphasis)
        Text(
            text = buildAnnotatedString {
                append(lead)
                append('\n')
                append(second)
                append(' ')
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(emphasis) }
            },
            style = MaterialTheme.typography.displayMedium.copy(letterSpacing = (-0.8).sp),
            color = Neutral900,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().semantics { heading() },
        )
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            illustration()
        }
        Text(
            text = stringResource(description),
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp, letterSpacing = 0.sp),
            color = Neutral700,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(descriptionGap))
        PillButton(text = stringResource(primaryLabel), onClick = onPrimary)
        Spacer(Modifier.height(6.dp))
        TextPillButton(text = stringResource(secondaryLabel), onClick = onSecondary)
        Spacer(Modifier.height(20.dp))
    }
}

/** Figma "Editorial/Antetítulo" (Verde): green dot and spaced caps. */
@Composable
private fun Eyebrow(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(Forest))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(lineHeight = 13.sp, letterSpacing = 1.54.sp),
            color = Neutral700,
        )
    }
}

private const val INTRO_PLATE = 0
private const val PRODUCER_PLATE = 1
private const val PAGE_MILLIS = 420
