package pe.edu.upc.viora.features.harvestsettlement.presentation.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.VioraVoice
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green700
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest100
import pe.edu.upc.viora.core.designsystem.theme.Harvest800
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.CampaignClosedUiState
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.SettlementReceipt
import pe.edu.upc.viora.features.harvestsettlement.presentation.viewmodel.CampaignClosedViewModel
import pe.edu.upc.viora.features.phenology.presentation.ui.formatKg
import pe.edu.upc.viora.features.phenology.presentation.ui.formatPercent
import pe.edu.upc.viora.features.phenology.presentation.ui.formatTonnesPerHectare
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.labelRes
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.CircleIconButton
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.PrimaryPillButton

/**
 * P73: the receipt of a campaign. "Campaña cerrada" when the server stored the settlement, "Cosecha
 * guardada" while it waits on the phone (with its conflict and failed variants).
 *
 * @param onDone "Listo" and the close button: back to where the producer came from.
 * @param onOpenPlot "Ver expediente del lote".
 * @param onOpenAlternation the "año más estable" tile.
 * @param onCorrect "Corregir los kilos": opens the form prefilled from the pending settlement.
 */
@Composable
fun CampaignClosedScreen(
    onDone: () -> Unit,
    onOpenPlot: (plotId: String) -> Unit,
    onOpenAlternation: (plotId: String, plotName: String) -> Unit,
    onCorrect: (plotId: String, year: Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CampaignClosedViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CampaignClosedContent(
        state = state,
        onDone = onDone,
        onOpenPlot = onOpenPlot,
        onOpenAlternation = onOpenAlternation,
        onCorrect = onCorrect,
        onConflictViewReceipt = viewModel::showExistingReceipt,
        onConflictAcknowledge = { viewModel.acknowledgeConflict(onDone) },
        modifier = modifier,
    )
}

@Composable
internal fun CampaignClosedContent(
    state: CampaignClosedUiState,
    onDone: () -> Unit,
    onOpenPlot: (plotId: String) -> Unit,
    onOpenAlternation: (plotId: String, plotName: String) -> Unit,
    onCorrect: (plotId: String, year: Int) -> Unit,
    onConflictViewReceipt: () -> Unit,
    onConflictAcknowledge: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLow)) {
        when (state) {
            CampaignClosedUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Green900)
            }
            CampaignClosedUiState.Missing -> MissingReceipt(onDone)
            is CampaignClosedUiState.Closed -> ReceiptScreen(
                receipt = state.receipt,
                pending = false,
                failed = false,
                onDone = onDone,
                onOpenPlot = onOpenPlot,
                onOpenAlternation = onOpenAlternation,
                onCorrect = onCorrect,
            )
            is CampaignClosedUiState.Pending -> ReceiptScreen(
                receipt = state.receipt,
                pending = true,
                failed = state.failed,
                onDone = onDone,
                onOpenPlot = onOpenPlot,
                onOpenAlternation = onOpenAlternation,
                onCorrect = onCorrect,
            )
            is CampaignClosedUiState.Conflict -> {
                // Nothing to show behind the dialog but the empty screen: it is the 409 answer.
                AlreadySettledDialog(
                    year = state.campaignYear,
                    plotName = state.plotName,
                    existing = state.existing,
                    onViewReceipt = onConflictViewReceipt,
                    onDismiss = onConflictAcknowledge,
                )
            }
        }
    }
}

