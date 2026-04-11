package io.ejekta.percale

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.*
import io.ejekta.percale.encoder.PassEncoder
import io.ejekta.percale.decoder.PassDecoder
import io.ejekta.percale.reverse.toSerializer
import kotlinx.serialization.*
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.contextual
import kotlin.jvm.optionals.getOrNull

// ### Encoding ###

@PublishedApi
internal fun <T, U : Any> encodeWithDynamicOps(serializer: SerializationStrategy<U>, obj: U, ops: DynamicOps<T>, serialMod: SerializersModule): T? {
    val encoder = PassEncoder.pickEncoder(serializer.descriptor, ops, serialMod)
    encoder.encodeSerializableValue(serializer, obj)
    return encoder.getResult()
}

inline fun <T, reified U : Any> DynamicOps<T>.serialize(obj: U): T? {
    return encodeWithDynamicOps(serializer<U>(), obj, this, EmptySerializersModule())
}

fun <T, U : Any> DynamicOps<T>.serialize(
    obj: U,
    serializer: SerializationStrategy<U>,
    serialMod: SerializersModule = EmptySerializersModule()
): T? {
    return encodeWithDynamicOps(serializer, obj, this, serialMod)
}


// ### Decoding ###

@PublishedApi
internal fun <T, U : Any> decodeWithDynamicOps(serializer: DeserializationStrategy<U>, obj: T, ops: DynamicOps<T>, serialMod: SerializersModule): U {
    val decoder = PassDecoder.pickDecoder(serializer.descriptor, ops, obj, 0, serialMod)
    //println("Picked: ${decoder::class.simpleName}")
    return serializer.deserialize(decoder)
}

inline fun <T, reified U : Any> DynamicOps<in T>.deserialize(obj: T): U {
    return decodeWithDynamicOps(serializer<U>(), obj, this, EmptySerializersModule())
}

fun <T, U : Any> DynamicOps<T>.deserialize(
    obj: T,
    serializer: DeserializationStrategy<U>,
    serialMod: SerializersModule = EmptySerializersModule()
): U {
    return decodeWithDynamicOps(serializer, obj, this, serialMod)
}

// ### Codec

fun <U : Any> KSerializer<U>.toCodec(serialMod: SerializersModule = EmptySerializersModule()): Codec<U> {
    return object : Codec<U> {
        override fun <T : Any> encode(input: U, ops: DynamicOps<T>, prefix: T?): DataResult<T> {
            val result = encodeWithDynamicOps(this@toCodec, input, ops, serialMod)!!
            if (prefix == null || prefix == ops.empty()) {
                return DataResult.success(result)
            }

            ops.getMap(result).result().getOrNull()?.let { mapEntries ->
                return ops.mergeToMap(prefix, mapEntries)
            }

            ops.getStream(result).result().getOrNull()?.toList()?.let { listEntries ->
                return ops.mergeToList(prefix, listEntries)
            }

            return ops.mergeToPrimitive(prefix, result)
        }

        override fun <T : Any?> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<U, T>> {
            val result = decodeWithDynamicOps(this@toCodec, input, ops, serialMod)
            return DataResult.success(Pair(result, input))
        }
    }
}

inline fun <reified A : Any> SerializersModuleBuilder.contextualCodec(codec: Codec<A>) {
    contextual(codec.toSerializer())
}
