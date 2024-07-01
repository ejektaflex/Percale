package io.ejekta.kambrikx.serial

import AbstractOpEncoder
import com.mojang.serialization.DynamicOps
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.EmptySerializersModule

@OptIn(ExperimentalSerializationApi::class)
class DynamicArrayEncoder<T>(private val ops: DynamicOps<T>) : AbstractOpEncoder<T>() {

    var currentIndex = 0
    private val listBuilder = mutableListOf<T>()

    override val serializersModule = EmptySerializersModule()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return this
    }

    override fun encodeString(value: String) {
        listBuilder.add(ops.createString(value))
    }

    override fun encodeInt(value: Int) {
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
        currentIndex = index
        return true
    }
}