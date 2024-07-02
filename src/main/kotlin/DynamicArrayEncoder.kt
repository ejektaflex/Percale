package io.ejekta.kambrikx.serial

import AbstractOpEncoder
import com.mojang.serialization.DynamicOps
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.EmptySerializersModule

@OptIn(ExperimentalSerializationApi::class)
class DynamicArrayEncoder<T>(private val ops: DynamicOps<T>) : AbstractOpEncoder<T>() {

    var currentIndex = 0
    private val listBuilder = mutableListOf<T>()
    private val nestedEncoders = mutableListOf<AbstractOpEncoder<T>>()

    override val serializersModule = EmptySerializersModule()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        println("LISTY: $descriptor - $currentIndex")
        // Root encoder will have index 0
        if (currentIndex == 0) {
            return this
        }
        println(descriptor.kind)
        val nestedEncoder: AbstractOpEncoder<T> = when (descriptor.kind) {
            StructureKind.CLASS -> DynamicObjectEncoder(ops)
            StructureKind.LIST -> DynamicArrayEncoder(ops)
            else -> throw SerializationException("unsupported descriptor type for our custom array encoder")
        }
        nestedEncoders.add(nestedEncoder)
        return nestedEncoder
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        println("LISTE: $descriptor - leftover: $nestedEncoders")
        for (nestedEncoder in nestedEncoders) {
            listBuilder.add(nestedEncoder.getResult())
        }
    }

    override fun encodeString(value: String) {
        listBuilder.add(ops.createString(value))
    }

    override fun encodeInt(value: Int) {
        println("Encoding int: $value")
        listBuilder.add(ops.createInt(value))
    }

    override fun encodeBoolean(value: Boolean) {
        listBuilder.add(ops.createBoolean(value))
    }

    override fun encodeDouble(value: Double) {
        listBuilder.add(ops.createDouble(value))
    }

    override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        super.encodeSerializableValue(serializer, value)
    }

    override fun getResult(): T {
        return ops.createList(listBuilder.stream())
    }

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        println("ENCODE CALL: $descriptor - $index")
        currentIndex = index
        return true
    }
}