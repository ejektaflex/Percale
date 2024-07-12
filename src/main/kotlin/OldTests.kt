import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.Encoder
import com.mojang.serialization.JavaOps
import com.mojang.serialization.JsonOps
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
import percale.encoder.PassEncoder
import com.google.gson.JsonElement as GsonElement


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

// This is gross but ok
object GsonElementSerializer : KSerializer<GsonElement> {
    override val descriptor: SerialDescriptor
        get() = JsonElement.serializer().descriptor
    override fun serialize(encoder: KEncoder, value: GsonElement) {
        encoder.encodeSerializableValue(JsonElement.serializer(), Json.decodeFromString(JsonElement.serializer(), value.toString()))
    }

    override fun deserialize(decoder: Decoder): GsonElement {
        TODO("Not yet implemented")
    }
}

class PassOps<A : Any>(val ops: DynamicOps<A>) : DynamicOps<A> {
    override fun empty(): A {
        return ops.empty()
    }

    override fun getNumberValue(input: A): DataResult<Number> {
        TODO("Not yet implemented")
    }

    override fun <U : Any?> convertTo(outOps: DynamicOps<U>?, input: A): U {
        TODO("Not yet implemented")
    }

    override fun createNumeric(i: Number): A {
        val pEnc = PassEncoder.pickEncoder(PrimitiveSerialDescriptor("yes", PrimitiveKind.INT), ops)
        pEnc.encodeSerializableValue(Int.serializer(), i.toInt())
        return pEnc.getResult()
    }

    override fun createString(value: String): A {
        val pEnc = PassEncoder.pickEncoder(PrimitiveSerialDescriptor("yess", PrimitiveKind.STRING), ops)
        pEnc.encodeSerializableValue(String.serializer(), value)
        return pEnc.getResult()
    }

    override fun remove(input: A, key: String?): A {
        TODO("Not yet implemented")
    }

    override fun createList(input: Stream<A>?): A {
        TODO("Not yet implemented")
    }

    override fun getStream(input: A): DataResult<Stream<A>> {
        TODO("Not yet implemented")
    }

    override fun createMap(map: Stream<Pair<A, A>>?): A {
        TODO("Not yet implemented")
    }

    override fun getMapValues(input: A): DataResult<Stream<Pair<A, A>>> {
        TODO("Not yet implemented")
    }

    /*
    TODO in the case that value is a serializable type, we can perhaps pass a serialModule to the entire Ops and then
    grab the serializer contextually ?? maybe this won't work but there might be something to it
     */
    override fun mergeToMap(map: A, key: A, value: A): DataResult<A> {
        return ops.mergeToMap(map, key, value)
    }

    override fun mergeToList(list: A, value: A): DataResult<A> {
        TODO("Not yet implemented")
    }

    override fun getStringValue(input: A): DataResult<String> {
        TODO("Not yet implemented")
    }
}





fun <A> Encoder<A>.toKotlinJsonSerializer(): KSerializer<A> {
    return toGenericSer(JsonOps.INSTANCE, GsonElementSerializer)
}

fun <A, U> Encoder<A>.toGenericSer(ops: DynamicOps<U>, opsSerializer: KSerializer<U>): KSerializer<A> {
    return object : KSerializer<A> {
        override val descriptor: SerialDescriptor
            get() = PrimitiveSerialDescriptor("test", PrimitiveKind.INT)

        override fun serialize(encoder: KEncoder, value: A) {
            val result = this@toGenericSer.encodeStart(ops, value).result().get()
            encoder.encodeSerializableValue(opsSerializer, result)
        }

        override fun deserialize(decoder: Decoder): A {
            TODO("Not yet implemented")
        }
    }
}

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


    val ourJson = Json {
        serializersModule = SerializersModule {
            contextual(MyPersonCodec.toKotlinJsonSerializer())
        }
    }

    // a Codec is a set of instructions explaining how to encode or decode an object
    // as such, it's the same as a Kotlin KSerializer

    // KSerializer is our version of Codec
    val ser = MyPersonCodec.toKotlinJsonSerializer()

    // Abstract[En/De]coder is our version of DynamicOps
    // Json is our version of JsonOps
    val result = ourJson.encodeToJsonElement(ser, MyPerson("Jimothy", 36))


    /*
    We created an AED that wrapped a DynamicOps,

     */

    println(result)



    val passOps = PassOps(JsonOps.INSTANCE)
    val passSer = MyPersonCodec.toGenericSer(passOps, GsonElementSerializer)

    val personPassEncoded = ourJson.encodeToString(passSer, MyPerson("Jimothy", 36))

    println("PE:")
    println(personPassEncoded)

//    val party = MyParty(33, MyPerson("Jimothy", 36))
//
//    val partyEncoded = ourJson.encodeToJsonElement(MyParty.serializer(), party)
//
//    println(partyEncoded)


}


