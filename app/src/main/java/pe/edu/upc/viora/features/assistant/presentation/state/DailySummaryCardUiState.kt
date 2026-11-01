package pe.edu.upc.viora.features.assistant.presentation.state

import pe.edu.upc.viora.features.assistant.domain.entity.DailySummary

/** The Viora card of Inicio (Figma P110, P111). */
sealed interface DailySummaryCardUiState {

    /** Reading the summary of the day; the card already shows its title and the ask button. */
    data object Loading : DailySummaryCardUiState

    /** The summary of the day, collapsed in one line (P110) or open with its voice and three cards (P111). */
    data class Ready(val summary: DailySummary) : DailySummaryCardUiState

    /** There is no summary to show now (no signal and none kept today, or the server failed): only the ask button. */
    data object WithoutSummary : DailySummaryCardUiState

    /** Without an active plan or without active plots the assistant is not offered: no card. */
    data object Hidden : DailySummaryCardUiState
}