@Composable
private fun ReceiptScreen(
    receipt: SettlementReceipt,
    pending: Boolean,
    failed: Boolean,
    onDone: () -> Unit,
    onOpenPlot: (String) -> Unit,
    onOpenAlternation: (String, String) -> Unit,
    onCorrect: (String, Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 16.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            CircleIconButton(icon = R.drawable.ic_close, contentDescription = stringResource(R.string.action_close), onClick = onDone)
            if (pending) {
                StatusChip(R.drawable.ic_cloud_sync, stringResource(R.string.settle_receipt_chip_phone), Harvest100, Harvest800)
            } else {
                StatusChip(R.drawable.ic_menu_book, stringResource(R.string.settle_receipt_chip_logbook), Green200, Green800)
            }
        }
        val headline = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp, lineHeight = 50.sp, letterSpacing = (-1.2).sp)
        Column(Modifier.padding(top = 28.dp)) {
            Text(
                text = stringResource(if (pending) R.string.settle_receipt_pending_lead else R.string.settle_receipt_closed_lead, receipt.campaignYear),
                style = headline,
                color = Neutral900,
            )
            Text(
                text = stringResource(if (pending) R.string.settle_receipt_pending_emphasis else R.string.settle_receipt_closed_emphasis),
                style = headline,
                fontStyle = FontStyle.Italic,
                color = Neutral900,
            )
        }
        VioraVoice(
            text = if (pending) {
                stringResource(R.string.settle_receipt_voice_pending)
            } else {
                stringResource(R.string.settle_receipt_voice_closed, receipt.campaignYear + 1)
            },
            modifier = Modifier.padding(top = 14.dp),
        )
        ReceiptCard(receipt, pending, failed, Modifier.padding(top = 24.dp))
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val calibre = receipt.calibre
            if (calibre != null) {
                CalibreTile(calibre, receipt, pending, Modifier.weight(1f))
            }
            StableYearTile(
                year = receipt.campaignYear + 1,
                onClick = if (pending) null else ({ onOpenAlternation(receipt.plotId, receipt.plotName) }),
                modifier = Modifier.weight(1f),
            )
        }
        PrimaryPillButton(
            text = stringResource(R.string.settle_receipt_done),
            onClick = onDone,
            modifier = Modifier.padding(top = 36.dp),
            trailingIcon = R.drawable.ic_check,
        )
        Text(
            text = stringResource(if (pending) R.string.settle_receipt_correct else R.string.settle_receipt_view_plot),
            style = MaterialTheme.typography.titleSmall,
            color = Green800,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button) {
                    if (pending) onCorrect(receipt.plotId, receipt.campaignYear) else onOpenPlot(receipt.plotId)
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        )
    }
}

@Composable
private fun StatusChip(@DrawableRes icon: Int, text: String, background: Color, content: Color) {
    Row(
        modifier = Modifier.clip(CircleShape).background(background).padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(Neutral0), contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), contentDescription = null, tint = Green900, modifier = Modifier.size(24.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, lineHeight = 18.sp), color = content)
    }
}

/** The receipt: total, plot line, green / black split, a perforation and the status footer. */
@Composable
private fun ReceiptCard(receipt: SettlementReceipt, pending: Boolean, failed: Boolean, modifier: Modifier = Modifier) {
    val notchColor = MaterialTheme.colorScheme.surfaceContainerLow
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Neutral0)
            .drawWithContent {
                drawContent()
                // Tear-off notches at both ends of the perforation line (the footer row is the card's last 50 dp).
                val y = size.height - NOTCH_FROM_BOTTOM.toPx()
                drawCircle(notchColor, radius = 11.dp.toPx(), center = Offset(0f, y))
                drawCircle(notchColor, radius = 11.dp.toPx(), center = Offset(size.width, y))
                drawLine(
                    color = Neutral200,
                    start = Offset(24.dp.toPx(), y),
                    end = Offset(size.width - 24.dp.toPx(), y),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx())),
                    cap = Stroke.DefaultCap,
                )
            }
            .padding(start = 22.dp, end = 22.dp, top = 20.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = stringResource(if (pending) R.string.settle_receipt_label_pending else R.string.settle_receipt_label_closed),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.88.sp),
                color = Neutral600,
            )
            when {
                pending -> ReceiptPill(
                    stringResource(if (failed) R.string.settle_receipt_status_failed else R.string.settle_receipt_status_pending),
                    Harvest100,
                    Harvest800,
                )
                receipt.receiptNumber != null -> ReceiptPill(stringResource(R.string.settle_receipt_number, receipt.receiptNumber), MaterialTheme.colorScheme.surface, Neutral700)
            }
        }
        Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = formatKg(receipt.totalKg),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp, lineHeight = 70.sp, letterSpacing = (-1.92).sp),
                color = Neutral900,
                modifier = Modifier.alignByBaseline(),
            )
            Text(
                text = stringResource(R.string.settle_receipt_unit),
                style = MaterialTheme.typography.headlineLarge.copy(fontStyle = FontStyle.Italic),
                color = Neutral900,
                modifier = Modifier.alignByBaseline(),
            )
        }
        Text(
            text = receiptLine(receipt),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
            color = Neutral700,
            modifier = Modifier.padding(top = 4.dp),
        )
        SplitBar(receipt.greenShare, Modifier.padding(top = 16.dp))
        Row(Modifier.fillMaxWidth().padding(top = 10.dp)) {
            QualityKilos(R.string.settle_green_label, Green700, receipt.greenKg, receipt.greenShare, Modifier.weight(1f))
            QualityKilos(R.string.settle_black_label, Neutral900, receipt.blackKg, receipt.greenShare?.let { 1 - it }, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth().height(FOOTER_HEIGHT),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                painter = painterResource(if (pending) R.drawable.ic_cloud_sync else R.drawable.ic_task_alt),
                contentDescription = null,
                tint = Neutral600,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = when {
                    pending && failed -> stringResource(R.string.settle_receipt_footer_failed)
                    pending -> stringResource(R.string.settle_receipt_footer_pending)
                    else -> stringResource(R.string.settle_receipt_footer_closed, formatSettleDate(receipt.weighedOn, R.string.settle_pattern_full))
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (pending && failed) Terracotta700 else Neutral600,
            )
        }
    }
}

