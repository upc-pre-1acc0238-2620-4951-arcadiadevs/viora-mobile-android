package pe.edu.upc.viora.features.iam.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral500
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.features.iam.presentation.state.IamCountry

/**
 * Frame shared by every IAM step (Figma frames 03 and 05): back arrow on top, scrollable content
 * and a footer pinned to the bottom (primary pill + caption). The footer rides above the keyboard.
 */
@Composable
internal fun IamFrame(
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    footer: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Neutral100)
            .systemBarsPadding()
            .imePadding(),
    ) {
        Box(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 12.dp)) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back),
                        contentDescription = stringResource(R.string.action_back),
                        tint = Neutral900,
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            content = content,
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = footer,
        )
    }
}

/**
 * Underlined text field of the auth screens: small label on top, 18 sp value and a line that turns
 * dark on focus and red on error. [secret] adds the "Mostrar / Ocultar" toggle at the end.
 */
@Composable
internal fun UnderlineField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    isError: Boolean = error != null,
    secret: Boolean = false,
    prefix: String? = null,
    supporting: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    enabled: Boolean = true,
    errorContent: (@Composable () -> Unit)? = null,
) {
    var focused by remember { mutableStateOf(false) }
    var revealed by remember { mutableStateOf(false) }
    val errorColor = MaterialTheme.colorScheme.error
    val lineColor = when {
        isError -> errorColor
        focused -> Green900
        else -> Neutral300
    }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isError) errorColor else Neutral600,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (prefix != null) {
                Text(
                    text = prefix,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = Neutral600,
                    modifier = Modifier.padding(end = 8.dp),
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, color = Neutral900),
                cursorBrush = SolidColor(Green900),
                visualTransformation = if (secret && !revealed) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (secret) KeyboardType.Password else keyboardType,
                    imeAction = imeAction,
                    autoCorrectEnabled = false,
                ),
                keyboardActions = KeyboardActions(onAny = { onImeAction() }),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp)
                    .onFocusChanged { focused = it.isFocused },
            )
            if (secret) {
                Text(
                    text = stringResource(if (revealed) R.string.iam_hide else R.string.iam_show),
                    style = MaterialTheme.typography.labelLarge,
                    color = Neutral900,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button) { revealed = !revealed }
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                )
            }
        }
        Box(Modifier.fillMaxWidth().height(if (focused || isError) 2.dp else 1.dp).background(lineColor))
        when {
            errorContent != null -> errorContent()
            error != null -> Text(error, style = MaterialTheme.typography.bodySmall, color = errorColor)
            supporting != null -> Text(supporting, style = MaterialTheme.typography.bodySmall, color = Neutral600)
        }
    }
}

/** One live password requirement: "✓ 8 caracteres". Red cross once the producer has typed and it fails. */
internal data class Rule(val label: String, val met: Boolean, val touched: Boolean)

@Composable
internal fun RuleRow(rules: List<Rule>, modifier: Modifier = Modifier) {
    val errorColor = MaterialTheme.colorScheme.error
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        rules.forEach { rule ->
            val failed = rule.touched && !rule.met
            val color = when {
                rule.met -> Green800
                failed -> errorColor
                else -> Neutral500
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(if (failed) R.drawable.ic_close else R.drawable.ic_check),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp),
                )
                Text(rule.label, style = MaterialTheme.typography.labelMedium, color = if (failed) errorColor else Neutral600)
            }
        }
    }
}

/** Six underlined digit slots over one hidden numeric field (Figma T04a). Digits turn red on error. */
@Composable
internal fun OtpInput(
    code: String,
    onCodeChange: (String) -> Unit,
    isError: Boolean,
    modifier: Modifier = Modifier,
    length: Int = 6,
    enabled: Boolean = true,
) {
    val focusRequester = remember { FocusRequester() }
    val errorColor = MaterialTheme.colorScheme.error
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    BasicTextField(
        value = code,
        onValueChange = { onCodeChange(it.filter(Char::isDigit).take(length)) },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
        modifier = modifier.focusRequester(focusRequester),
        decorationBox = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(length) { index ->
                    val active = index == code.length && !isError
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.height(52.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = code.getOrNull(index)?.toString().orEmpty(),
                                style = MaterialTheme.typography.displaySmall.copy(fontFamily = NewsreaderFamily, fontSize = 34.sp),
                                color = if (isError) errorColor else Neutral900,
                            )
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(if (active || isError) 2.dp else 1.dp)
                                .background(
                                    when {
                                        isError -> errorColor
                                        active -> Green900
                                        else -> Neutral300
                                    },
                                ),
                        )
                    }
                }
            }
        },
    )
}

