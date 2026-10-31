package pe.edu.upc.viora.features.assistant.presentation.state

import pe.edu.upc.viora.features.assistant.domain.entity.AssistantAnswer
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantQuestion
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantScreen
import pe.edu.upc.viora.features.assistant.domain.entity.QuestionIcon
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/** The two tabs of the sheet (Figma P115 · P118). */
enum class AssistantTab { ASK, RECENT }

/**
 * The assistant sheet of one screen and plot (Figma section 23). [catalog] are the questions of the
 * screen; [page] is what the "Preguntar" tab shows; [recent] is the "Recientes" tab, null until the
 * phone has read it.
 */
data class AssistantSheetUiState(
    val screen: AssistantScreen,
    val plotName: String,
    val tab: AssistantTab = AssistantTab.ASK,
    val offline: Boolean = false,
    val catalog: Catalog = Catalog.Loading,
    val page: AskPage = AskPage.Opening,
    val recent: List<RecentAnswer>? = null,
)

/** The questions of the screen. */
sealed interface Catalog {
    data object Loading : Catalog

    data class Ready(val questions: List<AssistantQuestion>) : Catalog

    data class Failed(val reason: AskFailure) : Catalog
}

/**
 * The question the producer tapped, as the chip of the answer shows it. [label] is null only for an
 * old answer of a question the screen no longer offers; the chip then says "Tu pregunta".
 */
data class AskedQuestion(val id: String, val label: String?, val icon: QuestionIcon)

/** What the "Preguntar" tab shows. */
sealed interface AskPage {

    /** Looking for the answer the screen reopens on (P114v); nothing is shown yet. */
    data object Opening : AskPage

    /** The list of questions (P115, P121, P126 offline). */
    data object Questions : AskPage

    /** Viora is reading the data of the plot (P116). */
    data class Thinking(val question: AskedQuestion) : AskPage

    /**
     * An answer (P117, P122), or the same dimmed with "Volver a preguntar" when the data changed (P119).
     * [refreshing] while it is asked again with the new data.
     */
    data class Answered(val question: AskedQuestion, val answer: AssistantAnswer, val refreshing: Boolean = false) : AskPage

    /** The question could not be answered (P127, and the 429/409/403 variants). */
    data class Failed(val question: AskedQuestion, val reason: AskFailure) : AskPage
}

/** Why a question was not answered; each one has its own text and buttons. */
sealed interface AskFailure {
    /** The server or the model failed: "Reintentar" with the same request. */
    data object Generic : AskFailure

    /** No signal: the recent answers can still be read. */
    data object Offline : AskFailure

    /** Too many new answers in a short time; the wait is rounded up to whole minutes. */
    data class RateLimited(val retryAfterMinutes: Int?) : AskFailure

    /** The question has no data yet (409): the list is read again. */
    data object Unavailable : AskFailure

    /** The plan expired (403). */
    data object NoPlan : AskFailure
}

/** An answer of the "Recientes" tab with the name of its plot, null when the plot is not on the phone. */
data class RecentAnswer(val answer: AssistantAnswer, val plotName: String?)

/** A day group of the "Recientes" tab (HOY · AYER · HACE N DÍAS). */
data class RecentDay(val daysAgo: Int, val answers: List<RecentAnswer>)

/** How long ago an answer was written, for the overline of the answer (P117 "HACE 2 MIN"). */
sealed interface AnswerAge {
    data object JustNow : AnswerAge
    data class Minutes(val minutes: Int) : AnswerAge
    data class Hours(val hours: Int) : AnswerAge

    /** Older than today: the overline names the day ("RESPUESTA DEL 23 DE OCT", P119). */
    data class OnDay(val date: LocalDate) : AnswerAge
}

object AssistantPresenter {

    /** The recent answers grouped by the day they were written, newest day first. */
    fun groupByDay(recent: List<RecentAnswer>, now: Instant, zone: ZoneId): List<RecentDay> {
        val today = now.atZone(zone).toLocalDate()
        return recent
            .groupBy { ChronoUnit.DAYS.between(it.answer.createdAt.atZone(zone).toLocalDate(), today).toInt().coerceAtLeast(0) }
            .toSortedMap()
            .map { (days, answers) -> RecentDay(days, answers.sortedByDescending { it.answer.createdAt }) }
    }

    fun ageOf(createdAt: Instant, now: Instant, zone: ZoneId): AnswerAge {
        val day = createdAt.atZone(zone).toLocalDate()
        if (day != now.atZone(zone).toLocalDate()) return AnswerAge.OnDay(day)
        val elapsed = Duration.between(createdAt, now).coerceAtLeast(Duration.ZERO)
        return when {
            elapsed < Duration.ofMinutes(1) -> AnswerAge.JustNow
            elapsed < Duration.ofHours(1) -> AnswerAge.Minutes(elapsed.toMinutes().toInt())
            else -> AnswerAge.Hours(elapsed.toHours().toInt())
        }
    }

    /** The chip of an answer: the question as it was read, or the label the screen offers for it now. */
    fun askedQuestionOf(answer: AssistantAnswer, catalog: Catalog): AskedQuestion {
        val offered = (catalog as? Catalog.Ready)?.questions?.firstOrNull { it.id == answer.questionId }
        return AskedQuestion(answer.questionId, answer.question ?: offered?.label, offered?.icon ?: QuestionIcon.OTHER)
    }
}
