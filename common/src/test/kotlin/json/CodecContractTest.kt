package json

import TestData
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.ejekta.percale.toCodec
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse

class CodecContractTest {

    private val ops = JsonOps.INSTANCE
    private val personCodec = TestData.Person.serializer().toCodec()
    private data class WrappedPerson(val label: String, val person: TestData.Person)

    @Test
    fun testEncodeMergesIntoExistingMapPrefix() {
        val prefix = JsonObject().apply {
            add("existing", JsonPrimitive(true))
        }

        val encoded = personCodec.encode(TestData.Person("Jimothy", 36), ops, prefix).orThrow

        expectThat(encoded.asJsonObject.get("existing")).isEqualTo(JsonPrimitive(true))
        expectThat(encoded.asJsonObject.get("name")).isEqualTo(JsonPrimitive("Jimothy"))
        expectThat(encoded.asJsonObject.get("age")).isEqualTo(JsonPrimitive(36))
    }

    @Test
    fun testEncodeMergesIntoExistingListPrefix() {
        val prefix = JsonArray().apply {
            add(JsonPrimitive("seed"))
        }
        val listCodec = Codec.list(Codec.INT)

        val encoded = listCodec.encode(listOf(1, 2, 3), ops, prefix).orThrow

        expectThat(encoded).isEqualTo(JsonArray().apply {
            add("seed")
            add(1)
            add(2)
            add(3)
        })
    }

    @Test
    fun testDecodeReturnsOriginalInputAsRemainder() {
        val input = JsonObject().apply {
            add("name", JsonPrimitive("Jimothy"))
            add("age", JsonPrimitive(36))
        }

        val decoded = personCodec.decode(ops, input).orThrow

        expectThat(decoded.first).isEqualTo(TestData.Person("Jimothy", 36))
        expectThat(decoded.second).isEqualTo(input)
    }

    @Test
    fun testGeneratedCodecCanComposeInRecordBuilder() {
        val wrapperCodec: Codec<WrappedPerson> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.fieldOf("label").forGetter { it.label },
                personCodec.fieldOf("person").forGetter { it.person }
            ).apply(instance, ::WrappedPerson)
        }

        val input = JsonObject().apply {
            add("label", JsonPrimitive("anything"))
            add("person", JsonObject().apply {
                add("name", JsonPrimitive("Jimothy"))
                add("age", JsonPrimitive(36))
            })
        }

        val result = wrapperCodec.decode(ops, input)

        expectThat(result.isError).isFalse()
    }
}