/** "La Yarada 02 · 8,3 t/ha · pesado el 14 de mayo"; the parts that are not known are left out. */
@Composable
private fun receiptLine(receipt: SettlementReceipt): String = listOfNotNull(
    receipt.plotName.takeIf { it.isNotBlank() },
    receipt.tonnesPerHectare?.let { stringResource(R.string.settle_receipt_tonnes, formatTonnesPerHectare(it)) },
    stringResource(R.string.settle_receipt_weighed, formatSettleDate(receipt.weighedOn, R.string.settle_pattern_day)),
).joinToString(" · ")

@Composable
private fun ReceiptPill(text: String, background: Color, content: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
        color = content,
        modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(background).padding(horizontal = 10.dp, vertical = 4.dp),
    )
}

@Composable
private fun QualityKilos(label: Int, dot: Color, kilos: Double, share: Double?, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.padding(top = 5.dp).size(8.dp).clip(CircleShape).background(dot))
        Column {
            Text(stringResource(label), style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp), color = Neutral900)
            Text(
                text = if (share != null) {
                    stringResource(R.string.settle_receipt_part, formatKg(kilos), formatPercent(share))
                } else {
                    stringResource(R.string.settle_total_value, formatKg(kilos))
                },
                style = MaterialTheme.typography.bodySmall,
                color = Neutral600,
            )
        }
    }
}

@Composable
private fun CalibreTile(calibre: String, receipt: SettlementReceipt, pending: Boolean, modifier: Modifier = Modifier) {
    Column(modifier.height(TILE_HEIGHT).clip(RoundedCornerShape(28.dp)).background(Green200).padding(18.dp)) {
        Text(
            text = calibre,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 34.sp, lineHeight = 40.sp, letterSpacing = (-0.68).sp),
            color = Green900,
        )
        Text(
            text = stringResource(R.string.settle_receipt_calibre_label),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
            color = Green800,
            modifier = Modifier.padding(top = 10.dp),
        )
        val hint = when {
            pending -> stringResource(R.string.settle_receipt_calibre_hint_pending)
            receipt.variety != null -> stringResource(R.string.settle_receipt_calibre_hint, stringResource(receipt.variety.labelRes()))
            else -> null
        }
        if (hint != null) Text(hint, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = Green800.copy(alpha = 0.75f))
    }
}

/** "{year} · año más estable · ver mi alternancia": static copy; it opens the alternation when [onClick] is set. */
@Composable
private fun StableYearTile(year: Int, onClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(TILE_HEIGHT)
            .clip(RoundedCornerShape(28.dp))
            .background(Harvest100)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                text = year.toString(),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 40.sp, lineHeight = 44.sp, letterSpacing = (-0.8).sp),
                color = Neutral900,
            )
            Text(
                text = stringResource(R.string.settle_receipt_stable_label),
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp),
                color = Harvest800,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                text = stringResource(R.string.settle_receipt_stable_hint),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = Harvest800.copy(alpha = 0.75f),
            )
        }
        Box(
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 16.dp, end = 16.dp).size(36.dp).clip(CircleShape).background(Neutral0),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_forward),
                contentDescription = null,
                tint = Terracotta700,
                modifier = Modifier.size(20.dp).rotate(-45f),
            )
        }
    }
}

@Composable
private fun MissingReceipt(onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Text(stringResource(R.string.settle_receipt_missing_title), style = MaterialTheme.typography.headlineLarge, color = Neutral900)
        Text(stringResource(R.string.settle_receipt_missing_body), style = MaterialTheme.typography.bodyMedium, color = Neutral700)
        PrimaryPillButton(text = stringResource(R.string.settle_receipt_done), onClick = onDone, modifier = Modifier.padding(top = 12.dp))
    }
}

private val TILE_HEIGHT = 120.dp
private val NOTCH_FROM_BOTTOM = 50.dp
private val FOOTER_HEIGHT = NOTCH_FROM_BOTTOM
