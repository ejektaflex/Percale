package json

import TestData
import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.mojang.serialization.JsonOps
import io.ejekta.percale.deserialize
import io.ejekta.percale.serialize
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class NullableRoundTripTest {

    private val ops = JsonOps.INSTANCE

    @Test
    fun testEncodeNullableObjectStructure() {
        val value = TestData.NullableContainer(
            primary = TestData.NullablePerson(name = null, age = 36),
            aliases = listOf("Jim", null, "J"),
            scores = mapOf("main" to 9, "bonus" to null)
        )

        val expected = JsonObject().apply {
            add("primary", JsonObject().apply {
                add("name", JsonNull.INSTANCE)
                add("age", JsonPrimitive(36))
            })
            add("aliases", JsonArray().apply {
                add("Jim")
                add(JsonNull.INSTANCE)
                add("J")
            })
            add("scores", JsonObject().apply {
                add("main", JsonPrimitive(9))
                add("bonus", JsonNull.INSTANCE)
            })
        }

        expectThat(ops.serialize(value, TestData.NullableContainer.serializer())).isEqualTo(expected)
    }

    @Test
    fun testEncodeNullableListEntries() {
        val value = listOf("a", null, "b")
        val serializer = ListSerializer(String.serializer().nullable)
        val expected = JsonArray().apply {
            add("a")
            add(JsonNull.INSTANCE)
            add("b")
        }

        expectThat(ops.serialize(value, serializer)).isEqualTo(expected)
    }

    @Test
    fun testDecodeNullableMapValues() {
        val input = JsonObject().apply {
            add("a", JsonPrimitive(1))
            add("b", JsonNull.INSTANCE)
        }
        val serializer = MapSerializer(String.serializer(), Int.serializer().nullable)

        expectThat(ops.deserialize(input, serializer)).isEqualTo(mapOf("a" to 1, "b" to null))
    }
}
