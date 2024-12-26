package io.ejekta.percale.reverse

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.internal.LazilyParsedNumber
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import io.ejekta.percale.decoder.PassDecoder
import io.ejekta.percale.encoder.PassEncoder
import io.ejekta.percale.serialize
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.resources.RegistryOps

object GsonStringSerializer : KSerializer<JsonPrimitive> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("percale.GsonPrimitiveString", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: JsonPrimitive) {
        encoder.encodeString(value.asString)
    }
    override fun deserialize(decoder: Decoder): JsonPrimitive {
        val pass = decoder as? PassDecoder<*>

        // If inverse, serialize
//        if (pass?.ops is NbtOps) {
//            val newDec = JsonOps.INSTANCE.serialize(pass.input as StringTag, NbtStringSerializer, pass.serializersModule)
//            return newDec as JsonPrimitive
//        }

        return JsonPrimitive(decoder.decodeString())
    }
}

object GsonIntSerializer : KSerializer<JsonPrimitive> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("percale.GsonPrimitiveInt", PrimitiveKind.INT)
    override fun serialize(encoder: Encoder, value: JsonPrimitive) {
        encoder.encodeInt(value.asInt)
    }
    override fun deserialize(decoder: Decoder): JsonPrimitive {
        return JsonPrimitive(decoder.decodeInt())
    }
}

object GsonLongSerializer : KSerializer<JsonPrimitive> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("percale.GsonPrimitiveLong", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: JsonPrimitive) {
        encoder.encodeLong(value.asLong)
    }
    override fun deserialize(decoder: Decoder): JsonPrimitive {
        return JsonPrimitive(decoder.decodeLong())
    }
}

object GsonFloatSerializer : KSerializer<JsonPrimitive> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("percale.GsonPrimitiveFloat", PrimitiveKind.FLOAT)
    override fun serialize(encoder: Encoder, value: JsonPrimitive) {
        encoder.encodeFloat(value.asFloat)
    }
    override fun deserialize(decoder: Decoder): JsonPrimitive {
        return JsonPrimitive(decoder.decodeFloat())
    }
}

object GsonLazyNumberSerializer : KSerializer<JsonPrimitive> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("percale.GsonLazyNumber", PrimitiveKind.DOUBLE)
    override fun serialize(encoder: Encoder, value: JsonPrimitive) {
        encoder.encodeDouble(value.asDouble)
    }
    override fun deserialize(decoder: Decoder): JsonPrimitive {
        return JsonPrimitive(decoder.decodeDouble())
    }
}

object GsonBoolSerializer : KSerializer<JsonPrimitive> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("percale.GsonPrimitve", PrimitiveKind.BOOLEAN)
    override fun serialize(encoder: Encoder, value: JsonPrimitive) {
        encoder.encodeBoolean(value.asBoolean)
    }
    override fun deserialize(decoder: Decoder): JsonPrimitive {
        return JsonPrimitive(decoder.decodeBoolean())
    }
}

object GsonObjectSerializer : KSerializer<JsonObject> {
    private val ser
        get() = MapSerializer(String.serializer(), GsonElementSerializer)
    override val descriptor: SerialDescriptor = deferred { ser.descriptor }
    override fun serialize(encoder: Encoder, value: JsonObject) {
        encoder.encodeSerializableValue(ser, value.asMap())
    }
    override fun deserialize(decoder: Decoder): JsonObject {
        val pass = decoder as? PassDecoder<*>

        val jsonMap = decoder.decodeSerializableValue(ser)
        return JsonObject().apply {
            for ((key, value) in jsonMap) {
                add(key, value)
            }
        }
    }
}

object GsonArraySerializer : KSerializer<JsonArray> {
    private val ser
        get() = ListSerializer(GsonElementSerializer)
    override val descriptor: SerialDescriptor = deferred { ser.descriptor }
    override fun serialize(encoder: Encoder, value: JsonArray) {
        encoder.encodeSerializableValue(ser, value.asList())
    }
    override fun deserialize(decoder: Decoder): JsonArray {
        val jsonList = decoder.decodeSerializableValue(ser)
        return JsonArray().apply {
            for (item in jsonList) {
                add(item)
            }
        }
    }
}


object GsonElementSerializer : KSerializer<JsonElement> {
    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    // Even if NBT won't use this, it's useful for JsonOps and such
    override val descriptor: SerialDescriptor = buildSerialDescriptor("percale.Tag", PolymorphicKind.OPEN) {
        element("percale.GsonPrimitiveString", GsonStringSerializer.descriptor)
        element("percale.GsonPrimitiveInt", GsonIntSerializer.descriptor)
        element("percale.GsonPrimitiveLong", GsonLongSerializer.descriptor)
        element("percale.GsonPrimitiveFloat", GsonFloatSerializer.descriptor)
        element("percale.GsonObject", GsonObjectSerializer.descriptor)
        element("percale.GsonArray", GsonArraySerializer.descriptor)
        element("percale.GsonLazyNumber", GsonLazyNumberSerializer.descriptor) // is this the right place for this?
    }

    override fun serialize(encoder: Encoder, value: JsonElement) {
        if (encoder is PassEncoder<*>) {
            val ser = fromInput(value)
            return encoder.encodeSerializableValue(ser, value)
        }
        return encoder.encodeSerializableValue(PolymorphicSerializer(JsonElement::class), value)
    }

    override fun deserialize(decoder: Decoder): JsonElement {
        // If not an NBT pass decoder, then this could be a Tag being serialized by JsonOps! handle normally in that instance
        val pass = decoder as? PassDecoder<*> ?: return decoder.decodeSerializableValue(PolymorphicSerializer(JsonElement::class))

        // If inverse, serialize
//        if (pass.ops is NbtOps) {
//
//            val doot = JsonOps.INSTANCE.serialize(pass.input as Tag, TagSerializer, pass.serializersModule)
//
//            val ro: RegistryOps<JsonElement>
//
//
//
////            val newDec = JsonOps.INSTANCE.serialize(pass.input as Tag, TagSerializer, pass.serializersModule)
////            return newDec as JsonElement
//        }

        val inp = pass.input as JsonElement
        val deser = fromInput(inp)
        return pass.decodeSerializableValue(deser, inp)
    }

    fun fromInput(input: JsonElement): KSerializer<JsonElement> {
        val ser =  when (input) {
            is JsonPrimitive -> {
                when (true) {
                    input.isString -> GsonStringSerializer
                    input.isNumber -> {
                        when (input.asNumber) {
                            is Int -> GsonIntSerializer
                            is Long -> GsonLongSerializer
                            is Float -> GsonFloatSerializer
                            is LazilyParsedNumber -> GsonLazyNumberSerializer
                            else -> {
                                throw Exception("Err: bad input number type: $input was not a known number format of class ${input.asNumber::class.simpleName}")
                            }
                        }
                    }
                    input.isBoolean -> GsonBoolSerializer
                    else -> throw Exception("No gson serial handling set up for: $input")
                }
            }
            is JsonObject -> GsonObjectSerializer
            is JsonArray -> GsonArraySerializer
            else -> throw Exception("GsonElementSerializer does not know what serializer to use for this type: ${input::class.simpleName}")
            //...etc
        }
        return ser as KSerializer<JsonElement>
    }
}