package pe.edu.upc.viora.features.assistant.presentation

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upc.viora.features.assistant.answer
import pe.edu.upc.viora.features.assistant.domain.entity.QuestionIcon
import pe.edu.upc.viora.features.assistant.presentation.state.AnswerAge
import pe.edu.upc.viora.features.assistant.presentation.state.AssistantPresenter
import pe.edu.upc.viora.features.assistant.presentation.state.Catalog
import pe.edu.upc.viora.features.assistant.presentation.state.RecentAnswer
import pe.edu.upc.viora.features.assistant.question
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class AssistantPresenterTest {

    private val lima = ZoneId.of("America/Lima")
    private val now = Instant.parse("2026-10-30T15:00:00Z")

    @Test
    fun theAgeOfAnAnswerIsToldInMinutesHoursOrItsDay() {
        assertEquals(AnswerAge.JustNow, AssistantPresenter.ageOf(now.minusSeconds(30), now, lima))
        assertEquals(AnswerAge.Minutes(2), AssistantPresenter.ageOf(now.minusSeconds(150), now, lima))
        assertEquals(AnswerAge.Hours(3), AssistantPresenter.ageOf(now.minusSeconds(3 * 3600 + 60), now, lima))
        // 04:00 UTC is still the day before in Lima
        assertEquals(AnswerAge.OnDay(LocalDate.of(2026, 10, 29)), AssistantPresenter.ageOf(Instant.parse("2026-10-30T04:00:00Z"), now, lima))
    }

    @Test
    fun recentAnswersAreGroupedByTheirDayNewestFirst() {
        val today = RecentAnswer(answer(id = "a", createdAt = now.minusSeconds(600)), "La Yarada 02")
        val earlierToday = RecentAnswer(answer(id = "b", createdAt = now.minusSeconds(3600)), "La Yarada 02")
        val fiveDaysAgo = RecentAnswer(answer(id = "c", createdAt = now.minusSeconds(5 * 86_400)), null)

        val days = AssistantPresenter.groupByDay(listOf(fiveDaysAgo, earlierToday, today), now, lima)

        assertEquals(listOf(0, 5), days.map { it.daysAgo })
        assertEquals(listOf("a", "b"), days[0].answers.map { it.answer.id })
    }

    @Test
    fun theChipUsesTheQuestionAsItWasReadAndTheIconOfTheScreen() {
        val catalog = Catalog.Ready(listOf(question(label = "¿Por qué mi índice es 0,48?")))

        val asked = AssistantPresenter.askedQuestionOf(answer(), catalog)
        assertEquals("¿Por qué mi índice es 0,51?", asked.label)
        assertEquals(QuestionIcon.CALENDAR, asked.icon)

        val old = AssistantPresenter.askedQuestionOf(answer(question = null), catalog)
        assertEquals("¿Por qué mi índice es 0,48?", old.label)
    }
}
