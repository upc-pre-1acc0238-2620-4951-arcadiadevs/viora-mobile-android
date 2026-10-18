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
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.ZoneId
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Harvest400
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.core.presentation.formatDecimal
import pe.edu.upc.viora.core.presentation.messageRes
import pe.edu.upc.viora.features.croploadregulation.presentation.ui.newsreader
import pe.edu.upc.viora.features.croploadregulation.presentation.ui.roboto
import pe.edu.upc.viora.features.croploadregulation.presentation.ui.robotoMedium
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.AgronomicDossier
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.AgronomicDossierUiState
import pe.edu.upc.viora.features.harvestsettlement.presentation.viewmodel.AgronomicDossierViewModel
import pe.edu.upc.viora.features.phenology.presentation.ui.formatKg
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.formatHectares
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.CircleIconButton
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.PrimaryPillButton

/**
 * P76 · Expediente del lote (US30): the sealed dossier of a settled campaign, its SHA-256 and the way to its PDF.
 * The code card opens P78, "Descargar PDF" and "Compartir" open P77 once the PDF is on the phone. Without signal it is
 * the "Sin conexión" variant: the PDF cannot be downloaded and the last one downloaded is offered instead.
 */
@Composable
fun AgronomicDossierScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AgronomicDossierViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pdf by viewModel.pdfSheet.collectAsStateWithLifecycle()
    val verification by viewModel.verification.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLow)) {
        when (val s = state) {
            AgronomicDossierUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Green900)
            }
            is AgronomicDossierUiState.NoDossier -> MessageScreen(
                onBack = onBack,
                lead = stringResource(R.string.dossier_empty_lead),
                emphasis = stringResource(R.string.dossier_empty_emphasis),
                body = stringResource(R.string.dossier_empty_body, s.plotName),
            )
            is AgronomicDossierUiState.Error -> MessageScreen(
                onBack = onBack,
                body = stringResource(s.error.messageRes()),
                onRetry = viewModel::load,
            )
            is AgronomicDossierUiState.Content -> {
                DossierContent(
                    state = s,
                    onBack = onBack,
                    onVerify = viewModel::verify,
                    onOpenPdf = viewModel::openPdf,
                )
                pdf?.let { file ->
                    DossierPdfSheet(dossier = s.dossier, file = file, onDismiss = viewModel::closePdf)
                }
                verification?.let { answer ->
                    DossierVerificationSheet(dossier = s.dossier, state = answer, onDismiss = viewModel::closeVerification)
                }
            }
        }
    }
}

@Composable
private fun DossierContent(
    state: AgronomicDossierUiState.Content,
    onBack: () -> Unit,
    onVerify: () -> Unit,
    onOpenPdf: () -> Unit,
) {
    val dossier = state.dossier
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 24.dp),
    ) {
        TopBar(
            title = stringResource(R.string.dossier_title),
            subtitle = stringResource(R.string.dossier_subtitle, dossier.plotName, dossier.campaignYear),
            onBack = onBack,
        )
        if (state.offline) OfflineChip(Modifier.padding(top = 24.dp))
        DocumentCard(dossier, Modifier.padding(top = if (state.offline) 12.dp else 24.dp))
        CodeCard(dossier.verificationHash, onClick = onVerify, modifier = Modifier.padding(top = 14.dp))
        PrimaryPillButton(
            text = stringResource(R.string.dossier_download),
            onClick = onOpenPdf,
            trailingIcon = R.drawable.ic_download,
            enabled = !state.offline,
            isLoading = state.downloading,
            modifier = Modifier.padding(top = 16.dp),
        )
        val locale = LocalConfiguration.current.locales[0]
        val lastPdfDay = state.lastPdfSavedAt?.let { formatDay(it.atZone(ZoneId.systemDefault()).toLocalDate(), locale) }
        when {
            !state.offline -> WhitePillButton(R.drawable.ic_share, stringResource(R.string.dossier_share), onOpenPdf, Modifier.padding(top = 10.dp))
            state.lastPdf != null && lastPdfDay != null ->
                WhitePillButton(R.drawable.ic_visibility, stringResource(R.string.dossier_open_last, lastPdfDay), onOpenPdf, Modifier.padding(top = 10.dp))
        }
        if (state.downloadFailed && !state.offline) {
            Text(
                text = stringResource(R.string.dossier_download_failed),
                style = roboto(13.sp, 18.sp),
                color = Terracotta700,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )
        }
    }
}

