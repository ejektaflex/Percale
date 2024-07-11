import com.mojang.datafixers.util.Pair
import com.mojang.serialization.*
import percale.encoder.PassEncoder
import percale.decoder.PassDecoder
import kotlinx.serialization.*

// ### Encoding ###

@OptIn(ExperimentalSerializationApi::class)
fun <T, U : Any> encodeWithDynamicOps(serializer: SerializationStrategy<U>, obj: U, ops: DynamicOps<T>): T? {
    val encoder = PassEncoder.pickEncoder(serializer.descriptor, ops)
    encoder.encodeSerializableValue(serializer, obj)
    return encoder.getResult()
}

inline fun <T, reified U : Any> DynamicOps<T>.serialize(obj: U): T? {
    return encodeWithDynamicOps(serializer<U>(), obj, this)
}

fun <T, U : Any> DynamicOps<T>.serialize(obj: U, serializer: SerializationStrategy<U>): T? {
    return encodeWithDynamicOps(serializer, obj, this)
}

fun <U : Any> SerializationStrategy<U>.toEncoder(): Encoder<U> {
    return object : Encoder<U> {
        override fun <T : Any> encode(input: U, ops: DynamicOps<T>, prefix: T): DataResult<T> {
            val result = encodeWithDynamicOps(this@toEncoder, input, ops)!!
            return DataResult.success(result)
        }
    }
}

// ### Decoding ###

@OptIn(ExperimentalSerializationApi::class)
fun <T, U : Any> decodeWithDynamicOps(serializer: DeserializationStrategy<U>, obj: T, ops: DynamicOps<T>): U {
    val decoder = PassDecoder.pickDecoder(serializer.descriptor, ops, obj)
    return serializer.deserialize(decoder)
}

inline fun <T, reified U : Any> DynamicOps<in T>.deserialize(obj: T): U {
    return decodeWithDynamicOps(serializer<U>(), obj, this)
}

fun <T, U : Any> DynamicOps<T>.deserialize(obj: T, serializer: DeserializationStrategy<U>): U {
    return decodeWithDynamicOps(serializer, obj, this)
}

fun <U : Any> DeserializationStrategy<U>.toDecoder(): Decoder<U> {
    return object : Decoder<U> {
        override fun <T : Any?> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<U, T>> {
            val result = decodeWithDynamicOps(this@toDecoder, input, ops)
            return DataResult.success(Pair(result, ops.empty()))
        }
    }
}

// ### Codec

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



fun main() {
//    val result = JsonOps.INSTANCE.serialize(true)
//    println("Encoded Data: $result")
//
//    val bob = Person("Bob")
//
//    val bobEncoded = JsonOps.INSTANCE.serialize(bob)
//
//    println("\n\n### DECODING NOW! ###\n\n")
//
//    //val bobDecoded = JsonOps.INSTANCE.deserialize<JsonElement, Person>(bobEncoded!!)
//    val bobDecoded = passWithDynamicOps(Person.serializer(), bobEncoded!!, JsonOps.INSTANCE)
//
//    println("Decoded Data: $bobDecoded")

    val jimothy = Person("Jimothy", 36)

    println("PeaceTest: ${JsonOps.INSTANCE.serialize(jimothy)}")

    val personCodec = Person.serializer().toCodec()

    val result = personCodec.encodeStart(JsonOps.INSTANCE, jimothy).result().get()

    println(result)

    println("Now decoding...")

    val decoded = personCodec.decode(JsonOps.INSTANCE, result).result().get().first

    println(decoded)

}

