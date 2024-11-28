import com.google.gson.JsonParser
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import io.ejekta.percale.deserialize
import io.ejekta.percale.serialize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import strikt.api.expectThat
import strikt.assertions.isEqualTo

data class TestValidation<U : Any>(val obj: U, val serial: KSerializer<U>, val str: String) {
    fun <T> decode(ops: DynamicOps<T>, serialMod: SerializersModule = EmptySerializersModule()) {
        val result = ops.deserialize(JsonParser.parseString(str) as T, serial, serialMod) // unsafe cast, but we only have JsonOps to test on so works for now
        expectThat(result).isEqualTo(obj)
    }



    fun <T> encode(ops: DynamicOps<T>, serialMod: SerializersModule = EmptySerializersModule()) {
        val stringEquivalent = JsonParser.parseString(str) as T
        val result = ops.serialize(obj, serial, serialMod)
        expectThat(result).isEqualTo(stringEquivalent)
    }
}