/** Back on the left and the title centred over the plot and campaign, as every lot screen. */
@Composable
private fun TopBar(title: String, subtitle: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        CircleIconButton(icon = R.drawable.ic_arrow_back, contentDescription = stringResource(R.string.action_back), onClick = onBack)
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Text(title, style = robotoMedium(15.sp, 20.sp), color = Neutral900, maxLines = 1)
            Text(subtitle, style = roboto(12.sp, 16.sp), color = Neutral600, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        // Keeps the title centred on the screen, as the 48 dp back button does on the left.
        Spacer(Modifier.size(48.dp))
    }
}

/** "Sin conexión · el PDF sellado necesita internet": the dark pill of the offline variant. */
@Composable
private fun OfflineChip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clip(CircleShape).background(Neutral900).padding(start = 12.dp, end = 14.dp, top = 9.dp, bottom = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(R.drawable.ic_cloud_sync), contentDescription = null, tint = Neutral0, modifier = Modifier.size(18.dp))
        Text(stringResource(R.string.dossier_offline), style = robotoMedium(12.sp, 16.sp), color = Neutral0)
    }
}

/** The preview of the PDF: its green cover with the plot and holder, then the summary of what it states. */
@Composable
private fun DocumentCard(dossier: AgronomicDossier, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = shape, ambientColor = CardShadow, spotColor = CardShadow)
            .clip(shape)
            .background(Neutral0),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().background(Green900).padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(stringResource(R.string.dossier_eyebrow), style = robotoMedium(11.sp, 16.sp).copy(letterSpacing = 0.88.sp), color = Harvest400)
            Text(dossier.plotName, style = newsreader(34.sp, 38.sp), color = Neutral0)
            // The cooperative is not part of the dossier (no data source): only the holder is written.
            dossier.holderName?.let {
                Text(it, style = newsreader(16.sp, 20.sp), fontStyle = FontStyle.Italic, color = Neutral50)
            }
        }
        Column(Modifier.fillMaxWidth().padding(start = 22.dp, end = 22.dp, top = 10.dp, bottom = 18.dp)) {
            val rows = summaryRows(dossier)
            rows.forEachIndexed { index, (label, value) ->
                SummaryRow(label, value)
                if (index < rows.lastIndex) HorizontalDivider(thickness = 1.dp, color = Neutral200)
            }
        }
    }
}

@Composable
private fun summaryRows(dossier: AgronomicDossier): List<Pair<String, String>> {
    val locale = LocalConfiguration.current.locales[0]
    val separator = " · "
    val none = stringResource(R.string.dossier_no_record)

    val variety = listOfNotNull(
        dossier.variety?.let { varietyLabel(it) },
        dossier.areaHectares?.let { formatHectares(it) + " ha" },
    ).joinToString(separator).ifEmpty { none }

    val load = dossier.fruitLoad?.preThinningFruitsPerShoot?.let { perShoot ->
        listOfNotNull(
            stringResource(R.string.dossier_load_value, formatDecimal(perShoot, 2)),
            dossier.fruitLoad.overTargetPercentage?.let { formatOverTarget(it) },
        ).joinToString(separator)
    } ?: none

    val prescription = dossier.prescription?.removalPercentage?.let { share ->
        val opens = dossier.prescription.windowOpensOn
        val closes = dossier.prescription.windowClosesOn
        listOfNotNull(
            stringResource(R.string.dossier_prescription_value, formatShare(share)),
            if (opens != null && closes != null) formatWindow(opens, closes, locale) else null,
        ).joinToString(separator)
    } ?: none

    val thinning = dossier.thinning?.takeIf { it.recorded }?.let { done ->
        listOfNotNull(done.actualRemovalPercentage?.let { formatShare(it) }, done.executedOn?.let { formatDay(it, locale) })
            .joinToString(separator).ifEmpty { null }
    } ?: none

    val harvest = dossier.harvest?.totalKg?.let { kg ->
        listOfNotNull(
            stringResource(R.string.dossier_harvest_kg, formatKg(kg)),
            dossier.harvest.receiptNumber?.let { stringResource(R.string.dossier_harvest_receipt, it) },
        ).joinToString(separator)
    } ?: none

    val climate = dossier.climate?.let { c ->
        listOfNotNull(
            c.linkedNodes?.let { pluralStringResource(R.plurals.dossier_climate_nodes, it, it) },
            c.daysWithReadings?.let { pluralStringResource(R.plurals.dossier_climate_days, it, it) },
        ).joinToString(separator).ifEmpty { null }
    } ?: none

    return listOf(
        stringResource(R.string.dossier_row_campaign) to stringResource(R.string.dossier_campaign_closed, dossier.campaignYear),
        stringResource(R.string.dossier_row_variety) to variety,
        stringResource(R.string.dossier_row_load) to load,
        stringResource(R.string.dossier_row_prescription) to prescription,
        stringResource(R.string.dossier_row_thinning) to thinning,
        stringResource(R.string.dossier_row_harvest) to harvest,
        stringResource(R.string.dossier_row_climate) to climate,
    )
}

