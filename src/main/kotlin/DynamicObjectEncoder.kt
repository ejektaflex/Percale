package io.ejekta.kambrikx.serial

import AbstractOpEncoder
import com.mojang.serialization.DynamicOps
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.EmptySerializersModule

@OptIn(ExperimentalSerializationApi::class)
class DynamicObjectEncoder<T>(private val ops: DynamicOps<T>) : AbstractOpEncoder<T>() {

    // To handle current tag (field name) context
    private var currentTag: String = ""

    private val mapBuilder = mutableMapOf<String, T>()
    private val nestedEncoders = mutableMapOf<String, AbstractOpEncoder<T>>()

    override val serializersModule = EmptySerializersModule()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        // Root encoder will have no tag name
        if (currentTag == "") {
            return this
        }
        println("Beginning Structure: $descriptor for key: $currentTag - (${descriptor.kind})")
        val nestedEncoder: AbstractOpEncoder<T> = when (descriptor.kind) {
            is StructureKind.CLASS -> DynamicObjectEncoder(ops)
            is StructureKind.LIST -> DynamicArrayEncoder(ops)
            else -> throw Exception("What encoder do we use for this?: ${descriptor.kind} - $descriptor")
        }
        nestedEncoders[currentTag] = nestedEncoder
        return nestedEncoder
    }


    override fun endStructure(descriptor: SerialDescriptor) {
        println("Ending Structure: $descriptor")
        if (nestedEncoders.isNotEmpty()) {
            for ((neKey, neVal) in nestedEncoders) {
                mapBuilder[neKey] = neVal.getResult()
            }
        }
    }

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        println("BEGINNING COLLECTION: $descriptor - ${descriptor.kind}")
        return super.beginCollection(descriptor, collectionSize)
    }

    override fun encodeString(value: String) {
        println("Encoding string: $value")
        mapBuilder[currentTag] = ops.createString(value)
    }

    override fun encodeInt(value: Int) {
        println("Encoding int")
        mapBuilder[currentTag] = ops.createInt(value)
    }

    override fun encodeBoolean(value: Boolean) {
        mapBuilder[currentTag] = ops.createBoolean(value)
    }

    override fun encodeDouble(value: Double) {
        mapBuilder[currentTag] = ops.createDouble(value)
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
        return ops.createMap(mapBuilder.map { entry -> ops.createString(entry.key) to entry.value }.toMap())
        //return ops.createMap(mapBuilder.mapValues { ops.createString(it.value.toString()) })
    }

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        currentTag = descriptor.getElementName(index)
        return true
    }
}