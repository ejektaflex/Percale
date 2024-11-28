package io.ejekta.percale.reverse

import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.ListCodec
import io.ejekta.percale.Percale
import io.ejekta.percale.decoder.PassDecoder
import io.ejekta.percale.encoder.PassEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.LongArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.resources.RegistryOps
import net.minecraft.util.NullOps
import net.minecraft.world.item.ItemStack

fun <A> Codec<A>.toSerializer(like: KSerializer<*>): KSerializer<A> {
    return toSerializer(like.descriptor)
}

private val baseLookup: Map<Codec<*>, KSerializer<*>> = mapOf(
    Codec.BOOL to Boolean.serializer(),
    Codec.BYTE to Byte.serializer(),
    // Codec.BYTE_BUFFER
    Codec.DOUBLE to Double.serializer(),
    // Codec.EMPTY
    Codec.FLOAT to Float.serializer(),
    Codec.INT to Int.serializer(),
    Codec.INT_STREAM to IntArraySerializer(),
    Codec.LONG to Long.serializer(),
    Codec.LONG_STREAM to LongArraySerializer(),
    // Codec.PASSTHROUGH
    Codec.SHORT to Short.serializer(),
    Codec.STRING to String.serializer()
)

private val mcLookup: Map<Codec<*>, KSerializer<*>> = mapOf(

)

fun <A> Codec<A>.guessSerializer(): KSerializer<*>? {
    when (this) {
        ItemStack.CODEC -> return JsonObject.serializer()
    }
    return getSerializerFromName(toString())
}

private fun getSerializerFromName(codecName: String): KSerializer<*>? {
    val prefix = codecName.substringBefore("[")
    println("Prefix: $prefix")
    val heuristic: KSerializer<*>? = when (prefix) {
        "IntStream" -> baseLookup[Codec.INT_STREAM]
        "String" -> baseLookup[Codec.STRING]
        "ListCodec" -> {
            val listCodecPath = codecName.split("][").first() + "]"
            println("LC: '$listCodecPath'")
            val listCodecType = codecName.substringAfter("[").substringBeforeLast("]")
            println("LCT: $listCodecType")

            val subCodecSerial = getSerializerFromName(listCodecType)

            println("SCS: $subCodecSerial")

            subCodecSerial?.let {
                ListSerializer(it)
            }
        }
        else -> null
    }
    return heuristic
}

// TODO
//fun <A> MapCodec<A>.toSerializer(typeDescriptor: SerialDescriptor? = null): KSerializer<Map<String, A>> {
//    return MapSerializer(String.serializer(), codec().toSerializer(typeDescriptor))
//}

fun <A> Codec<A>.toSerializer(typeDescriptor: SerialDescriptor? = null): KSerializer<A> {
    return object : KSerializer<A> {
        override val descriptor: SerialDescriptor
            get() {
                // If we supply one directly, use it
                if (typeDescriptor != null) {
                    return typeDescriptor
                }

                // Otherwise, if an CompoundTag codec, then use that
                if (this@toSerializer == CompoundTag.CODEC) {
                    return CompoundTagSerializer.descriptor
                }

                // Otherwise, static lookup
//                descriptorLookup[this@toSerializer]?.let {
//                    return it
//                }
                // Otherwise, static list codec type lookup
                println(this@toSerializer is ListCodec<*>)

                println("Must heuristically generate a SerialDescriptor for: ${this@toSerializer}")

                // Otherwise, icky heuristic lookup
                val heuristic = guessSerializer()?.descriptor

                return heuristic ?: throw Exception("No descriptor found for codec: $this@toSerializer (${this@toSerializer::class.simpleName})")// PrimitiveSerialDescriptor("UNKNOWN_SER", PrimitiveKind.STRING)
            }
        override fun serialize(encoder: Encoder, value: A) {
            val pass = (encoder as? PassEncoder<*>)?.ops ?: throw Exception("Cannot serialize a non-dynamicops format with this serializer!")

            println("SERIAL ME!!")
            println(pass::class)
            println()

            val realOps = if (pass is RegistryOps) {
                println("REALLY WAS: ${pass.delegate}")
                pass.delegate // uses AT/AW
            } else {
                pass
            }

            when (realOps) {
                is NullOps -> {
                    // No-op
                    println("Skipping nullops serialize")
                }
                is JsonOps -> {
                    val result = this@toSerializer.encodeStart(pass, value)
                    if (result.isError) {
                        throw SerializationException("Cannot auto-serialize codec, msg: ${result.error().get().message()}")
                    }
                    println("RES: ${result.orThrow}")
                    encoder.encodeSerializableValue(GsonElementSerializer, result.orThrow as JsonElement)
                }
                is NbtOps -> {
                    val result = this@toSerializer.encodeStart(pass, value)
                    if (result.isError) {
                        throw SerializationException("Cannot auto-serialize codec, msg: ${result.error().get().message()}")
                    }
                    encoder.encodeSerializableValue(TagSerializer, result.orThrow as Tag)
                }
                else -> throw Exception("Unknown ops type!: $pass (${pass::class.simpleName})")
            }
        }
        override fun deserialize(decoder: Decoder): A {
            val passDecoder = (decoder as? PassDecoder<*>) ?: throw Exception("Cannot serialize a non-dynamicops format with this serializer!")
            val pass = passDecoder.ops as DynamicOps<Any>

            val realOps = if (pass is RegistryOps) {
                pass.delegate // uses AT/AW
            } else {
                pass
            }

            println("Must deser: ${passDecoder.input}, ${passDecoder.input!!::class.simpleName}, passClass: ${pass::class.simpleName}, rc: ${realOps::class.simpleName}")

            when (realOps) {
                is NullOps -> {
                    // No-op
                    println("Skipping nullops deserialize")
                    // If this ever breaks, we can maybe passDecoder.decodeSerializableValue with a null value/serialier or something
                    return Unit as A
                }
                is JsonOps -> {
                    //val result = this@toSerializer.parse(pass, passDecoder.input)
                    val jsony = passDecoder.decodeSerializableValue(GsonElementSerializer)
                    println("JSONY: $jsony")
                    val result = this@toSerializer.parse(pass, jsony)
                    println("RESULT: $result")
                    return result.orThrow
                }
                is NbtOps -> {
                    val nbty = passDecoder.decodeSerializableValue(TagSerializer)
                    val result = this@toSerializer.parse(pass, nbty)
                    return result.orThrow
                }
            }


            TODO("Not yet implemented")
        }
    }
}

