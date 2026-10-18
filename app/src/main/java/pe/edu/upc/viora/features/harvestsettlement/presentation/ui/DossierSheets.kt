package pe.edu.upc.viora.features.harvestsettlement.presentation.ui

import android.content.ClipData
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import kotlinx.coroutines.launch
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.theme.Green200
import pe.edu.upc.viora.core.designsystem.theme.Green800
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral600
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.croploadregulation.presentation.ui.newsreader
import pe.edu.upc.viora.features.croploadregulation.presentation.ui.roboto
import pe.edu.upc.viora.features.croploadregulation.presentation.ui.robotoMedium
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.AgronomicDossier
import pe.edu.upc.viora.features.harvestsettlement.presentation.state.DossierVerificationState
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.formatHectares
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.CircleIconButton
import pe.edu.upc.viora.features.plotmanagement.presentation.ui.registration.PrimaryPillButton

/**
 * P77 · PDF listo: the sealed PDF already on the phone, to open it, send it by WhatsApp with its code, or save a copy
 * where the producer chooses. Every action closes the sheet, as in the prototype.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DossierPdfSheet(dossier: AgronomicDossier, file: File, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val shareText = stringResource(R.string.dossier_share_text, dossier.plotName, dossier.campaignYear, dossier.verificationHash)
    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { target ->
        if (target != null) DossierFiles.copyTo(context, file, target)
        onDismiss()
    }
    DossierSheet(onDismiss = onDismiss) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SerifTitle(stringResource(R.string.dossier_pdf_title_lead), stringResource(R.string.dossier_pdf_title_emphasis))
                Text(
                    stringResource(R.string.dossier_pdf_sealed_on, formatSealedAt(dossier.sealedAt)),
                    style = roboto(13.sp, 18.sp),
                    color = Neutral600,
                )
            }
            CircleIconButton(icon = R.drawable.ic_close, contentDescription = stringResource(R.string.action_close), onClick = onDismiss)
        }
        FileCard(dossier, file, Modifier.padding(top = 24.dp))
        PrimaryPillButton(
            text = stringResource(R.string.dossier_pdf_open),
            onClick = {
                DossierFiles.open(context, file)
                onDismiss()
            },
            trailingIcon = R.drawable.ic_visibility,
            modifier = Modifier.padding(top = 16.dp),
        )
        WhitePillButton(
            icon = R.drawable.ic_chat_bubble,
            text = stringResource(R.string.dossier_pdf_whatsapp),
            onClick = {
                DossierFiles.shareOnWhatsApp(context, file, shareText)
                onDismiss()
            },
            modifier = Modifier.padding(top = 10.dp),
        )
        TextAction(stringResource(R.string.dossier_pdf_save), onClick = { saveLauncher.launch(dossier.fileName) })
    }
}

/** The file: a miniature of the first page, its name, pages, size and seal. */
@Composable
private fun FileCard(dossier: AgronomicDossier, file: File, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Neutral0).padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(R.drawable.ill_dossier_pdf), contentDescription = null, modifier = Modifier.size(width = 58.dp, height = 76.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(dossier.fileName, style = robotoMedium(14.sp, 17.sp), color = Neutral900, maxLines = 1, overflow = TextOverflow.Ellipsis)
            val details = listOfNotNull(
                dossier.pageCount?.let { pluralStringResource(R.plurals.dossier_pages, it, it) },
                formatFileSize(dossier.sizeBytes ?: file.length()),
                stringResource(R.string.dossier_pdf_seal),
            ).joinToString(" · ")
            Text(details, style = roboto(12.sp, 15.sp), color = Neutral600)
        }
    }
}

