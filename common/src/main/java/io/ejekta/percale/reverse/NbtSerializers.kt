package io.ejekta.percale.reverse

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import io.ejekta.percale.decoder.PassDecoder
import io.ejekta.percale.encoder.PassEncoder
import io.ejekta.percale.serialize
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.LongArraySerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.nbt.*

object NbtStringSerializer : KSerializer<StringTag> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("percale.StringTag", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: StringTag) {
        encoder.encodeString(value.asString().orElseThrow())
    }
    override fun deserialize(decoder: Decoder): StringTag {
        // If inverse, serialize
        val pass = decoder as? PassDecoder<*>
//        if (pass?.ops is JsonOps) {
//            val newDec = NbtOps.INSTANCE.serialize(pass.input as JsonPrimitive, GsonStringSerializer, pass.serializersModule)
//            return newDec as StringTag
//        }
        return StringTag.valueOf(decoder.decodeString())
    }
}

object NbtIntSerializer : KSerializer<IntTag> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("percale.IntTag", PrimitiveKind.INT)
    override fun serialize(encoder: Encoder, value: IntTag) {
        encoder.encodeInt(value.asInt().orElseThrow())
    }
    override fun deserialize(decoder: Decoder): IntTag {
        return IntTag.valueOf(decoder.decodeInt())
    }
}

object NbtByteSerializer : KSerializer<ByteTag> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("percale.ByteTag", PrimitiveKind.BYTE)
    override fun serialize(encoder: Encoder, value: ByteTag) {
        encoder.encodeByte(value.asByte().orElseThrow())
    }
    override fun deserialize(decoder: Decoder): ByteTag {
        return ByteTag.valueOf(decoder.decodeByte())
    }
}

object NbtLongSerializer : KSerializer<LongTag> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("percale.LongTag", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: LongTag) {
        encoder.encodeLong(value.asLong().orElseThrow())
    }
    override fun deserialize(decoder: Decoder): LongTag {
        return LongTag.valueOf(decoder.decodeLong())
    }
}

object NbtFloatSerializer : KSerializer<FloatTag> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("percale.FloatTag", PrimitiveKind.FLOAT)
    override fun serialize(encoder: Encoder, value: FloatTag) {
        encoder.encodeFloat(value.asFloat().orElseThrow())
    }
    override fun deserialize(decoder: Decoder): FloatTag {
        return FloatTag.valueOf(decoder.decodeFloat())
    }
}

object NbtDoubleSerializer : KSerializer<DoubleTag> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("percale.DoubleTag", PrimitiveKind.DOUBLE)
    override fun serialize(encoder: Encoder, value: DoubleTag) {
        encoder.encodeDouble(value.asDouble().orElseThrow())
    }
    override fun deserialize(decoder: Decoder): DoubleTag {
        return DoubleTag.valueOf(decoder.decodeDouble())
    }
}

object NbtShortSerializer : KSerializer<ShortTag> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("percale.ShortTag", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: ShortTag) {
        encoder.encodeShort(value.asShort().orElseThrow())
    }
    override fun deserialize(decoder: Decoder): ShortTag {
        return ShortTag.valueOf(decoder.decodeShort())
    }
}

object NbtByteArraySerializer : KSerializer<ByteArrayTag> {
    private val ser: KSerializer<List<Byte>>
        get() = ListSerializer(Byte.serializer())
    override val descriptor: SerialDescriptor = deferred { ser.descriptor }
    override fun serialize(encoder: Encoder, value: ByteArrayTag) {
        if (encoder is PassEncoder<*>) {
            @Suppress("UNCHECKED_CAST")
            (encoder as PassEncoder<Tag>).encodeFunc { value }
            return
        }
        encoder.encodeSerializableValue(ser, value.asByteArray().orElseThrow().toList())
    }
    override fun deserialize(decoder: Decoder): ByteArrayTag {
        val bytes = decoder.decodeSerializableValue(ser).toByteArray()
        return ByteArrayTag(bytes)
    }
}

object NbtListSerializer : KSerializer<ListTag> {
    private val ser: KSerializer<List<Tag>>
        get() = ListSerializer(TagSerializer)
    override val descriptor: SerialDescriptor = deferred { ser.descriptor }
    override fun serialize(encoder: Encoder, value: ListTag) {
        encoder.encodeSerializableValue(ser, value)
    }
    override fun deserialize(decoder: Decoder): ListTag {
        val ListTag = decoder.decodeSerializableValue(ser)
        val baseList = ListTag()
        for (item in ListTag) {
            baseList.add(item)
        }
        return baseList
    }
}

object CompoundTagSerializer : KSerializer<CompoundTag> {
    private val ser: KSerializer<Map<String, Tag>>
        get() = MapSerializer(String.serializer(), TagSerializer)
    override val descriptor: SerialDescriptor = deferred { ser.descriptor }
    override fun serialize(encoder: Encoder, value: CompoundTag) {
        encoder.encodeSerializableValue(ser, value.keySet().associateWith { value.get(it)!! })
    }
    override fun deserialize(decoder: Decoder): CompoundTag {
        val nbtMap = decoder.decodeSerializableValue(ser)
        val baseCompound = CompoundTag()
        for ((key, value) in nbtMap) {
            baseCompound.put(key, value)
        }
        return baseCompound
    }
}

