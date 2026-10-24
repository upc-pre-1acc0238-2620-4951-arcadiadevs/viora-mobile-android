package pe.edu.upc.viora.features.profiles.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.EditorialHeadline
import pe.edu.upc.viora.core.designsystem.theme.Green900
import pe.edu.upc.viora.core.designsystem.theme.Neutral0
import pe.edu.upc.viora.core.designsystem.theme.Neutral100
import pe.edu.upc.viora.core.designsystem.theme.Neutral300
import pe.edu.upc.viora.core.designsystem.theme.Neutral50
import pe.edu.upc.viora.core.designsystem.theme.Neutral700
import pe.edu.upc.viora.core.designsystem.theme.Neutral900
import pe.edu.upc.viora.core.designsystem.theme.NewsreaderFamily
import pe.edu.upc.viora.core.designsystem.theme.Terracotta100
import pe.edu.upc.viora.core.designsystem.theme.Terracotta500
import pe.edu.upc.viora.core.designsystem.theme.Terracotta700
import pe.edu.upc.viora.features.profiles.presentation.state.DeleteAccountError
import pe.edu.upc.viora.features.profiles.presentation.state.DeleteAccountUiState
import pe.edu.upc.viora.features.profiles.presentation.viewmodel.DeleteAccountViewModel

/**
 * P100 · Eliminar cuenta (Figma section 21): what is deleted for good, a last look before leaving (the dossiers and
 * the plan), the password and the "Entiendo" box. The button opens the confirmation sheet; once the server deletes
 * the account [onDeleted] receives the first name for the farewell (P101).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAccountScreen(
    onBack: () -> Unit,
    onOpenLogbook: () -> Unit,
    onDeleted: (firstName: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DeleteAccountViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    LaunchedEffect(state.isDeleted) { if (state.isDeleted) onDeleted(state.firstName) }

    // imePadding shrinks the page, so the password field rises above the keyboard instead of hiding under it.
    Column(modifier.fillMaxSize().background(Neutral100).statusBarsPadding().imePadding()) {
        AccountTopBar(
            title = stringResource(R.string.delete_account_title),
            subtitle = stringResource(R.string.account_title),
            onBack = onBack,
        )
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AccountGutter)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            EditorialHeadline(
                lead = stringResource(R.string.delete_account_headline_lead),
                emphasis = stringResource(R.string.delete_account_headline_emphasis),
            )
            WhatIsDeletedCard(state)
            AccountSection(title = stringResource(R.string.delete_account_section_before)) {
                AccountRow(
                    label = stringResource(R.string.delete_account_download),
                    value = stringResource(R.string.delete_account_download_value),
                    showDivider = state.hasActivePlan == true,
                    onClick = onOpenLogbook,
                )
                if (state.hasActivePlan == true) {
                    AccountRow(
                        label = stringResource(R.string.delete_account_plan),
                        value = stringResource(R.string.delete_account_plan_value),
                    )
                }
            }
            ValidatedFieldCard(
                label = stringResource(R.string.delete_account_password_label),
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                isSecret = true,
                errorText = when (state.error) {
                    DeleteAccountError.WRONG_PASSWORD -> stringResource(R.string.delete_account_error_password)
                    else -> null
                },
                imeAction = ImeAction.Done,
                onImeAction = { focusManager.clearFocus() },
            )
            UnderstoodBox(checked = state.understood, onChange = viewModel::onUnderstoodChange)
            when (state.error) {
                DeleteAccountError.PROTECTED_ACCOUNT -> FieldNote(stringResource(R.string.delete_account_error_protected), isError = true)
                DeleteAccountError.OFFLINE -> FieldNote(stringResource(R.string.delete_account_error_offline), isError = true)
                DeleteAccountError.GENERIC -> FieldNote(stringResource(R.string.delete_account_error_generic), isError = true)
                else -> Unit
            }
            DestructiveButton(
                label = stringResource(R.string.delete_account_action),
                enabled = state.canDelete,
                loading = state.isDeleting,
                onClick = viewModel::askConfirmation,
            )
            Text(
                stringResource(R.string.delete_account_keep),
                style = MaterialTheme.typography.titleMedium,
                color = Neutral900,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onBack)
                    .padding(vertical = 14.dp),
            )
            Spacer(Modifier.height(16.dp))
        }
    }

    if (state.isConfirming) {
        ModalBottomSheet(
            onDismissRequest = viewModel::dismissConfirmation,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Neutral50,
        ) {
            ConfirmationSheet(state, onConfirm = viewModel::confirm, onKeep = viewModel::dismissConfirmation)
        }
    }
}

@Composable
private fun WhatIsDeletedCard(state: DeleteAccountUiState) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Terracotta100).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(Neutral0), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.ic_priority_high), contentDescription = null, tint = Terracotta700, modifier = Modifier.size(20.dp))
            }
            Text(
                stringResource(R.string.delete_account_warning_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = NewsreaderFamily, fontSize = 24.sp),
                color = Terracotta700,
            )
        }
        Text(stringResource(R.string.delete_account_warning_body), style = MaterialTheme.typography.bodyMedium, color = Neutral700)
        val items = listOf(
            pluralStringResource(R.plurals.delete_account_item_plots, state.plotCount, state.plotCount),
            stringResource(R.string.delete_account_item_records),
            stringResource(R.string.delete_account_item_notes),
            stringResource(R.string.delete_account_item_profile),
        )
        items.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.padding(top = 10.dp).width(14.dp).height(1.dp).background(Terracotta500))
                Text(item, style = MaterialTheme.typography.bodyMedium, color = Neutral900)
            }
        }
    }
}

@Composable
private fun UnderstoodBox(checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Checkbox) { onChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (checked) Terracotta700 else Neutral0)
                .border(1.5.dp, if (checked) Terracotta700 else Neutral300, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) Icon(painterResource(R.drawable.ic_check), contentDescription = null, tint = Neutral0, modifier = Modifier.size(16.dp))
        }
        Text(stringResource(R.string.delete_account_understood), style = MaterialTheme.typography.bodyMedium, color = Neutral900)
    }
}

@Composable
private fun DestructiveButton(label: String, enabled: Boolean, loading: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(CircleShape)
            .background(if (enabled || loading) Terracotta700 else Terracotta700.copy(alpha = 0.35f))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(color = Neutral50, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
        } else {
            Text(label, style = MaterialTheme.typography.titleMedium, color = Neutral50)
        }
    }
}

@Composable
private fun ConfirmationSheet(state: DeleteAccountUiState, onConfirm: () -> Unit, onKeep: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val style = MaterialTheme.typography.headlineMedium.copy(fontFamily = NewsreaderFamily, fontSize = 32.sp, lineHeight = 36.sp)
            Text(stringResource(R.string.delete_account_sheet_lead), style = style, color = Neutral900, textAlign = TextAlign.Center)
            Text(
                stringResource(R.string.delete_account_sheet_emphasis),
                style = style,
                fontStyle = FontStyle.Italic,
                color = Neutral900,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            pluralStringResource(R.plurals.delete_account_sheet_body, state.plotCount, state.plotCount),
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral700,
            textAlign = TextAlign.Center,
        )
        DestructiveButton(stringResource(R.string.delete_account_sheet_confirm), enabled = true, loading = false, onClick = onConfirm)
        Box(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(CircleShape)
                .border(1.dp, Neutral300, CircleShape)
                .clickable(role = Role.Button, onClick = onKeep),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(R.string.delete_account_sheet_keep), style = MaterialTheme.typography.titleMedium, color = Neutral900)
        }
    }
}

/**
 * P101 · Cuenta eliminada: the farewell after the deletion, with the same shape and isotype as the payment results.
 * The session is already closed; [onDone] returns to the welcome (01).
 */
