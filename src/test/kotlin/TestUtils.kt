import com.google.gson.JsonParser
import com.mojang.serialization.DynamicOps
import kotlinx.serialization.KSerializer
import strikt.api.expectThat
import strikt.assertions.isEqualTo

data class TestValidation<U : Any>(val obj: U, val serial: KSerializer<U>, val str: String) {
    fun <T> decode(ops: DynamicOps<T>) {
        val result = ops.deserialize(JsonParser.parseString(str) as T, serial) // unsafe cast, but we only have JsonOps to test on so works for now
        expectThat(result).isEqualTo(obj)
    }

    fun <T> encode(ops: DynamicOps<T>) {
        val result = ops.serialize(obj, serial)
        expectThat(result.toString()).isEqualTo(str)
    }
}
