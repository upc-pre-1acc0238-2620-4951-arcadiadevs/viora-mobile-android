package pe.edu.upc.viora.features.telemetry.presentation.ui.components

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.RobotoFamily
import pe.edu.upc.viora.features.telemetry.domain.entity.MitigationStep

@Composable
fun MitigationChecklistCard(
    steps: List<MitigationStep>,
    onToggleStep: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val completedCount = steps.count { it.completed }
    val totalCount = steps.size

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Neutral0)
            .padding(20.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.alert_checklist_header, completedCount, totalCount),
                fontFamily = NewsreaderFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = Neutral900,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                steps.forEach { step ->
                    MitigationStepRow(
                        step = step,
                        onClick = { onToggleStep(step.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun MitigationStepRow(
    step: MitigationStep,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !step.completed, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .then(
                    if (step.completed) {
                        Modifier.background(Green800)
                    } else {
                        Modifier
                            .background(Neutral0)
                            .border(1.5.dp, Neutral200, CircleShape)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (step.completed) {
                Icon(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    tint = Neutral0,
                    modifier = Modifier.size(16.dp),
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = step.instruction,
            fontFamily = RobotoFamily,
            fontWeight = if (step.completed) FontWeight.Normal else FontWeight.Medium,
            fontSize = 14.sp,
            color = if (step.completed) Neutral600 else Neutral900,
            textDecoration = if (step.completed) TextDecoration.LineThrough else TextDecoration.None,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f),
        )
    }
}
