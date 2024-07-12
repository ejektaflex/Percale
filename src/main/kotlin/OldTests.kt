import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.Encoder
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.PrimitiveCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import kotlinx.serialization.*
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import java.util.stream.Stream
import kotlinx.serialization.encoding.Encoder as KEncoder
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual


/*

val encoder = PassEncoder.pickEncoder(serializer.descriptor, ops)
    encoder.encodeSerializableValue(serializer, obj)
    return encoder.getResult()

fun <U : Any> KSerializer<U>.toCodec(): Codec<U> {
    return object : Codec<U> {
        override fun <T : Any> encode(input: U, ops: DynamicOps<T>, prefix: T): DataResult<T> {
            val result = encodeWithDynamicOps(this@toCodec, input, ops)!!
            return DataResult.success(result)
        }

        override fun <T : Any?> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<U, T>> {
            val result = decodeWithDynamicOps(this@toCodec, input, ops)
            return DataResult.success(Pair(result, ops.empty()))
        }
    }
}
 */

class KotlinJsonOps(val json: Json.Default) : DynamicOps<JsonElement> {
    override fun empty(): JsonElement {
        return JsonNull
    }

    override fun createNumeric(i: Number): JsonElement {
        println("Encoding: $i")
        return when (i) {
            is Int -> json.encodeToJsonElement(Int.serializer(), i)
            is Long -> json.encodeToJsonElement(Long.serializer(), i)
            is Double -> json.encodeToJsonElement(Double.serializer(), i)
            is Float -> json.encodeToJsonElement(Float.serializer(), i)
            else -> throw Exception("Can't encode numeric here")
        }
    }

    override fun createString(value: String): JsonElement {
        return json.encodeToJsonElement(value)
    }

    override fun remove(input: JsonElement, key: String?): JsonElement {
        TODO("Not yet implemented")
    }

    override fun createList(input: Stream<JsonElement>?): JsonElement {
        TODO("Not yet implemented")
    }

    override fun getStream(input: JsonElement): DataResult<Stream<JsonElement>> {
        TODO("Not yet implemented")
    }

    override fun createMap(map: Stream<Pair<JsonElement, JsonElement>>?): JsonElement {
        TODO("Not yet implemented")
    }

    override fun getMapValues(input: JsonElement): DataResult<Stream<Pair<JsonElement, JsonElement>>> {
        TODO("Not yet implemented")
    }

    override fun mergeToMap(map: JsonElement, key: JsonElement, value: JsonElement): DataResult<JsonElement> {
        return DataResult.success(
            buildJsonObject {
                (map as? JsonObject)?.let {
                    // todo make recursive for deep copy
                    for ((k, v) in it) put(k, v)
                }
                put(key.jsonPrimitive.content, value)
            }
        )
    }

    override fun mergeToList(list: JsonElement, value: JsonElement): DataResult<JsonElement> {
        TODO("Not yet implemented")
    }

    override fun getStringValue(input: JsonElement): DataResult<String> {
        TODO("Not yet implemented")
    }

    override fun getNumberValue(input: JsonElement): DataResult<Number> {
        TODO("Not yet implemented")
    }

    override fun <U : Any?> convertTo(outOps: DynamicOps<U>?, input: JsonElement): U {
        TODO("Not yet implemented")
    }

}




fun <A> Encoder<A>.toKotlinJsonSerializer(ops: KotlinJsonOps = KotlinJsonOps(Json)): KSerializer<A> {
    return object : KSerializer<A> {
        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor("test", PrimitiveKind.INT)

        override fun serialize(encoder: KEncoder, value: A) {
            println("Auto-gen ser encoding..")
            val result = this@toKotlinJsonSerializer.encodeStart(ops, value)
            println("Result: ${result.result().get()}")
            encoder.encodeSerializableValue(JsonElement.serializer(), result.result().get())
        }

        override fun deserialize(decoder: Decoder): A {
            TODO("Not yet implemented")
        }
    }
}


/*

We created a KSX Encoder/Decoder that wraps DynamicOps calls
Now we need to create a DynamicOps that wraps KSX calls

Then...

OurNbtOps.INSTANCE.encodeStart(






WHAT WE WANT:

BlockPos.CODEC.toSerializer()


 */

data class MyPerson(val name: String, val age: Int)

val MyPersonCodec : Codec<MyPerson> = RecordCodecBuilder.create { instance ->
    instance.group(
        Codec.STRING.fieldOf("name").forGetter { it.name },
        Codec.INT.fieldOf("age").forGetter { it.age }
    ).apply(instance, ::MyPerson)
}

@Serializable
data class MyParty(val size: Int, val organizer: @Contextual MyPerson)

fun main() {

    val kotlinOps = KotlinJsonOps(Json.Default)

    val ourJson = Json {
        serializersModule = SerializersModule {
            contextual(MyPersonCodec.toKotlinJsonSerializer())
        }
    }

    // a Codec is a set of instructions explaining how to encode or decode an object
    // as such, it's the same as a Kotlin KSerializer

    // KSerializer is our version of Codec
    val ser = MyPersonCodec.toKotlinJsonSerializer(kotlinOps)

    // Abstract[En/De]coder is our version of DynamicOps
    // Json is our version of JsonOps
    val result = ourJson.encodeToJsonElement(ser, MyPerson("Jimothy", 36))


    /*
    We created an AED that wrapped a DynamicOps,

     */

    println(result)


    val party = MyParty(33, MyPerson("Jimothy", 36))

    val partyEncoded = ourJson.encodeToJsonElement(MyParty.serializer(), party)

    println(partyEncoded)


}


