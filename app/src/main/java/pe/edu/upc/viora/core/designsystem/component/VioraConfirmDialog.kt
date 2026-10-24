package pe.edu.upc.viora.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily

/**
 * The centered confirmation of Viora (Figma "Capa · ¿Descartar este lote?" and "Capa · Confirma tu
 * plan anual"): a round icon, a serif title, a short body, the main action as a dark pill and the
 * other one as a text button under it. Tapping outside or Back calls [onDismiss].
 */
@Composable
fun VioraConfirmDialog(
    @DrawableRes icon: Int,
    iconBackground: Color,
    iconTint: Color,
    title: String,
    body: String,
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String,
    onSecondary: () -> Unit,
    onDismiss: () -> Unit,
    secondaryColor: Color = Neutral900,
    note: String? = null,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier
                .padding(horizontal = 32.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Neutral50)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(iconBackground), contentAlignment = Alignment.Center) {
                Icon(painterResource(icon), contentDescription = null, tint = iconTint, modifier = Modifier.size(26.dp))
            }
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = NewsreaderFamily, fontSize = 26.sp, lineHeight = 30.sp),
                color = Neutral900,
                textAlign = TextAlign.Center,
            )
            Text(body, style = MaterialTheme.typography.bodyMedium, color = Neutral700, textAlign = TextAlign.Center)
            note?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Neutral700, textAlign = TextAlign.Center) }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(CircleShape)
                    .background(Green900)
                    .clickable(role = Role.Button, onClick = onPrimary),
                contentAlignment = Alignment.Center,
            ) {
                Text(primaryLabel, style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp), color = Neutral50)
            }
            Text(
                secondaryLabel,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                color = secondaryColor,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onSecondary)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            )
        }
    }
}
