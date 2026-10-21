package pe.edu.upc.viora.features.iam.presentation.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upc.viora.R
import pe.edu.upc.viora.core.designsystem.component.VioraVoice
import pe.edu.upc.viora.core.designsystem.theme.Neutral200
import pe.edu.upc.viora.features.iam.presentation.state.OnboardingStep
import pe.edu.upc.viora.features.iam.presentation.state.OnboardingUiState
import pe.edu.upc.viora.features.iam.presentation.viewmodel.OnboardingViewModel

/**
 * T02b-T02f (Figma "02 · Preguntas del onboarding"): one question per screen with the "01 / 06"
 * counter, then the summary. [onCreateAccount] runs on "Crear mi cuenta" and [onExit] on back from
 * the first question.
 */
@Composable
fun OnboardingScreen(
    onCreateAccount: () -> Unit,
    onExit: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val onBack = { if (!viewModel.back()) onExit() }
    BackHandler(onBack = onBack)

    when (state.step) {
        OnboardingStep.NAME -> NameStep(state, viewModel, onBack)
        OnboardingStep.PHONE -> PhoneStep(state, viewModel, onBack)
        OnboardingStep.COOPERATIVE_CODE -> CooperativeCodeStep(state, viewModel, onBack)
        OnboardingStep.HECTARES -> HectaresStep(state, viewModel, onBack)
        OnboardingStep.ALERTS -> AlertsStep(viewModel, onBack)
        OnboardingStep.LOCATION -> LocationStep(viewModel, onBack)
        OnboardingStep.SUMMARY -> SummaryStep(state, viewModel, onBack, onCreateAccount)
    }
}

/** The frame of a question: counter, serif title and Viora's line, then [content]. */
@Composable
private fun QuestionFrame(
    step: OnboardingStep,
    title: String,
    voice: String,
    onBack: () -> Unit,
    footer: @Composable () -> Unit,
    contentGap: Dp = 44.dp,
    content: @Composable () -> Unit,
) {
    IamFrame(
        onBack = onBack,
        stepLabel = step.number?.let { stringResource(R.string.onboarding_step_counter, it, STEP_COUNT) },
        contentSpacing = 0.dp,
        footerSpacing = 6.dp,
        footer = { footer() },
    ) {
        IamTitle(title)
        Spacer(Modifier.height(16.dp))
        VioraVoice(voice)
        Spacer(Modifier.height(contentGap))
        content()
    }
}

// ---- T02b · Name ----------------------------------------------------------------------------

@Composable
private fun NameStep(state: OnboardingUiState, vm: OnboardingViewModel, onBack: () -> Unit) {
    QuestionFrame(
        step = state.step,
        title = stringResource(R.string.onboarding_name_title),
        voice = stringResource(R.string.onboarding_name_voice),
        onBack = onBack,
        footer = {
            PillButton(text = stringResource(R.string.iam_continue), onClick = vm::continueFromName, enabled = state.nameValid)
        },
    ) {
        UnderlineField(
            label = stringResource(R.string.onboarding_name_label),
            value = state.fullName,
            onValueChange = vm::onName,
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done,
            onImeAction = vm::continueFromName,
        )
    }
}

// ---- T02b2 · Mobile -------------------------------------------------------------------------

@Composable
private fun PhoneStep(state: OnboardingUiState, vm: OnboardingViewModel, onBack: () -> Unit) {
    QuestionFrame(
        step = state.step,
        title = stringResource(R.string.onboarding_phone_title),
        voice = stringResource(R.string.onboarding_phone_voice),
        onBack = onBack,
        footer = {
            PillButton(text = stringResource(R.string.iam_continue), onClick = vm::continueFromPhone, enabled = state.phoneValid)
        },
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            CountryField(selected = state.country, onSelect = vm::onCountry, modifier = Modifier.width(90.dp))
            UnderlineField(
                label = stringResource(R.string.onboarding_phone_label),
                value = state.phone,
                onValueChange = vm::onPhone,
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done,
                onImeAction = vm::continueFromPhone,
                onFocusLost = vm::onPhoneLeft,
                visualTransformation = GroupsOfThree,
                error = if (state.showPhoneError) stringResource(state.country.invalidHintRes) else null,
                supporting = stringResource(R.string.onboarding_phone_hint),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** "987654321" shows as "987 654 321", like the Figma field and the summary. */
private object GroupsOfThree : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val grouped = text.text.chunked(3).joinToString(" ")
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = offset + (offset - 1).coerceAtLeast(0) / 3
            override fun transformedToOriginal(offset: Int): Int = (offset - offset / 4).coerceIn(0, text.length)
        }
        return TransformedText(AnnotatedString(grouped), mapping)
    }
}

// ---- T02c · Cooperative code ----------------------------------------------------------------

@Composable
private fun CooperativeCodeStep(state: OnboardingUiState, vm: OnboardingViewModel, onBack: () -> Unit) {
    QuestionFrame(
        step = state.step,
        title = stringResource(R.string.onboarding_code_title),
        voice = stringResource(R.string.onboarding_code_voice),
        onBack = onBack,
        footer = {
            PillButton(
                text = stringResource(R.string.onboarding_code_save),
                onClick = vm::saveCooperativeCode,
                enabled = state.cooperativeCodeValid,
            )
            OutlinedPillButton(text = stringResource(R.string.onboarding_code_none), onClick = vm::skipCooperativeCode)
        },
    ) {
        UnderlineField(
            label = stringResource(R.string.onboarding_code_label),
            value = state.cooperativeCode,
            onValueChange = vm::onCooperativeCode,
            capitalization = KeyboardCapitalization.Characters,
            imeAction = ImeAction.Done,
            onImeAction = vm::saveCooperativeCode,
            supporting = stringResource(R.string.onboarding_code_hint),
        )
    }
}