/** Full-width 56 dp dark pill; the disabled look is the flat sand pill of the Figma variants. */
@Composable
internal fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(CircleShape)
            .background(if (enabled) Green900 else Neutral200)
            .clickable(enabled = enabled && !loading, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp, color = Neutral50)
        } else {
            Text(text, style = MaterialTheme.typography.titleMedium, color = if (enabled) Neutral50 else Neutral500)
        }
    }
}

/** Underlined text link ("Cambiar correo", "¿Olvidaste tu contraseña?"). */
@Composable
internal fun UnderlinedLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Neutral900,
    enabled: Boolean = true,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(textDecoration = TextDecoration.Underline),
        color = if (enabled) color else Neutral500,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(vertical = 8.dp),
    )
}

/** Small tinted dot + caps label above the centred titles ("● ENLACE ENVIADO"). */
@Composable
internal fun StatusLabel(text: String, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(Terracotta500))
        Text(text, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp), color = Neutral600)
    }
}

/** Centred two-line serif title of the result screens ("Revisa tu / *correo.*"). */
@Composable
internal fun CenteredHeadline(lead: String, emphasis: String, modifier: Modifier = Modifier) {
    val style = MaterialTheme.typography.displaySmall.copy(fontSize = 36.sp, lineHeight = 40.sp, textAlign = TextAlign.Center)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(lead, style = style, color = Neutral900)
        Text(emphasis, style = style, fontStyle = FontStyle.Italic, color = Neutral900)
    }
}

/** Soft green disc with an open envelope: the "enlace enviado" and "enlace vencido" artwork. */
@Composable
internal fun EnvelopeIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier.size(200.dp)) {
        val w = size.width
        drawCircle(color = Green200.copy(alpha = 0.55f), radius = w * 0.46f, center = Offset(w * 0.5f, w * 0.52f))
        val left = w * 0.25f
        val top = w * 0.36f
        val envW = w * 0.5f
        val envH = w * 0.36f
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawRoundRect(Neutral50, Offset(left, top), Size(envW, envH), CornerRadius(6.dp.toPx()))
        drawRoundRect(Neutral900, Offset(left, top), Size(envW, envH), CornerRadius(6.dp.toPx()), style = stroke)
        val flap = Path().apply {
            moveTo(left, top)
            lineTo(left + envW / 2, top + envH * 0.55f)
            lineTo(left + envW, top)
        }
        drawPath(flap, Neutral900, style = stroke)
        val fold = Path().apply {
            moveTo(left, top + envH)
            lineTo(left + envW * 0.36f, top + envH * 0.52f)
            moveTo(left + envW, top + envH)
            lineTo(left + envW * 0.64f, top + envH * 0.52f)
        }
        drawPath(fold, Neutral900, style = stroke)
        drawCircle(Terracotta500, radius = 5.dp.toPx(), center = Offset(left + envW, top + envH))
    }
}

/** Dark green disc with the Viora isotype: the "contraseña actualizada" artwork. */
@Composable
internal fun IsotypeBadge(modifier: Modifier = Modifier) {
    Box(modifier.size(200.dp).clip(CircleShape).background(Green200.copy(alpha = 0.55f)), contentAlignment = Alignment.Center) {
        Box(Modifier.size(104.dp).clip(CircleShape).background(Green900), contentAlignment = Alignment.Center) {
            Icon(painter = painterResource(R.drawable.ic_isotype), contentDescription = null, tint = Neutral50, modifier = Modifier.size(52.dp))
        }
    }
}

/** Pill chips to choose the country of the operation. */
@Composable
internal fun CountryChips(selected: IamCountry, onSelect: (IamCountry) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IamCountry.entries.forEach { country ->
            val isSelected = country == selected
            Box(
                Modifier
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Green900 else Neutral50)
                    .clickable(role = Role.RadioButton) { onSelect(country) }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(country.nameRes),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) Neutral50 else Neutral900,
                )
            }
        }
    }
}

