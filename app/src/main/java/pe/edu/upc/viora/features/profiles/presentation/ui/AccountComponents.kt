package pe.edu.upc.viora.features.profiles.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.CircleIconButton
import pe.edu.upc.viora.core.designsystem.component.VioraTopBar
import pe.edu.upc.viora.core.designsystem.component.VioraTopBarTitle
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest
import pe.edu.upc.viora.core.designsystem.theme.Harvest400
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral500
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.profiles.domain.entity.UserProfile

/** Top bar of the account screens: round back (or close) button and a centered title over a subtitle, or [notice]. */
@Composable
internal fun AccountTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isClose: Boolean = false,
    notice: String? = null,
) {
    VioraTopBar(
        onBack = onBack,
        modifier = modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        navigationIcon = if (isClose) R.drawable.ic_close else R.drawable.ic_arrow_back,
        navigationDescription = stringResource(if (isClose) R.string.action_close else R.string.action_back),
    ) {
        if (notice != null) {
            // The confirmation of a language switch takes the place of the title (P95 · English).
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) { LanguageNotice(text = notice) }
        } else {
            VioraTopBarTitle(
                title = title,
                subtitle = subtitle,
                titleStyle = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
                subtitleStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
                horizontalPadding = 0.dp,
            )
        }
    }
}

/** The "Language: English" pill that confirms the switch, in place of the title (P95 · English). */
@Composable
internal fun LanguageNotice(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Green800)
            .padding(start = 6.dp, top = 6.dp, end = 14.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(28.dp).clip(CircleShape).background(Harvest400),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = Neutral900, modifier = Modifier.size(18.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
            color = Neutral0,
        )
    }
}

/**
 * The sage card of P95: initials, the access badge ("Plan activo" or "Acceso en pausa", none until the server
 * answers), the name in two lines and the email.
 */
@Composable
internal fun ProfileHeroCard(profile: UserProfile, isMember: Boolean?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Green200)
            .padding(start = 22.dp, top = 22.dp, end = 22.dp, bottom = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier.size(64.dp).clip(CircleShape).background(Neutral0),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = profile.initials,
                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = NewsreaderFamily, fontSize = 24.sp),
                    color = Green900,
                )
            }
            if (isMember != null) MembershipBadge(active = isMember, modifier = Modifier.padding(top = 18.dp))
        }
        Spacer(Modifier.height(8.dp))
        // "Teodoro" over an italic "Mamani", like the editorial titles.
        val first = profile.fullName.trim().substringBefore(' ')
        val rest = profile.fullName.trim().substringAfter(' ', missingDelimiterValue = "")
        val nameStyle = MaterialTheme.typography.displaySmall.copy(
            fontFamily = NewsreaderFamily,
            fontSize = 34.sp,
            lineHeight = 36.sp,
            letterSpacing = (-0.34).sp,
        )
        Text(text = first, style = nameStyle, color = Green900, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (rest.isNotEmpty()) {
            Text(text = rest, style = nameStyle, fontStyle = FontStyle.Italic, color = Green900, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (!profile.email.isNullOrBlank()) {
            Spacer(Modifier.height(14.dp))
            Text(text = profile.email, style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp), color = Green900)
        }
    }
}

@Composable
private fun MembershipBadge(active: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(Neutral0)
            .padding(start = 8.dp, top = 6.dp, end = 12.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(if (active) R.drawable.ic_task_alt else R.drawable.ic_priority_high),
            contentDescription = null,
            tint = if (active) Green900 else Terracotta700,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = stringResource(if (active) R.string.account_membership_active else R.string.account_membership_paused),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
            color = Green900,
        )
    }
}

/** A group of rows: the spaced capitals over a white card. */
@Composable
internal fun AccountSection(title: String, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.88.sp),
            color = Neutral600,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Neutral0)
                .padding(start = 20.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        ) {
            content()
        }
    }
}

/** One row of a section: label, value and chevron; [showDivider] draws the hairline under it. */
@Composable
internal fun AccountRow(
    label: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    isDestructive: Boolean = false,
    showDivider: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .then(if (showDivider) Modifier.hairline(Neutral200) else Modifier)
            .padding(vertical = 15.dp)
            .height(20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = if (isDestructive) FontWeight.Medium else FontWeight.Normal,
            ),
            color = if (isDestructive) Terracotta700 else Neutral900,
            modifier = Modifier.weight(1f),
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 20.sp),
                color = Neutral600,
                maxLines = 1,
            )
        }
        if (!isDestructive) {
            Icon(painterResource(R.drawable.ic_chevron_right), contentDescription = null, tint = Neutral500, modifier = Modifier.size(20.dp))
        }
    }
}