@Composable
fun AccountDeletedScreen(firstName: String, onDone: () -> Unit, modifier: Modifier = Modifier) {
    BackHandler(onBack = onDone)
    Column(
        modifier.fillMaxSize().background(Neutral50).statusBarsPadding().navigationBarsPadding().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(76.dp))
        Box(Modifier.size(width = 220.dp, height = 200.dp), contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.payment_result_shape), contentDescription = null, modifier = Modifier.fillMaxSize())
            Icon(painterResource(R.drawable.ic_isotype), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(100.dp))
        }
        Spacer(Modifier.height(32.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(Terracotta500))
            Text(
                stringResource(R.string.account_deleted_eyebrow).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
                color = Neutral700,
            )
        }
        Spacer(Modifier.height(12.dp))
        val titleStyle = MaterialTheme.typography.displaySmall.copy(fontFamily = NewsreaderFamily, fontSize = 44.sp, lineHeight = 48.sp)
        Text(stringResource(R.string.account_deleted_lead), style = titleStyle, color = Neutral900, textAlign = TextAlign.Center)
        if (firstName.isNotBlank()) {
            Text(stringResource(R.string.account_deleted_name, firstName), style = titleStyle, fontStyle = FontStyle.Italic, color = Neutral900, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(14.dp))
        Text(
            stringResource(R.string.account_deleted_body),
            style = MaterialTheme.typography.bodyMedium,
            color = Neutral700,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
        Box(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .height(56.dp)
                .clip(CircleShape)
                .background(Green900)
                .clickable(role = Role.Button, onClick = onDone),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(R.string.account_deleted_done), style = MaterialTheme.typography.titleMedium, color = Neutral50)
        }
    }
}
