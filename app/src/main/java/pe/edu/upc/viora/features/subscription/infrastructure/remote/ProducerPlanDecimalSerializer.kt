package pe.edu.upc.viora.features.subscription.infrastructure.remote

import java.math.BigDecimal
import kotlinx.serialization.KSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonUnquotedLiteral
import kotlinx.serialization.json.jsonPrimitive

/** Reads the original JSON decimal lexeme without an intermediate binary floating-point number. */
object ProducerPlanDecimalSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ProducerPlanDecimal", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): BigDecimal = try {
        val json = decoder as? JsonDecoder ?: throw SerializationException("Expected JSON catalog decimal")
        json.decodeJsonElement().jsonPrimitive.content.toBigDecimal()
    } catch (exception: IllegalArgumentException) {
        throw SerializationException("Invalid catalog decimal", exception)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: BigDecimal) {
        val json = encoder as? JsonEncoder ?: throw SerializationException("Expected JSON catalog encoder")
        json.encodeJsonElement(JsonUnquotedLiteral(value.toPlainString()))
    }
}
