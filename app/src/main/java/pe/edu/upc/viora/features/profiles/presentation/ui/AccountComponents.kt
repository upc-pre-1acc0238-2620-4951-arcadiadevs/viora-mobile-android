package pe.edu.upc.viora.features.profiles.presentation.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral500
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.features.profiles.domain.entity.UserProfile

private val PaleTerracottaCard = Color(0xFFFBE8DE)
private val AvatarSalmon = Color(0xFFF6D9CD)
private val AvatarTextBrown = Color(0xFFC15A2E)

/**
 * Top bar matching Figma frames (P15, P16, P17):
 * Back arrow or close (X) button on left, centered title and subtitle.
 */
@Composable
fun AccountTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isClose: Boolean = false,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Icon(
                painter = painterResource(if (isClose) R.drawable.ic_close else R.drawable.ic_arrow_back),
                contentDescription = stringResource(if (isClose) R.string.action_close else R.string.action_back),
                tint = Neutral900,
            )
        }
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Neutral900,
                maxLines = 1,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Neutral600,
                maxLines = 1,
            )
        }
    }
}

/** Editorial headline with Newsreader font (e.g. "Tu\n*cuenta.*"). */
@Composable
fun AccountEditorialTitle(lead: String, emphasis: String, modifier: Modifier = Modifier) {
    val annotated = buildAnnotatedString {
        append(lead)
        append("\n")
        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
            append(emphasis)
        }
    }
    Text(
        text = annotated,
        style = MaterialTheme.typography.displaySmall.copy(
            fontFamily = NewsreaderFamily,
            fontSize = 38.sp,
            lineHeight = 42.sp,
            letterSpacing = (-0.76).sp,
        ),
        color = Neutral900,
        modifier = modifier.fillMaxWidth(),
    )
}

/**
 * Warm salmon / pale terracotta card with user avatar, "Licencia activa" badge, name and email.
 */
@Composable
fun ProfileHeroCard(profile: UserProfile, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PaleTerracottaCard)
            .padding(20.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(AvatarSalmon),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = profile.initials,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AvatarTextBrown,
                        ),
                    )
                }

                // Active license badge
                if (profile.licenseActive) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Neutral0)
                            .border(1.dp, Neutral200, CircleShape)
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_check),
                            contentDescription = null,
                            tint = Green800,
                            modifier = Modifier.size(14.dp),
                        )
                        Text(
                            text = stringResource(R.string.account_license_active),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                            ),
                            color = Green900,
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = profile.fullName,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = NewsreaderFamily,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = Neutral900,
                )
                if (!profile.email.isNullOrBlank()) {
                    Text(
                        text = profile.email,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = Neutral600,
                    )
                }
            }
        }
    }
}

/** Group of settings rows inside a rounded white card. */
@Composable
fun AccountSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
            ),
            color = Neutral600,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Neutral0),
        ) {
            content()
        }
    }
}

/**
 * Single setting row: label on left, value on right with chevron indicator.
 */
@Composable
fun AccountRow(
    label: String,
    modifier: Modifier = Modifier,
    value: String? = null,
    isDestructive: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            color = if (isDestructive) Terracotta500 else Neutral900,
        )
        if (value != null || onClick != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (value != null) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = Neutral600,
                    )
                }
                if (onClick != null && !isDestructive) {
                    Icon(
                        painter = painterResource(R.drawable.ic_chevron_right),
                        contentDescription = null,
                        tint = Neutral500,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

/** Row divider. */
@Composable
fun AccountDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(0.5.dp)
            .background(Neutral200),
    )
}

/**
 * Card input field matching P16:
 * Rounded card with inner label, value input, and helper/error text below.
 */
@Composable
fun ValidatedFieldCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    isError: Boolean = errorText != null,
    enabled: Boolean = true,
    isSecret: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
) {
    var revealed by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Neutral0)
                .border(
                    width = if (isError) 1.5.dp else 1.dp,
                    color = if (isError) Terracotta500 else Neutral200,
                    shape = RoundedCornerShape(14.dp),
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                    color = if (isError) Terracotta500 else Neutral600,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        enabled = enabled,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 17.sp,
                            color = if (enabled) Neutral900 else Neutral600,
                        ),
                        cursorBrush = SolidColor(Green900),
                        visualTransformation = when {
                            isSecret && !revealed -> PasswordVisualTransformation()
                            else -> VisualTransformation.None
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = if (isSecret) KeyboardType.Password else keyboardType,
                            imeAction = imeAction,
                        ),
                        keyboardActions = KeyboardActions(onAny = { onImeAction() }),
                        decorationBox = { innerTextField ->
                            if (value.isEmpty() && placeholder != null) {
                                Text(
                                    text = placeholder,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                                    color = Neutral500,
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.weight(1f),
                    )
                    if (isSecret) {
                        IconButton(
                            onClick = { revealed = !revealed },
                            modifier = Modifier.size(24.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_visibility),
                                contentDescription = null,
                                tint = Neutral600,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }

        // Subtext / Helper / Error
        when {
            isError && errorText != null -> {
                Text(
                    text = errorText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Terracotta500,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            helperText != null -> {
                Text(
                    text = helperText,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Neutral600,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
        }
    }
}

/**
 * 56dp action pill button with right-aligned circular check badge.
 * Matches Figma P16/P17 "Guardar cambios" / "Cambiar contraseña".
 */
@Composable
fun CheckmarkPillButton(
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
            .clickable(enabled = enabled && !loading, role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp,
                color = Neutral50,
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (enabled) Neutral50 else Neutral500,
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (enabled) Harvest else Neutral300),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = if (enabled) Green900 else Neutral500,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