/** "Campaña ............ 2026 · cerrada": the label on the left, the value on the right. */
@Composable
internal fun SummaryRow(label: String, value: String, verticalPadding: Int = 11) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = verticalPadding.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = roboto(13.sp, 18.sp), color = Neutral600)
        Text(
            text = value,
            style = robotoMedium(13.sp, 18.sp),
            color = Neutral900,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
    }
}

/** The SHA-256 with its "Íntegro" pill; a tap opens P78 to check it against the Viora record. */
@Composable
private fun CodeCard(hash: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Green200)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            // Narrow phones shrink the eyebrow instead of wrapping it under the pill.
            Text(
                text = stringResource(R.string.dossier_code_eyebrow),
                style = robotoMedium(11.sp, 16.sp).copy(letterSpacing = 0.88.sp),
                color = Green900,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 11.sp),
                modifier = Modifier.weight(1f, fill = false),
            )
            Row(
                modifier = Modifier.padding(start = 8.dp).clip(CircleShape).background(Neutral0).padding(start = 8.dp, end = 10.dp, top = 4.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = Green900, modifier = Modifier.size(14.dp))
                Text(stringResource(R.string.dossier_code_intact), style = robotoMedium(11.sp, 14.sp), color = Green900)
            }
        }
        HashText(hash)
        Text(stringResource(R.string.dossier_code_body), style = roboto(12.sp, 16.sp), color = Green900)
    }
}

/** The whole SHA-256, four groups of four characters a line. */
@Composable
internal fun HashText(hash: String, modifier: Modifier = Modifier) {
    Text(
        text = formatHash(hash),
        style = robotoMedium(20.sp, 28.sp).copy(letterSpacing = 1.2.sp),
        color = Neutral900,
        modifier = modifier,
    )
}

/** The white 56 dp pill with an icon: "Compartir", "Abrir la última versión", "Compartir por WhatsApp". */
@Composable
internal fun WhitePillButton(@DrawableRes icon: Int, text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(CircleShape)
            .background(Neutral0)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = Neutral900, modifier = Modifier.size(20.dp))
        Text(text, style = robotoMedium(15.sp, 20.sp), color = Neutral900)
    }
}

/** Empty and error screens: a back button, the headline or message and, for errors, "Reintentar". */
@Composable
private fun MessageScreen(
    onBack: () -> Unit,
    body: String,
    lead: String? = null,
    emphasis: String? = null,
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 24.dp),
    ) {
        CircleIconButton(icon = R.drawable.ic_arrow_back, contentDescription = stringResource(R.string.action_back), onClick = onBack)
        if (lead != null && emphasis != null) {
            Column(Modifier.padding(top = 32.dp)) {
                Text(lead, style = newsreader(34.sp, 38.sp), color = Neutral900)
                Text(emphasis, style = newsreader(34.sp, 38.sp), fontStyle = FontStyle.Italic, color = Neutral900)
            }
            Text(body, style = roboto(14.sp, 20.sp), color = Neutral700, modifier = Modifier.padding(top = 12.dp))
        } else {
            Spacer(Modifier.weight(1f))
            Text(body, style = MaterialTheme.typography.bodyLarge, color = Neutral700, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            if (onRetry != null) {
                PrimaryPillButton(text = stringResource(R.string.dossier_retry), onClick = onRetry, modifier = Modifier.padding(top = 16.dp))
            }
            Spacer(Modifier.weight(1f))
        }
    }
}

private val CardShadow = Color(0x141A1F1A)
