package io.ejekta.percale.decoder

import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import io.ejekta.percale.Percale
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.modules.SerializersModule
import net.minecraft.nbt.Tag
import net.minecraft.resources.RegistryOps
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalSerializationApi::class)
abstract class PassDecoder<T>(open val ops: DynamicOps<T>, val level: Int, serialMod: SerializersModule) : AbstractDecoder() {
    abstract fun <V> decodeFunc(func: () -> DataResult<V>): V
    abstract val currentValue: T?
    abstract val input: T

    override val serializersModule = serialMod

    override fun decodeString(): String {
        return decodeFunc { ops.getStringValue(currentValue) }
    }

    override fun decodeInt(): Int {
        return decodeFunc { ops.getNumberValue(currentValue) }.toInt()
    }

    override fun decodeBoolean(): Boolean {
        // NBT has no bool, so sometimes it will get converted to number
        return try {
            decodeFunc {
                ops.getBooleanValue(currentValue)
            }
        } catch (ise: IllegalStateException) {
            decodeFunc { ops.getNumberValue(currentValue) }.run {
                this.toByte().toInt() != 0
            }
        }
    }

    override fun decodeLong(): Long {
        return decodeFunc { ops.getNumberValue(currentValue) }.toLong()
    }

    override fun decodeFloat(): Float {
        return decodeFunc { ops.getNumberValue(currentValue) }.toFloat()
    }

    override fun decodeDouble(): Double {
        return decodeFunc { ops.getNumberValue(currentValue) }.toDouble()
    }

    override fun decodeShort(): Short {
        return decodeFunc { ops.getNumberValue(currentValue) }.toShort()
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        return enumDescriptor.getElementIndex(decodeString())
    }

    override fun decodeByte(): Byte {
        return decodeFunc { ops.getNumberValue(currentValue) }.toByte()
    }

    override fun decodeNotNullMark(): Boolean {
        return true
    }

    override fun decodeNull(): Nothing? {
        return null
    }

    override fun decodeSequentially(): Boolean {
        return false
    }

    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        Percale.syslog(level, "SERVAL decode required for ${currentValue ?: input}")
        val pickedDecoder = pickDecoder(deserializer.descriptor, ops, currentValue ?: input, level + 1, serializersModule)
        //Percale.syslog(level, "Picked decoder: ${pickedDecoder.javaClass.simpleName} for value: ${currentValue ?: input}")
        return deserializer.deserialize(pickedDecoder).also { Percale.syslog(level + 1, "decoded: $it") }
    }

    override fun <T : Any> decodeNullableSerializableValue(deserializer: DeserializationStrategy<T?>): T? {
        return decodeSerializableValue(deserializer)
    }

    companion object {
        fun <V> pickDecoder(descriptor: SerialDescriptor, inOps: DynamicOps<V>, input: V, level: Int = 0, serialMod: SerializersModule): PassDecoder<V> {
            Percale.syslog(level, "Picking decoder for $descriptor - ${descriptor.kind}")

            val ops = inOps

            return when (descriptor.kind) {
                StructureKind.CLASS, PolymorphicKind.OPEN, PolymorphicKind.SEALED -> PassObjectDecoder(ops, input, level + 1, serialMod)
                is PrimitiveKind, SerialKind.ENUM, SerialKind.CONTEXTUAL  -> PassPrimitiveDecoder(ops, input, level + 1, serialMod)
                StructureKind.MAP -> PassMapDecoder(ops, input, level + 1, serialMod)
                StructureKind.LIST -> PassListDecoder(ops, input, level + 1, serialMod)
                else -> throw SerializationException("Unsupported descriptor type for our DynamicOps encoder: ${descriptor.kind}, ${descriptor.kind::class}")
            }.also {
                Percale.syslog(level, "Picked ${it::class.simpleName} for input: $input")
            }
        }
    }
}