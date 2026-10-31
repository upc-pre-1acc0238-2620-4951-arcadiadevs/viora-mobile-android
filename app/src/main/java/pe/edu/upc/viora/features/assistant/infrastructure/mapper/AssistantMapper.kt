package pe.edu.upc.viora.features.assistant.infrastructure.mapper

import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import pe.edu.upc.viora.core.domain.enumOrDefault
import pe.edu.upc.viora.features.assistant.domain.entity.AnswerAction
import pe.edu.upc.viora.features.assistant.domain.entity.AnswerFact
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantAnswer
import pe.edu.upc.viora.features.assistant.domain.entity.AssistantQuestion
import pe.edu.upc.viora.features.assistant.domain.entity.FactTone
import pe.edu.upc.viora.features.assistant.domain.entity.Freshness
import pe.edu.upc.viora.features.assistant.domain.entity.QuestionIcon
import pe.edu.upc.viora.features.assistant.infrastructure.local.AssistantAnswerEntity
import pe.edu.upc.viora.features.assistant.infrastructure.remote.AssistantAnswerDto
import pe.edu.upc.viora.features.assistant.infrastructure.remote.QuestionDto
import java.time.Instant

/** The only buttons the app knows how to open; anything else the server sends is left out. */
private const val DEEPLINK = "DEEPLINK"

/** Stored shapes of the JSON columns, independent of the wire DTOs. */
@Serializable
private data class StoredFact(val value: String, val label: String, val pill: String, val tone: String)

@Serializable
private data class StoredAction(val label: String, val target: String)

fun QuestionDto.toDomain(): AssistantQuestion = AssistantQuestion(
    id = questionId,
    label = label,
    icon = QuestionIcon.fromKey(icon),
    enabled = enabled,
    disabledReason = disabledReason?.takeIf { it.isNotBlank() },
)

/** Null when the answer cannot be shown as designed: not exactly two cards, or a malformed date. */
fun AssistantAnswerDto.toDomainOrNull(): AssistantAnswer? {
    val created = runCatching { Instant.parse(createdAt) }.getOrNull() ?: return null
    val cards = facts.map { AnswerFact(it.value, it.label, it.pill, enumOrDefault(it.tone, FactTone.HIGHLIGHT)) }
    if (cards.size != 2) return null
    return AssistantAnswer(
        id = answerId,
        questionId = questionId,
        question = question?.takeIf { it.isNotBlank() },
        plotId = plotId,
        createdAt = created,
        freshness = enumOrDefault(freshness, Freshness.UP_TO_DATE),
        text = text,
        facts = cards,
        sources = sources.map { it.label }.filter { it.isNotBlank() },
        actions = actions.filter { it.type == DEEPLINK }.map { AnswerAction(it.label, it.target) },
    )
}

fun AssistantAnswer.toEntity(json: Json, movedOn: Boolean): AssistantAnswerEntity = AssistantAnswerEntity(
    id = id,
    questionId = questionId,
    question = question,
    plotId = plotId,
    createdAtEpochMs = createdAt.toEpochMilli(),
    freshness = freshness.name,
    text = text,
    factsJson = json.encodeToString(ListSerializer(StoredFact.serializer()), facts.map { StoredFact(it.value, it.label, it.pill, it.tone.name) }),
    sourcesJson = json.encodeToString(ListSerializer(String.serializer()), sources),
    actionsJson = json.encodeToString(ListSerializer(StoredAction.serializer()), actions.map { StoredAction(it.label, it.target) }),
    movedOn = movedOn,
)

/** Null for a row that no longer reads as an answer (it is then simply not shown). */
fun AssistantAnswerEntity.toDomainOrNull(json: Json): AssistantAnswer? = runCatching {
    AssistantAnswer(
        id = id,
        questionId = questionId,
        question = question,
        plotId = plotId,
        createdAt = Instant.ofEpochMilli(createdAtEpochMs),
        freshness = enumOrDefault(freshness, Freshness.UP_TO_DATE),
        text = text,
        facts = json.decodeFromString(ListSerializer(StoredFact.serializer()), factsJson)
            .map { AnswerFact(it.value, it.label, it.pill, enumOrDefault(it.tone, FactTone.HIGHLIGHT)) },
        sources = json.decodeFromString(ListSerializer(String.serializer()), sourcesJson),
        actions = json.decodeFromString(ListSerializer(StoredAction.serializer()), actionsJson).map { AnswerAction(it.label, it.target) },
    )
}.getOrNull()?.takeIf { it.facts.size == 2 }