// ---- T02d · Hectares ------------------------------------------------------------------------

@Composable
private fun HectaresStep(state: OnboardingUiState, vm: OnboardingViewModel, onBack: () -> Unit) {
    QuestionFrame(
        step = state.step,
        title = stringResource(R.string.onboarding_hectares_title),
        voice = stringResource(R.string.onboarding_hectares_voice),
        onBack = onBack,
        footer = {
            PillButton(text = stringResource(R.string.iam_continue), onClick = { vm.continueFromHectares(unknown = false) })
            TextPillButton(text = stringResource(R.string.onboarding_hectares_unknown), onClick = { vm.continueFromHectares(unknown = true) })
        },
    ) {
        Column {
            HectareStepper(hectares = state.hectares, onChange = vm::onHectares)
            Spacer(Modifier.height(12.dp))
            HectareRuler(hectares = state.hectares, onChange = vm::onHectares)
            Spacer(Modifier.height(32.dp))
            PlanEstimate(state)
        }
    }
}

// ---- T02e · Alerts --------------------------------------------------------------------------

@Composable
private fun AlertsStep(vm: OnboardingViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    // Android 13+ asks with the system dialog; "No permitir" continues all the same (Figma legend).
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        vm.onAlertsAnswered(granted)
    }
    QuestionFrame(
        step = OnboardingStep.ALERTS,
        title = stringResource(R.string.onboarding_alerts_title),
        voice = stringResource(R.string.onboarding_alerts_voice),
        onBack = onBack,
        footer = {
            PillButton(
                text = stringResource(R.string.onboarding_alerts_enable),
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        vm.onAlertsAnswered(NotificationManagerCompat.from(context).areNotificationsEnabled())
                    }
                },
            )
            TextPillButton(text = stringResource(R.string.onboarding_not_now), onClick = { vm.onAlertsAnswered(false) })
        },
    ) {
        Column {
            Spacer(Modifier.height(14.dp))
            AlertPreview()
            Spacer(Modifier.height(30.dp))
            DashList(
                listOf(
                    stringResource(R.string.onboarding_alerts_item_heat),
                    stringResource(R.string.onboarding_alerts_item_chill),
                    stringResource(R.string.onboarding_alerts_item_thinning),
                ),
            )
        }
    }
}

// ---- T02e2 · Location -----------------------------------------------------------------------

@Composable
private fun LocationStep(vm: OnboardingViewModel, onBack: () -> Unit) {
    // "Mientras la app está en uso" or "Solo esta vez" both count; denied, the lot is drawn by hand.
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        vm.onLocationAnswered(result.values.any { it })
    }
    QuestionFrame(
        step = OnboardingStep.LOCATION,
        title = stringResource(R.string.onboarding_location_title),
        voice = stringResource(R.string.onboarding_location_voice),
        onBack = onBack,
        footer = {
            PillButton(
                text = stringResource(R.string.onboarding_location_allow),
                onClick = {
                    launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                },
            )
            TextPillButton(text = stringResource(R.string.onboarding_not_now), onClick = { vm.onLocationAnswered(false) })
        },
    ) {
        Column {
            GpsTraceIllustration(Modifier)
            Spacer(Modifier.height(24.dp))
            DashList(
                listOf(
                    stringResource(R.string.onboarding_location_item_corners),
                    stringResource(R.string.onboarding_location_item_area),
                    stringResource(R.string.onboarding_location_item_climate),
                ),
            )
        }
    }
}

// ---- T02f · Summary -------------------------------------------------------------------------

@Composable
private fun SummaryStep(state: OnboardingUiState, vm: OnboardingViewModel, onBack: () -> Unit, onCreateAccount: () -> Unit) {
    val access = when {
        state.savedCooperativeCode != null -> stringResource(R.string.onboarding_summary_access_code, state.savedCooperativeCode)
        state.hectaresUnknown -> stringResource(R.string.onboarding_summary_access_plan)
        else -> stringResource(R.string.onboarding_summary_access_plan_ha, state.hectares)
    }
    QuestionFrame(
        step = OnboardingStep.SUMMARY,
        title = stringResource(R.string.onboarding_summary_title, state.fullName.trim().substringBefore(' ')),
        voice = stringResource(R.string.onboarding_summary_voice),
        onBack = onBack,
        contentGap = 24.dp,
        footer = {
            PillButton(text = stringResource(R.string.onboarding_summary_create), onClick = onCreateAccount)
            Spacer(Modifier.height(6.dp))
            LegalNotice()
        },
    ) {
        Column {
            SummaryDivider()
            SummaryRow(stringResource(R.string.onboarding_summary_name), state.fullName.trim(), { vm.edit(OnboardingStep.NAME) })
            SummaryRow(stringResource(R.string.onboarding_summary_phone), state.phoneInternational, { vm.edit(OnboardingStep.PHONE) })
            SummaryRow(stringResource(R.string.onboarding_summary_access), access, { vm.edit(OnboardingStep.COOPERATIVE_CODE) })
            SummaryRow(
                stringResource(R.string.onboarding_summary_alerts),
                stringResource(if (state.alertsEnabled) R.string.onboarding_summary_alerts_on else R.string.onboarding_summary_alerts_off),
                { vm.edit(OnboardingStep.ALERTS) },
            )
            SummaryRow(
                stringResource(R.string.onboarding_summary_location),
                stringResource(if (state.locationGranted) R.string.onboarding_summary_location_on else R.string.onboarding_summary_location_off),
                { vm.edit(OnboardingStep.LOCATION) },
            )
        }
    }
}

@Composable
private fun SummaryDivider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(Neutral200))
}

private const val STEP_COUNT = 6