object NbtIntArraySerializer : KSerializer<IntArrayTag> {
    private val ser: KSerializer<IntArray>
        get() = IntArraySerializer()
    override val descriptor: SerialDescriptor = deferred { ser.descriptor }
    override fun serialize(encoder: Encoder, value: IntArrayTag) {
        if (encoder is PassEncoder<*>) {
            @Suppress("UNCHECKED_CAST")
            (encoder as PassEncoder<Tag>).encodeFunc { value }
            return
        }
        encoder.encodeSerializableValue(ser, value.asIntArray().orElseThrow())
    }
    override fun deserialize(decoder: Decoder): IntArrayTag {
        return IntArrayTag(decoder.decodeSerializableValue(ser))
    }
}

object NbtLongArraySerializer : KSerializer<LongArrayTag> {
    private val ser: KSerializer<LongArray>
        get() = LongArraySerializer()
    override val descriptor: SerialDescriptor = deferred { ser.descriptor }
    override fun serialize(encoder: Encoder, value: LongArrayTag) {
        if (encoder is PassEncoder<*>) {
            @Suppress("UNCHECKED_CAST")
            (encoder as PassEncoder<Tag>).encodeFunc { value }
            return
        }
        encoder.encodeSerializableValue(ser, value.asLongArray().orElseThrow())
    }
    override fun deserialize(decoder: Decoder): LongArrayTag {
        return LongArrayTag(decoder.decodeSerializableValue(ser))
    }
}

object TagSerializer : KSerializer<Tag> {
    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    // Even if NBT won't use this, it's useful for JsonOps and such
    override val descriptor: SerialDescriptor = buildSerialDescriptor("percale.Tag", PolymorphicKind.OPEN) {
        element("percale.IntTag", NbtIntSerializer.descriptor)
        element("percale.ByteTag", NbtByteSerializer.descriptor)
        element("percale.StringTag", NbtStringSerializer.descriptor)
        element("percale.FloatTag", NbtFloatSerializer.descriptor)
        element("percale.DoubleTag", NbtDoubleSerializer.descriptor)
        element("percale.CompoundTag", CompoundTagSerializer.descriptor)
        element("percale.ListTag", NbtListSerializer.descriptor)
        element("percale.ByteArrayTag", NbtByteArraySerializer.descriptor)
        element("percale.IntArrayTag", NbtIntArraySerializer.descriptor)
        element("percale.LongTag", NbtLongSerializer.descriptor)
        element("percale.LongArrayTag", NbtLongArraySerializer.descriptor)
        //...etc
    }

    override fun serialize(encoder: Encoder, value: Tag) {
        if (encoder is PassEncoder<*>) {
            val ser = fromInput(value)
            return encoder.encodeSerializableValue(ser, value)
        }
        return encoder.encodeSerializableValue(PolymorphicSerializer(Tag::class), value)
    }

    override fun deserialize(decoder: Decoder): Tag {
        // If not an NBT pass decoder, then this could be an Tag being serialized by JsonOps! handle normally in that instance
        val pass = decoder as? PassDecoder<*> ?: return decoder.decodeSerializableValue(PolymorphicSerializer(Tag::class))

        // If inverse, serialize
//        if (pass.ops is JsonOps) {
//            val newDec = NbtOps.INSTANCE.serialize(pass.input as JsonElement, GsonElementSerializer, pass.serializersModule)
//            return newDec as Tag
//        }

        val inp = (pass.currentValue ?: pass.input) as Tag
        val deser = fromInput(inp)
        @Suppress("UNCHECKED_CAST")
        val nestedDecoder = PassDecoder.pickDecoder(
            deser.descriptor,
            pass.ops as DynamicOps<Tag>,
            inp,
            pass.level + 1,
            pass.serializersModule
        )
        return deser.deserialize(nestedDecoder)
    }

    fun fromInput(input: Tag): KSerializer<Tag> {
        val ser =  when (input) {
            is StringTag -> NbtStringSerializer
            is IntTag -> NbtIntSerializer
            is CompoundTag -> CompoundTagSerializer
            is ByteTag -> NbtByteSerializer
            is FloatTag -> NbtFloatSerializer
            is DoubleTag -> NbtDoubleSerializer
            is ListTag -> NbtListSerializer
            is ByteArrayTag -> NbtByteArraySerializer
            is IntArrayTag -> NbtIntArraySerializer
            is LongTag -> NbtLongSerializer
            is LongArrayTag -> NbtLongArraySerializer
            is ShortTag -> NbtShortSerializer
            else -> throw Exception("TagSerializer does not know what serializer to use for this type: ${input.type}")
            //...etc
        }
        return ser as KSerializer<Tag>
    }
}
