package json

import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.mojang.serialization.JsonOps
import io.ejekta.percale.deserialize
import io.ejekta.percale.reverse.GsonElementSerializer
import io.ejekta.percale.serialize
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ReverseJsonSerializerTest {

    private val ops = JsonOps.INSTANCE
    private val serializer = GsonElementSerializer

    @Test
    fun testDecodeJsonNull() {
        expectThat(ops.deserialize(JsonNull.INSTANCE, serializer)).isEqualTo(JsonNull.INSTANCE)
    }

    @Test
    fun testEncodeJsonNull() {
        expectThat(ops.serialize(JsonNull.INSTANCE, serializer)).isEqualTo(JsonNull.INSTANCE)
    }
}
