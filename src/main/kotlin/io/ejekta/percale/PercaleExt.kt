package io.ejekta.percale

import com.mojang.datafixers.util.Pair
import com.mojang.serialization.*
import io.ejekta.percale.encoder.PassEncoder
import io.ejekta.percale.decoder.PassDecoder
import io.ejekta.percale.reverse.toKotlinJsonSerializer
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.contextual

// ### Encoding ###

@OptIn(ExperimentalSerializationApi::class)
fun <T, U : Any> encodeWithDynamicOps(serializer: SerializationStrategy<U>, obj: U, ops: DynamicOps<T>, serialMod: SerializersModule = EmptySerializersModule()): T? {
    val encoder = PassEncoder.pickEncoder(serializer.descriptor, ops, serialMod)
    encoder.encodeSerializableValue(serializer, obj)
    return encoder.getResult()
}

inline fun <T, reified U : Any> DynamicOps<T>.serialize(obj: U, serialMod: SerializersModule = EmptySerializersModule()): T? {
    return encodeWithDynamicOps(serializer<U>(), obj, this, serialMod)
}

fun <T, U : Any> DynamicOps<T>.serialize(obj: U, serializer: SerializationStrategy<U>, serialMod: SerializersModule = EmptySerializersModule()): T? {
    return encodeWithDynamicOps(serializer, obj, this, serialMod)
}


// ### Decoding ###

@OptIn(ExperimentalSerializationApi::class)
fun <T, U : Any> decodeWithDynamicOps(serializer: DeserializationStrategy<U>, obj: T, ops: DynamicOps<T>, serialMod: SerializersModule = EmptySerializersModule()): U {
    val decoder = PassDecoder.pickDecoder(serializer.descriptor, ops, obj, 0, serialMod)
    return serializer.deserialize(decoder)
}

inline fun <T, reified U : Any> DynamicOps<in T>.deserialize(obj: T, serialMod: SerializersModule = EmptySerializersModule()): U {
    return decodeWithDynamicOps(serializer<U>(), obj, this, serialMod)
}

fun <T, U : Any> DynamicOps<T>.deserialize(obj: T, serializer: DeserializationStrategy<U>, serialMod: SerializersModule = EmptySerializersModule()): U {
    return decodeWithDynamicOps(serializer, obj, this, serialMod)
}

// ### Codec

fun <U : Any> KSerializer<U>.toCodec(serialMod: SerializersModule = EmptySerializersModule()): Codec<U> {
    return object : Codec<U> {
        override fun <T : Any> encode(input: U, ops: DynamicOps<T>, prefix: T?): DataResult<T> {
            val result = encodeWithDynamicOps(this@toCodec, input, ops, serialMod)!!
            return DataResult.success(result)
        }

        override fun <T : Any?> decode(ops: DynamicOps<T>, input: T): DataResult<Pair<U, T>> {
            val result = decodeWithDynamicOps(this@toCodec, input, ops, serialMod)
            return DataResult.success(Pair(result, ops.empty()))
        }
    }
}

inline fun <reified A : Any> SerializersModuleBuilder.codec(codec: Codec<A>, json: Json = Json.Default) {
    contextual(codec.toKotlinJsonSerializer(json))
}