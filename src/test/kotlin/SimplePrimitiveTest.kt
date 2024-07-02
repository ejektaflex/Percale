import com.google.gson.JsonPrimitive
import com.mojang.serialization.JsonOps
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class SimplePrimitiveTest {

    val ops = JsonOps.INSTANCE

    // Array consisting of primitives
    @Test fun testEncodePrimitiveInt() {
        expectThat(ops.serialize(1)).isEqualTo(JsonPrimitive(1))
        expectThat(ops.serialize(1L)).isEqualTo(JsonPrimitive(1L))
        expectThat(ops.serialize(1f)).isEqualTo(JsonPrimitive(1f))
    }

    @Test fun testEncodeEnum() {
        expectThat(ops.serialize(TestData.DogBreed.POMERANIAN)).isEqualTo(JsonPrimitive("POMERANIAN"))
    }

}
