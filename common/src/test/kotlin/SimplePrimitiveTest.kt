import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.mojang.serialization.JsonOps
import io.ejekta.percale.deserialize
import io.ejekta.percale.serialize
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

class SimplePrimitiveTest {

    val ops = JsonOps.INSTANCE

    // Array consisting of primitives
    @Test fun testEncodePrimitives() {
        expectThat(ops.serialize(1)).isEqualTo(JsonPrimitive(1))
        expectThat(ops.serialize(1L)).isEqualTo(JsonPrimitive(1L))
        expectThat(ops.serialize(1.1f)).isEqualTo(JsonPrimitive(1.1f))
        expectThat(ops.serialize(2.2)).isEqualTo(JsonPrimitive(2.2))
        expectThat(ops.serialize(true)).isEqualTo(JsonPrimitive(true))
        expectThat(ops.serialize("str")).isEqualTo(JsonPrimitive("str"))
    }

    @Test fun testDecodePrimitives() {
        expectThat(ops.deserialize<JsonElement, Int>(JsonPrimitive(1))).isEqualTo(1)
        expectThat(ops.deserialize<JsonElement, Long>(JsonPrimitive(1L))).isEqualTo(1L)
        expectThat(ops.deserialize<JsonElement, Float>(JsonPrimitive(1.1f))).isEqualTo(1.1f)
        expectThat(ops.deserialize<JsonElement, Double>(JsonPrimitive(2.2))).isEqualTo(2.2)
        expectThat(ops.deserialize<JsonElement, Boolean>(JsonPrimitive(true))).isTrue()
        expectThat(ops.deserialize<JsonElement, String>(JsonPrimitive("str"))).isEqualTo("str")
    }

    @Test fun testEncodeEnum() {
        expectThat(ops.serialize(TestData.DogBreed.POMERANIAN)).isEqualTo(JsonPrimitive("POMERANIAN"))
    }

    @Test fun testDecodeEnum() {
        expectThat(ops.deserialize<JsonElement, TestData.DogBreed>(JsonPrimitive("POMERANIAN"))).isEqualTo(TestData.DogBreed.POMERANIAN)
    }

}
