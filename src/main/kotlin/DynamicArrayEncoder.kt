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
    val nestedEncoders = mutableMapOf<String, DynamicArrayEncoder<T>>()

    override val serializersModule = EmptySerializersModule()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return this
//        println("Beginning Structure: $descriptor for key: $currentTag - (${descriptor.kind})")
//        val nestedEncoder = DynamicArrayEncoder(ops)
//        nestedEncoders[currentTag] = nestedEncoder
//        return nestedEncoder
    }


    override fun endStructure(descriptor: SerialDescriptor) {
//        println("Ending Structure: $descriptor")
//        if (nestedEncoders.isNotEmpty()) {
//            for ((neKey, neVal) in nestedEncoders) {
//                listBuilder[neKey] = neVal.getResult()
//            }
//        }
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
        println("Serializing value..: $value")
//        val obj = DynamicEncoder(ops).apply {
//            encodeSerializableValue(serializer, value)
//        }.getResult()
        super.encodeSerializableValue(serializer, value)
        //println("Ended serializing value ($value). Got: ${nestedEncoders.last().getResult()}")
        println("BTW: nested has: ${nestedEncoders.size}")
    }

    // Implement other encode methods as necessary...

    override fun getResult(): T {
        return ops.createList(listBuilder.stream())
    }

    // To handle current tag (field name) context
    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        currentIndex = index
        return true
    }
}