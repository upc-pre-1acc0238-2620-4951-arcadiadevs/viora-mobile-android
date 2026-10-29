package pe.edu.upc.viora.features.iam.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Shadow
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.presentation.PhoneCountry
import pe.edu.upc.viora.core.presentation.formatWhole
import pe.edu.upc.viora.features.iam.presentation.state.OnboardingUiState

/**
 * Serif title of every IAM and onboarding screen (Newsreader 36/40). Words between asterisks are set in
 * italic, as in Figma: "¿Cómo te\n*llamas*?".
 */
@Composable
internal fun IamTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = italicMarkup(text),
        style = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp, lineHeight = 40.sp, letterSpacing = (-0.72).sp),
        color = Neutral900,
        modifier = modifier.fillMaxWidth(),
    )
}

/** "*a*" becomes italic "a"; everything else stays as written. */
internal fun italicMarkup(text: String): AnnotatedString = buildAnnotatedString {
    text.split('*').forEachIndexed { index, part ->
        if (index % 2 == 1) withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(part) } else append(part)
    }
}

/** T02b2: the "País" field, "PE +51", that opens the list of countries. */
@Composable
internal fun CountryField(selected: PhoneCountry, onSelect: (PhoneCountry) -> Unit, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .clickable(role = Role.DropdownList) { open = true },
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(stringResource(R.string.onboarding_country_label), style = MaterialTheme.typography.labelMedium, color = Neutral600)
            Text(
                text = "${selected.iso} ${selected.dialCode}",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 20.sp, lineHeight = 28.sp),
                color = Neutral900,
                modifier = Modifier.padding(vertical = 6.dp),
            )
            Box(Modifier.fillMaxWidth().height(1.dp).background(Neutral300))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, containerColor = Neutral50) {
            PhoneCountry.entries.forEach { country ->
                DropdownMenuItem(
                    text = { Text("${stringResource(country.nameRes)}  ${country.dialCode}", color = Neutral900) },
                    onClick = {
                        onSelect(country)
                        open = false
                    },
                )
            }
        }
    }
}

// ---- T02d · Hectares -----------------------------------------------------------------------

/** "– 12 ha +": the big serif number between the two round buttons. */
@Composable
internal fun HectareStepper(hectares: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RoundStepButton(
            label = "–",
            description = stringResource(R.string.onboarding_hectares_less),
            filled = false,
            enabled = hectares > OnboardingUiState.MIN_HECTARES,
            onClick = { onChange(hectares - 1) },
        )
        Text(
            text = buildAnnotatedString {
                append(hectares.toString())
                append(' ')
                withStyle(SpanStyle(fontSize = 36.sp, fontStyle = FontStyle.Italic, color = Neutral600, letterSpacing = 0.sp)) {
                    append(stringResource(R.string.onboarding_hectares_unit))
                }
            },
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 96.sp, lineHeight = 96.sp, letterSpacing = (-3.84).sp),
            color = Neutral900,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        RoundStepButton(
            label = "+",
            description = stringResource(R.string.onboarding_hectares_more),
            filled = true,
            enabled = hectares < OnboardingUiState.MAX_HECTARES,
            onClick = { onChange(hectares + 1) },
        )
    }
}

@Composable
private fun RoundStepButton(label: String, description: String, filled: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .then(if (filled) Modifier.background(Green900) else Modifier.border(1.dp, Neutral300, CircleShape))
            .alpha(if (enabled) 1f else 0.4f)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 26.sp, color = if (filled) Neutral50 else Neutral900)
    }
}

/**
 * The 0-40 ha ruler under the stepper: dark ticks up to the value, light after it, a taller tick
 * every 5 ha and the harvest-yellow dot on the value. Tap or drag to pick hectares.
 */
