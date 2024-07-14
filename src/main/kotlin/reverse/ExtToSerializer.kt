package reverse

import com.google.gson.JsonElement as GsonElement
import com.google.gson.JsonParser as GsonParser
import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.contextual


// This is gross but ok
class GsonElementSerializer(val json: Json) : KSerializer<GsonElement> {
    val jsonSer = JsonElement.serializer()
    override val descriptor: SerialDescriptor = jsonSer.descriptor
    override fun serialize(encoder: Encoder, value: GsonElement) {
        encoder.encodeSerializableValue(jsonSer, json.decodeFromString(jsonSer, value.toString()))
    }

    override fun deserialize(decoder: Decoder): GsonElement {
        val result = decoder.decodeSerializableValue(jsonSer)
        return GsonParser.parseString(result.toString())
    }
}

fun <A> Codec<A>.toKotlinJsonSerializer(json: Json = Json.Default): KSerializer<A> {
    return toGenericSer(JsonOps.INSTANCE, GsonElementSerializer(json))
}

fun <A> Codec<A>.toWrappedJsonSerializer(ops: DynamicOps<GsonElement>, json: Json = Json.Default): KSerializer<A> {
    return toGenericSer(ops, GsonElementSerializer(json))
}

fun <A, U> Codec<A>.toGenericSer(ops: DynamicOps<U>, opsSerializer: KSerializer<U>): KSerializer<A> {
    return object : KSerializer<A> {
        override val descriptor: SerialDescriptor
            get() = opsSerializer.descriptor // I suppose this works

        override fun serialize(encoder: Encoder, value: A) {
            val result = this@toGenericSer.encodeStart(ops, value).result().get()
            encoder.encodeSerializableValue(opsSerializer, result)
        }

        override fun deserialize(decoder: Decoder): A {
            val result = decoder.decodeSerializableValue(opsSerializer)
            return this@toGenericSer.parse(ops, result).result().get()
        }
    }
}

inline fun <reified A : Any> SerializersModuleBuilder.codec(codec: Codec<A>, json: Json = Json.Default) {
    contextual(codec.toKotlinJsonSerializer(json))
}