/** A row with a switch instead of a value (Figma "Viora/Interruptor"); the whole row toggles it. */
@Composable
internal fun AccountSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = false,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .then(if (showDivider) Modifier.hairline(Neutral200) else Modifier)
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp),
            color = Neutral900,
            modifier = Modifier.weight(1f),
        )
        VioraSwitch(checked = checked)
    }
}

/** 52 × 32 pill: dark green with the knob right when on, grey with the knob left when off. */
@Composable
private fun VioraSwitch(checked: Boolean, modifier: Modifier = Modifier) {
    val knobX by animateDpAsState(targetValue = if (checked) 24.dp else 4.dp, label = "knob")
    val track by animateColorAsState(targetValue = if (checked) Green900 else Neutral200, label = "track")
    Box(
        modifier = modifier
            .size(width = 52.dp, height = 32.dp)
            .clip(CircleShape)
            .background(track),
    ) {
        Box(
            modifier = Modifier
                .offset(x = knobX, y = 4.dp)
                .size(24.dp)
                .shadow(elevation = 1.dp, shape = CircleShape)
                .background(Neutral0, CircleShape),
        )
    }
}

private fun Modifier.hairline(color: Color): Modifier = drawBehind {
    val y = size.height - 0.5.dp.toPx()
    drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
}

/**
 * The white field card of P96/P97: a small label over the value, an optional eye for secrets, and
 * under it a helper or an error with the warning icon. An error paints the border terracotta.
 */
@Composable
internal fun ValidatedFieldCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    readOnly: Boolean = false,
    isSecret: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
) {
    var revealed by remember { mutableStateOf(false) }
    val isError = errorText != null
    val shape = RoundedCornerShape(24.dp)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Neutral0)
                .then(if (isError) Modifier.border(1.5.dp, Terracotta500, shape) else Modifier)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
                    color = if (isError) Terracotta700 else Neutral600,
                )
                val textColor = when {
                    isError -> Terracotta700
                    readOnly -> Neutral600
                    else -> Neutral900
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    readOnly = readOnly,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 22.sp, color = textColor),
                    cursorBrush = SolidColor(Green900),
                    visualTransformation = if (isSecret && !revealed) PasswordVisualTransformation() else visualTransformation,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (isSecret) KeyboardType.Password else keyboardType,
                        imeAction = imeAction,
                    ),
                    keyboardActions = KeyboardActions(onAny = { onImeAction() }),
                    decorationBox = { inner ->
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 22.sp),
                                color = Neutral500,
                            )
                        }
                        inner()
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (isSecret) {
                Icon(
                    painter = painterResource(R.drawable.ic_visibility),
                    contentDescription = null,
                    tint = Neutral500,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable(role = Role.Switch) { revealed = !revealed },
                )
            }
        }
        FieldNote(text = errorText ?: helperText, isError = isError)
    }
}

/** The line under a field: the helper in grey, or the error with the warning icon. */
@Composable
internal fun FieldNote(text: String?, isError: Boolean, modifier: Modifier = Modifier) {
    if (text == null) return
    Row(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isError) {
            Icon(painterResource(R.drawable.ic_warning), contentDescription = null, tint = Terracotta500, modifier = Modifier.size(14.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 14.sp),
            color = if (isError) Terracotta700 else Neutral600,
        )
    }
}

/** The 60 dp dark pill with the yellow check of P96–P98; disabled it turns grey. */
@Composable
internal fun CheckmarkPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    checkColor: Color = Harvest,
) {
    val active = enabled || loading
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(CircleShape)
            .background(if (active) Green900 else Neutral200)
            .clickable(enabled = enabled && !loading, role = Role.Button, onClick = onClick)
            .padding(start = 24.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
            color = if (active) Neutral50 else Neutral500,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier.size(44.dp).clip(CircleShape).background(if (active) checkColor else Neutral100),
            contentAlignment = Alignment.Center,
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Neutral900)
            } else {
                Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = Neutral900, modifier = Modifier.size(24.dp))
            }
        }
    }
}

/** Shows the digits of a mobile in groups of three ("952184337" reads "952 184 337"); the value keeps only digits. */
internal object DigitGroupsTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val grouped = text.text.chunked(3).joinToString(" ")
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = offset + (offset - 1).coerceAtLeast(0) / 3
            override fun transformedToOriginal(offset: Int): Int = (offset - offset / 4).coerceIn(0, text.length)
        }
        return TransformedText(AnnotatedString(grouped), mapping)
    }
}

/** Horizontal margin of the account screens, as in Figma. */
internal val AccountGutter: Dp = 24.dp
