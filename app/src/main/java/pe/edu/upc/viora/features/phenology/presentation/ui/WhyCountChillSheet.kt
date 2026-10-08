package pe.edu.upc.viora.features.phenology.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green700
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest300
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.RobotoFamily

/**
 * Bottom Sheet P81: "¿Por qué cuento frío?"
 * Explains Erez dynamic model, cold accumulation vs warm days negation (> 24 °C),
 * and why 30 portions are critical for Sevillana / Criolla in southern coastal Peru.
 * Aligned 100% with Figma node 389:81359.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WhyCountChillSheet(
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Neutral100,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            Box(
                Modifier
                    .padding(top = 12.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Neutral300),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Eyebrow
            Text(
                text = stringResource(R.string.winter_chill_hero_eyebrow),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = RobotoFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                ),
                color = Neutral600,
            )

            // Headline (Newsreader 34sp: Line 1 regular, Line 2 italic)
            Column {
                Text(
                    text = stringResource(R.string.winter_chill_why_title_lead),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = NewsreaderFamily,
                        fontSize = 34.sp,
                        lineHeight = 38.sp,
                    ),
                    color = Neutral900,
                )
                Text(
                    text = stringResource(R.string.winter_chill_why_title_emphasis),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = NewsreaderFamily,
                        fontSize = 34.sp,
                        lineHeight = 38.sp,
                        fontStyle = FontStyle.Italic,
                    ),
                    color = Neutral900,
                )
            }

            // Intro explanation
            Text(
                text = stringResource(R.string.winter_chill_why_sheet_intro),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = RobotoFamily,
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                ),
                color = Neutral700,
            )

            // Card 1: Example equation
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Neutral0)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = stringResource(R.string.winter_chill_why_example_title),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = RobotoFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                    ),
                    color = Neutral900,
                )

                // Equation row: wraps instead of cutting the result on narrow screens
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Green800)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.winter_chill_why_pill_night),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = RobotoFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                            ),
                            color = Neutral0,
                        )
                    }

                    Text(
                        text = "+",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                        color = Neutral600,
                    )

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Harvest300)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.winter_chill_why_pill_day),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = RobotoFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                            ),
                            color = Neutral900,
                        )
                    }

                    Text(
                        text = "=",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                        color = Neutral600,
                    )

                    Text(
                        text = stringResource(R.string.winter_chill_why_result),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = NewsreaderFamily,
                            fontSize = 32.sp,
                            lineHeight = 32.sp,
                        ),
                        color = Neutral900,
                    )
                }

                Text(
                    text = stringResource(R.string.winter_chill_why_example_caption),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = RobotoFamily,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                    ),
                    color = Neutral600,
                )
            }

            // Card 2: Claves
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Neutral0)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Key 1: Sevillana y Criolla (Highlighted in gray pill container)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Green200)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Green700),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.winter_chill_why_key1_title),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = RobotoFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                            ),
                            color = Neutral900,
                        )
                        Text(
                            text = stringResource(R.string.winter_chill_why_key1_desc),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = RobotoFamily,
                                fontSize = 12.sp,
                            ),
                            color = Neutral700,
                        )
                    }
                }

                // Key 2: Días sobre 24 °C
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Terracotta500),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.winter_chill_why_key2_title),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = RobotoFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                            ),
                            color = Neutral900,
                        )
                        Text(
                            text = stringResource(R.string.winter_chill_why_key2_desc),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = RobotoFamily,
                                fontSize = 12.sp,
                            ),
                            color = Neutral700,
                        )
                    }
                }

                // Key 3: Brotación · 31 ago
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Green900),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = stringResource(R.string.winter_chill_why_key3_title),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontFamily = RobotoFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                            ),
                            color = Neutral900,
                        )
                        Text(
                            text = stringResource(R.string.winter_chill_why_key3_desc),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = RobotoFamily,
                                fontSize = 12.sp,
                            ),
                            color = Neutral700,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Entendido primary button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(CircleShape)
                    .background(Green900)
                    .clickable(role = Role.Button, onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.winter_chill_why_button_understood),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = RobotoFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                    ),
                    color = Neutral0,
                )
            }
        }
    }
}
