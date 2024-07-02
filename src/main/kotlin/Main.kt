import com.mojang.serialization.*
import decoder.DynamicObjectDecoder
import encoder.DynamicListEncoder
import encoder.DynamicObjectEncoder
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

// ### Encoding ###

@OptIn(ExperimentalSerializationApi::class)
fun <T, U : Any> encodeWithDynamicOps(serializer: SerializationStrategy<U>, obj: U, ops: DynamicOps<T>): T? {
    println("Picking kind: ${serializer.descriptor.kind}")
    val encoder = when (serializer.descriptor.kind) {
        StructureKind.LIST -> DynamicListEncoder(ops)
        else -> DynamicObjectEncoder(ops)
    }
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
    val decoder = when (serializer.descriptor.kind) {
        //StructureKind.LIST -> DynamicListDecoder(ops)
        else -> DynamicObjectDecoder(ops, obj)
    }
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

    val bobDecoded = decodeWithDynamicOps(Person.serializer(), bobEncoded!!, JsonOps.INSTANCE)

    println("Decoded Data: $bobDecoded")

}