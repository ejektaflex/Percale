package io.ejekta.percale.reverse

import io.ejekta.percale.decoder.PassDecoder
import io.ejekta.percale.encoder.PassEncoder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.IntArraySerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.nbt.*

object NbtStringSerializer : KSerializer<StringTag> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("percale.StringTag", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: StringTag) {
        encoder.encodeString(value.asString)
    }
    override fun deserialize(decoder: Decoder): StringTag {
        return StringTag.valueOf(decoder.decodeString())
    }
}

object NbtIntSerializer : KSerializer<IntTag> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("percale.IntTag", PrimitiveKind.INT)
    override fun serialize(encoder: Encoder, value: IntTag) {
        encoder.encodeInt(value.asInt)
    }
    override fun deserialize(decoder: Decoder): IntTag {
        return IntTag.valueOf(decoder.decodeInt())
    }
}

object NbtLongSerializer : KSerializer<LongTag> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("percale.LongTag", PrimitiveKind.LONG)
    override fun serialize(encoder: Encoder, value: LongTag) {
        encoder.encodeLong(value.asLong)
    }
    override fun deserialize(decoder: Decoder): LongTag {
        return LongTag.valueOf(decoder.decodeLong())
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
        encoder.encodeSerializableValue(ser, value.allKeys.associateWith { value.get(it)!! })
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
        encoder.encodeSerializableValue(ser, value.asIntArray)
    }
    override fun deserialize(decoder: Decoder): IntArrayTag {
        return IntArrayTag(decoder.decodeSerializableValue(ser))
    }
}

object TagSerializer : KSerializer<Tag> {
    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    // Even if NBT won't use this, it's useful for JsonOps and such
    override val descriptor: SerialDescriptor = buildSerialDescriptor("percale.Tag", PolymorphicKind.OPEN) {
        element("percale.IntTag", NbtIntSerializer.descriptor)
        element("percale.StringTag", NbtStringSerializer.descriptor)
        element("percale.CompoundTag", CompoundTagSerializer.descriptor)
        element("percale.ListTag", NbtListSerializer.descriptor)
        element("percale.IntArrayTag", NbtIntArraySerializer.descriptor)
        element("percale.LongTag", NbtLongSerializer.descriptor)
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
        val inp = pass.input as Tag
        val deser = fromInput(inp)
        return pass.decodeSerializableValue(deser, inp)
    }

    fun fromInput(input: Tag): KSerializer<Tag> {
        val ser =  when (input) {
            is StringTag -> NbtStringSerializer
            is IntTag -> NbtIntSerializer
            is CompoundTag -> CompoundTagSerializer
            is ListTag -> NbtListSerializer
            is IntArrayTag -> NbtIntArraySerializer
            is LongTag -> NbtLongSerializer
            else -> throw Exception("TagSerializer does not know what serializer to use for this type: ${input.type}")
            //...etc
        }
        return ser as KSerializer<Tag>
    }
}