@Composable
internal fun HectareRuler(hectares: Int, onChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    val max = OnboardingUiState.RULER_MAX_HECTARES
    val currentOnChange by rememberUpdatedState(onChange)
    Column(modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .pointerInput(Unit) {
                    fun pick(x: Float) = currentOnChange((x / size.width * max).roundToInt().coerceAtLeast(OnboardingUiState.MIN_HECTARES))
                    detectTapGestures { pick(it.x) }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ ->
                        currentOnChange((change.position.x / size.width * max).roundToInt().coerceIn(OnboardingUiState.MIN_HECTARES, max))
                    }
                },
        ) {
            val shown = hectares.coerceAtMost(max)
            val step = size.width / max
            val tick = 1.dp.toPx()
            for (i in 0..max) {
                val x = (i * step).coerceIn(tick / 2, size.width - tick / 2)
                val tall = i % 5 == 0
                drawLine(
                    color = if (i <= shown) Neutral900 else Neutral300,
                    start = Offset(x, 0f),
                    end = Offset(x, if (tall) 14.dp.toPx() else 9.dp.toPx()),
                    strokeWidth = tick,
                )
            }
            drawCircle(Harvest, radius = 6.dp.toPx(), center = Offset(shown * step, 29.dp.toPx()))
        }
        Box(Modifier.fillMaxWidth().padding(top = 6.dp)) {
            listOf(0, max / 2, max).forEachIndexed { index, value ->
                val align = when (index) {
                    0 -> Alignment.CenterStart
                    1 -> Alignment.Center
                    else -> Alignment.CenterEnd
                }
                Text(value.toString(), style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = Neutral600, modifier = Modifier.align(align))
            }
        }
    }
}

/** "PLAN ESTIMADO · S/ 7,920 al año" and its two explanation lines. */
@Composable
internal fun PlanEstimate(state: OnboardingUiState, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(Neutral200))
        Spacer(Modifier.height(24.dp))
        Caps(stringResource(R.string.onboarding_plan_estimate))
        Spacer(Modifier.height(6.dp))
        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.onboarding_plan_price, formatWhole(state.yearlyPrice)))
                append(' ')
                withStyle(SpanStyle(fontSize = 22.sp, fontStyle = FontStyle.Italic, color = Neutral600)) {
                    append(stringResource(R.string.onboarding_plan_per_year))
                }
            },
            style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.64).sp),
            color = Neutral900,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(
                R.string.onboarding_plan_detail,
                formatWhole(state.monthlyPrice),
                OnboardingUiState.PRICE_PER_HECTARE_MONTH,
            ),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 19.sp, letterSpacing = 0.sp),
            color = Neutral700,
        )
    }
}

// ---- T02e · Alerts --------------------------------------------------------------------------

/**
 * Two stacked notification cards as a preview of what Viora warns about: yesterday's one faded
 * behind, today's one in front with its shadow. The texts are examples, as in Figma.
 */
@Composable
internal fun AlertPreview(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(150.dp)) {
        NotificationCard(
            meta = stringResource(R.string.onboarding_alert_example_back_meta),
            body = stringResource(R.string.onboarding_alert_example_back_body),
            dot = Green800,
            modifier = Modifier.padding(horizontal = 20.dp).alpha(0.6f),
        )
        NotificationCard(
            meta = stringResource(R.string.onboarding_alert_example_front_meta),
            body = stringResource(R.string.onboarding_alert_example_front_body),
            dot = Terracotta500,
            modifier = Modifier
                .padding(top = 40.dp)
                .shadow(12.dp, RoundedCornerShape(22.dp), ambientColor = Shadow.copy(alpha = 0.10f), spotColor = Shadow.copy(alpha = 0.14f)),
        )
    }
}

@Composable
private fun NotificationCard(meta: String, body: String, dot: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Neutral0)
            .border(1.dp, Neutral200, RoundedCornerShape(22.dp))
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(dot))
            Caps(meta, letterSpacing = 1.1)
        }
        Text(body, style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp, lineHeight = 26.sp), color = Neutral900)
    }
}

/** The "—  item" list of T02e and T02e2. */
@Composable
internal fun DashList(items: List<String>, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(14.dp).height(1.dp).background(Neutral900))
                Text(item, style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp), color = Neutral700)
            }
        }
    }
}

// ---- T02e2 · Location -----------------------------------------------------------------------

/**
 * "Ilustración · Trazado GPS": the field map with the corners already walked, the estimated area,
 * the producer's position and the two chips. Drawn over the exported map on the 354 × 200 Figma box.
 */