/**
 * P78 · Verificar autenticidad: the dossier's code checked against the Viora record. The headline only says the
 * dossier is authentic when the record answered that it matches.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DossierVerificationSheet(dossier: AgronomicDossier, state: DossierVerificationState, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val copied = stringResource(R.string.dossier_verify_copied)
    val verified = (state as? DossierVerificationState.Matches)?.verification
    DossierSheet(onDismiss = onDismiss) {
        Text(
            stringResource(R.string.dossier_code_eyebrow),
            style = robotoMedium(11.sp, 14.sp).copy(letterSpacing = 0.88.sp),
            color = Neutral600,
            modifier = Modifier.padding(top = 2.dp),
        )
        val (lead, emphasis) = when (state) {
            is DossierVerificationState.Matches -> R.string.dossier_verify_title_lead to R.string.dossier_verify_title_emphasis
            DossierVerificationState.NoMatch -> R.string.dossier_verify_unknown_lead to R.string.dossier_verify_unknown_emphasis
            else -> R.string.dossier_verify_pending_lead to R.string.dossier_verify_pending_emphasis
        }
        SerifTitle(stringResource(lead), stringResource(emphasis), Modifier.padding(top = 8.dp))
        Text(
            stringResource(R.string.dossier_verify_body),
            style = roboto(14.sp, 20.sp),
            color = Neutral700,
            modifier = Modifier.padding(top = 10.dp),
        )
        // The record's own data when it answered; otherwise what the phone's dossier states.
        Column(
            modifier = Modifier.padding(top = 16.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Neutral0)
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            val plotName = verified?.plotName ?: dossier.plotName
            val area = verified?.areaHectares ?: dossier.areaHectares
            val holder = verified?.holderName ?: dossier.holderName
            val pages = verified?.pageCount ?: dossier.pageCount
            val fromYear = verified?.historyFromYear ?: dossier.history?.fromYear
            val toYear = verified?.historyToYear ?: dossier.history?.toYear
            val rows = listOfNotNull(
                stringResource(R.string.dossier_verify_row_plot) to listOfNotNull(plotName, area?.let { formatHectares(it) + " ha" }).joinToString(" · "),
                holder?.let { stringResource(R.string.dossier_verify_row_holder) to it },
                stringResource(R.string.dossier_verify_row_issued) to formatSealedAt(verified?.sealedAt ?: dossier.sealedAt),
                listOfNotNull(
                    pages?.let { pluralStringResource(R.plurals.dossier_pages, it, it) },
                    when {
                        fromYear == null || toYear == null -> null
                        fromYear == toYear -> "$toYear"
                        else -> "$fromYear–$toYear"
                    },
                ).takeIf { it.isNotEmpty() }?.let { stringResource(R.string.dossier_verify_row_content) to it.joinToString(" · ") },
            )
            rows.forEachIndexed { index, (label, value) ->
                SummaryRow(label, value, verticalPadding = 10)
                if (index < rows.lastIndex) HorizontalDivider(thickness = 1.dp, color = Neutral200)
            }
        }
        Column(
            modifier = Modifier.padding(top = 16.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Green200)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            HashText(dossier.verificationHash)
            RecordAnswer(state)
        }
        PrimaryPillButton(
            text = stringResource(R.string.dossier_verify_copy),
            onClick = {
                scope.launch {
                    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("SHA-256", dossier.verificationHash)))
                    Toast.makeText(context, copied, Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            },
            trailingIcon = R.drawable.ic_edit_note,
            modifier = Modifier.padding(top = 16.dp),
        )
        TextAction(stringResource(R.string.dossier_verify_done), onClick = onDismiss)
    }
}

/** "Coincide con el registro de Viora", or why it could not be said. */
@Composable
private fun RecordAnswer(state: DossierVerificationState) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        when (state) {
            DossierVerificationState.Checking -> CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = Green900)
            is DossierVerificationState.Matches -> Icon(painterResource(R.drawable.ic_task_alt), null, tint = Green800, modifier = Modifier.size(18.dp))
            DossierVerificationState.NoMatch, DossierVerificationState.Unreachable ->
                Icon(painterResource(R.drawable.ic_cloud_sync), null, tint = Terracotta700, modifier = Modifier.size(18.dp))
        }
        Text(
            text = stringResource(
                when (state) {
                    DossierVerificationState.Checking -> R.string.dossier_verify_checking
                    is DossierVerificationState.Matches -> R.string.dossier_verify_matches
                    DossierVerificationState.NoMatch -> R.string.dossier_verify_no_match
                    DossierVerificationState.Unreachable -> R.string.dossier_verify_offline
                },
            ),
            style = robotoMedium(13.sp, 16.sp),
            color = if (state is DossierVerificationState.Matches || state == DossierVerificationState.Checking) Green900 else Terracotta700,
        )
    }
}

/** The sheets of P76: cream, 32 dp top corners, the 36 dp handle, opened fully. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DossierSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = { Box(Modifier.padding(top = 12.dp).size(width = 36.dp, height = 4.dp).clip(CircleShape).background(Neutral300)) },
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).navigationBarsPadding().padding(horizontal = 24.dp).padding(top = 14.dp, bottom = 8.dp),
            content = content,
        )
    }
}

/** "Tu PDF" over an italic "está listo.". */
@Composable
private fun SerifTitle(lead: String, emphasis: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(lead, style = newsreader(34.sp, 38.sp), color = Neutral900)
        Text(emphasis, style = newsreader(34.sp, 38.sp), fontStyle = FontStyle.Italic, color = Neutral900)
    }
}

/** The plain centred action under the buttons: "Guardar en el teléfono", "Entendido". */
@Composable
private fun ColumnScope.TextAction(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = robotoMedium(14.sp, 18.sp),
        color = Neutral900,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(top = 6.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    )
}
