import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.Encoder
import com.mojang.serialization.JsonOps
import decoder.AbstractOpDecoder
import decoder.DynamicObjectDecoder
import encoder.AbstractOpEncoder
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.serializer

// ### Encoding ###

@OptIn(ExperimentalSerializationApi::class)
fun <T, U : Any> encodeWithDynamicOps(serializer: SerializationStrategy<U>, obj: U, ops: DynamicOps<T>): T? {
    println("Picking kind: ${serializer.descriptor.kind}")
    val encoder = AbstractOpEncoder.pickEncoder(serializer.descriptor, ops)
    encoder.encodeSerializableValue(serializer, obj)
    return encoder.getResult()
}

inline fun <T, reified U : Any> DynamicOps<T>.serialize(obj: U): T? {
    return encodeWithDynamicOps(serializer<U>(), obj, this)
}

fun <U : Any> createEncoderFromSerializer(serializer: SerializationStrategy<U>): Encoder<U> {
    return object : Encoder<U> {
        override fun <T : Any> encode(input: U, ops: DynamicOps<T>, prefix: T): DataResult<T> {
            val result = encodeWithDynamicOps(serializer, input, ops)!!
            return DataResult.success(result)
        }
    }
}

// ### Decoding ###

@OptIn(ExperimentalSerializationApi::class)
fun <T, U : Any> decodeWithDynamicOps(serializer: DeserializationStrategy<U>, obj: T, ops: DynamicOps<T>): U {
    println("Picking kind: ${serializer.descriptor.kind}")
    val decoder = AbstractOpDecoder.pickDecoder(serializer.descriptor, ops, obj)
    return serializer.deserialize(decoder)
}

inline fun <T, reified U : Any> DynamicOps<T>.deserialize(obj: T): U {
    return decodeWithDynamicOps(serializer<U>(), obj, this)
}

fun main() {
    val result = JsonOps.INSTANCE.serialize(true)
    println("Encoded Data: $result")

    val bob = Person("Bob")

    val bobEncoded = JsonOps.INSTANCE.serialize(bob)

    println("\n\n### DECODING NOW! ###\n\n")

    val bobDecoded = decodeWithDynamicOps(Person.serializer(), bobEncoded!!, JsonOps.INSTANCE)

    println("Decoded Data: $bobDecoded")

}