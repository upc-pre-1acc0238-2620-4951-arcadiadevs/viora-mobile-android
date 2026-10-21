package pe.edu.upc.viora.features.iam.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upc.viora.features.iam.domain.valueobject.OnboardingAnswers
import pe.edu.upc.viora.features.iam.infrastructure.local.OnboardingStore
import pe.edu.upc.viora.features.iam.presentation.state.IamCountry
import pe.edu.upc.viora.features.iam.presentation.state.OnboardingStep
import pe.edu.upc.viora.features.iam.presentation.state.OnboardingUiState

/**
 * The onboarding questions T02b-T02f. Order: name, mobile, cooperative code, hectares (skipped when
 * a code was saved), alerts, location, summary. Each answer is saved on the phone as soon as the
 * producer moves on, and an earlier run is picked up where its answers left off.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(private val store: OnboardingStore) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val answers = store.answers()
            _uiState.update {
                // The producer may already be typing when the saved answers arrive: keep their edits.
                if (it.fullName.isNotEmpty() || it.step != OnboardingStep.NAME) return@update it
                it.copy(
                    fullName = answers.fullName,
                    country = IamCountry.fromIso(answers.countryIso),
                    phone = answers.nationalPhone,
                    cooperativeCode = answers.cooperativeCode.orEmpty(),
                    savedCooperativeCode = answers.cooperativeCode,
                    hectares = answers.hectares ?: OnboardingUiState.DEFAULT_HECTARES,
                    // No hectares, no code and the alerts already answered: "Aún no lo sé" was chosen.
                    hectaresUnknown = answers.hectares == null && answers.cooperativeCode == null &&
                        answers.alertsEnabled != null,
                    alertsEnabled = answers.alertsEnabled == true,
                    locationGranted = answers.locationGranted == true,
                )
            }
        }
    }

    // ---- Field edits -----------------------------------------------------------------------

    fun onName(value: String) = _uiState.update { it.copy(fullName = value) }

    fun onCountry(value: IamCountry) =
        _uiState.update { it.copy(country = value, phone = it.phone.take(value.nationalDigits)) }

    fun onPhone(value: String) = _uiState.update {
        it.copy(phone = value.filter(Char::isDigit).take(it.country.nationalDigits))
    }

    fun onPhoneLeft() = _uiState.update { it.copy(phoneTouched = it.phone.isNotEmpty()) }

    fun onCooperativeCode(value: String) = _uiState.update {
        val code = value.uppercase().filter { c -> c.isLetterOrDigit() || c == '-' }
        it.copy(cooperativeCode = code.take(OnboardingUiState.MAX_CODE_LENGTH))
    }

    fun onHectares(value: Int) = _uiState.update {
        it.copy(hectares = value.coerceIn(OnboardingUiState.MIN_HECTARES, OnboardingUiState.MAX_HECTARES))
    }

    // ---- Moving forward --------------------------------------------------------------------

    fun continueFromName() {
        if (_uiState.value.nameValid) advance(OnboardingStep.PHONE)
    }

    fun continueFromPhone() {
        val s = _uiState.value
        if (!s.phoneValid) {
            _uiState.update { it.copy(phoneTouched = true) }
            return
        }
        advance(OnboardingStep.COOPERATIVE_CODE)
    }

    /** "Guardar código": a cooperative covers the plan, so the hectares question is skipped. */
    fun saveCooperativeCode() {
        val s = _uiState.value
        if (!s.cooperativeCodeValid) return
        _uiState.update { it.copy(savedCooperativeCode = it.cooperativeCode.trim()) }
        advance(OnboardingStep.ALERTS)
    }

    /** "No tengo código": the Plan Productor estimate comes next, also while editing. */
    fun skipCooperativeCode() {
        _uiState.update { it.copy(savedCooperativeCode = null, cooperativeCode = "") }
        goTo(OnboardingStep.HECTARES, editing = _uiState.value.editing)
        persist()
    }

    fun continueFromHectares(unknown: Boolean) {
        _uiState.update { it.copy(hectaresUnknown = unknown) }
        advance(OnboardingStep.ALERTS)
    }

    /** After the system dialog of "Activar alertas", or "Ahora no" (false). */
    fun onAlertsAnswered(enabled: Boolean) {
        _uiState.update { it.copy(alertsEnabled = enabled) }
        advance(OnboardingStep.LOCATION)
    }

    /** After the system dialog of "Permitir ubicación", or "Ahora no" (false). */
    fun onLocationAnswered(granted: Boolean) {
        _uiState.update { it.copy(locationGranted = granted) }
        advance(OnboardingStep.SUMMARY)
    }

    /** "Editar" on a summary row. */
    fun edit(step: OnboardingStep) = goTo(step, editing = true)

    /** Back arrow and system back. False on the first question, so the screen can be left. */
    fun back(): Boolean {
        val s = _uiState.value
        if (s.editing) {
            goTo(OnboardingStep.SUMMARY, editing = false)
            return true
        }
        val previous = when (s.step) {
            OnboardingStep.NAME -> return false
            OnboardingStep.PHONE -> OnboardingStep.NAME
            OnboardingStep.COOPERATIVE_CODE -> OnboardingStep.PHONE
            OnboardingStep.HECTARES -> OnboardingStep.COOPERATIVE_CODE
            OnboardingStep.ALERTS ->
                if (s.savedCooperativeCode != null) OnboardingStep.COOPERATIVE_CODE else OnboardingStep.HECTARES
            OnboardingStep.LOCATION -> OnboardingStep.ALERTS
            OnboardingStep.SUMMARY -> OnboardingStep.LOCATION
        }
        goTo(previous, editing = false)
        return true
    }

    // ---- Internals -------------------------------------------------------------------------

    /** Goes to [next], or back to the summary when the question was opened from "Editar". */
    private fun advance(next: OnboardingStep) {
        val editing = _uiState.value.editing
        goTo(if (editing) OnboardingStep.SUMMARY else next, editing = false)
        persist()
    }

    private fun goTo(step: OnboardingStep, editing: Boolean) =
        _uiState.update { it.copy(step = step, editing = editing) }

    private fun persist() {
        val s = _uiState.value
        val answers = OnboardingAnswers(
            fullName = s.fullName.trim(),
            countryIso = s.country.iso,
            nationalPhone = s.phone,
            cooperativeCode = s.savedCooperativeCode,
            hectares = s.hectares.takeUnless { s.hectaresUnknown || s.savedCooperativeCode != null },
            alertsEnabled = s.alertsEnabled.takeIf { s.step.ordinal > OnboardingStep.ALERTS.ordinal },
            locationGranted = s.locationGranted.takeIf { s.step == OnboardingStep.SUMMARY },
        )
        viewModelScope.launch { store.save(answers) }
    }
}