@Composable
internal fun GpsTraceIllustration(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(22.dp))) {
        val scale = maxWidth / 354.dp
        Image(
            painter = painterResource(R.drawable.ill_onboarding_field_map),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        Canvas(Modifier.fillMaxSize()) {
            val sx = size.width / 354f
            val sy = size.height / 200f
            fun p(x: Float, y: Float) = Offset(x * sx, y * sy)
            val area = Path().apply {
                moveTo(p(66f, 58f).x, p(66f, 58f).y)
                lineTo(p(236f, 40f).x, p(236f, 40f).y)
                lineTo(p(270f, 140f).x, p(270f, 140f).y)
                lineTo(p(92f, 160f).x, p(92f, 160f).y)
                close()
            }
            drawPath(area, Harvest.copy(alpha = 0.22f))
            drawPath(
                area,
                Shadow.copy(alpha = 0.5f),
                style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 5.dp.toPx()))),
            )
            val walked = Path().apply {
                moveTo(p(66f, 58f).x, p(66f, 58f).y)
                lineTo(p(236f, 40f).x, p(236f, 40f).y)
                lineTo(p(270f, 140f).x, p(270f, 140f).y)
                lineTo(p(186f, 150f).x, p(186f, 150f).y)
            }
            drawPath(walked, Shadow, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            listOf(p(66f, 58f), p(236f, 40f), p(270f, 140f)).forEach { corner ->
                drawCircle(Neutral0, radius = 7.dp.toPx(), center = corner)
                drawCircle(Shadow, radius = 6.dp.toPx(), center = corner, style = Stroke(width = 2.dp.toPx()))
            }
            val position = p(186f, 150f)
            drawCircle(Shadow.copy(alpha = 0.16f), radius = 23.dp.toPx(), center = position)
            drawCircle(Neutral0, radius = 9.dp.toPx(), center = position)
            drawCircle(Harvest, radius = 6.5.dp.toPx(), center = position)
        }
        MapChip(Modifier.offset(x = 14.dp * scale, y = 14.dp * scale)) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(Terracotta500))
            Caps(stringResource(R.string.onboarding_location_example_corner), letterSpacing = 1.3)
        }
        MapChip(Modifier.align(Alignment.BottomEnd).padding(end = 14.dp * scale, bottom = 14.dp * scale)) {
            Text(
                stringResource(R.string.onboarding_location_example_area),
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp, lineHeight = 20.sp),
                color = Neutral900,
            )
        }
    }
}

@Composable
private fun MapChip(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Row(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black.copy(alpha = 0.08f), spotColor = Color.Black.copy(alpha = 0.08f))
            .clip(RoundedCornerShape(20.dp))
            .background(Neutral0)
            .padding(start = 12.dp, end = 14.dp, top = 7.dp, bottom = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) { content() }
}

// ---- T02f · Summary -------------------------------------------------------------------------

/** One summary row: caps label, serif value and the underlined "Editar". */
@Composable
internal fun SummaryRow(label: String, value: String, onEdit: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        // 72 dp rows as in Figma: 12 + label 16 + 4 + value 26 + 12 (+ divider).
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Caps(label)
                Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp, lineHeight = 26.sp), color = Neutral900)
            }
            Text(
                text = stringResource(R.string.onboarding_summary_edit),
                style = MaterialTheme.typography.labelLarge.copy(textDecoration = TextDecoration.Underline, letterSpacing = 0.sp),
                color = Green800,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(role = Role.Button, onClick = onEdit)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Neutral200))
    }
}

/**
 * "Al continuar aceptas los *Términos y condiciones* y la *Política de privacidad* (Ley N.° 29733)."
 * The two names are underlined as in Figma; there are no published pages to open yet.
 */
@Composable
internal fun LegalNotice(modifier: Modifier = Modifier) {
    val terms = stringResource(R.string.onboarding_legal_terms)
    val privacy = stringResource(R.string.onboarding_legal_privacy)
    val text = stringResource(R.string.onboarding_legal_notice, terms, privacy)
    Text(
        text = buildAnnotatedString {
            append(text)
            listOf(terms, privacy).forEach { name ->
                val start = text.indexOf(name)
                if (start >= 0) {
                    addStyle(SpanStyle(color = Neutral900, textDecoration = TextDecoration.Underline), start, start + name.length)
                }
            }
        },
        style = MaterialTheme.typography.bodySmall.copy(lineHeight = 17.sp, letterSpacing = 0.sp),
        color = Neutral600,
        textAlign = TextAlign.Center,
        modifier = modifier.padding(horizontal = 12.dp),
    )
}

/** Small spaced caps used by the summary, the plan and the cards. */
@Composable
private fun Caps(text: String, letterSpacing: Double = 1.32) {
    Text(text, style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, letterSpacing = letterSpacing.sp), color = Neutral600)
}
