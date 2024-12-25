import com.google.gson.JsonParser
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import io.ejekta.percale.deserialize
import io.ejekta.percale.serialize
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtUtils
import net.minecraft.nbt.TagParser
import strikt.api.expectThat
import strikt.assertions.isEqualTo

data class TestValidation<U : Any>(val obj: U, val serial: KSerializer<U>, val str: String) {
    fun <T> decode(ops: DynamicOps<T>, serialMod: SerializersModule = EmptySerializersModule()) {
        val result = ops.deserialize(parsed(ops), serial, serialMod) // unsafe cast, but we only have JsonOps to test on so works for now
        expectThat(result).isEqualTo(obj)
    }

    fun <T> parsed(ops: DynamicOps<T>): T {
        return when(ops) {
            JsonOps.INSTANCE -> JsonParser.parseString(str) as T
            NbtOps.INSTANCE -> TagParser.parseTag("{a:$str}").get("a") as T
            else -> throw Exception("Not valid!")
        }
    }

    fun <T> encode(ops: DynamicOps<T>, serialMod: SerializersModule = EmptySerializersModule()) {
        val stringEquivalent = parsed(ops)
        val result = ops.serialize(obj, serial, serialMod)
        expectThat(result).isEqualTo(stringEquivalent)
    }
}
