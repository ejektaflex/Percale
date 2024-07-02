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
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.modules.EmptySerializersModule

@OptIn(ExperimentalSerializationApi::class)
class DynamicObjectEncoder<T>(private val ops: DynamicOps<T>) : AbstractOpEncoder<T>() {

    // To handle current tag (field name) context
    private var currentTag: String = ""

    private val mapBuilder = mutableMapOf<String, T>()
    private val nestedEncoders = mutableMapOf<String, AbstractOpEncoder<T>>()

    private var shortCircuitKey = false

    override val serializersModule = EmptySerializersModule()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        println("Beginning Structure: $descriptor for key: $currentTag - (${descriptor.kind})")
        // Root encoder will have no tag name
        if (currentTag == "") {
            return this
        }
        val nestedEncoder: AbstractOpEncoder<T> = when (descriptor.kind) {
            is StructureKind.CLASS -> DynamicObjectEncoder(ops)
            is StructureKind.LIST -> DynamicArrayEncoder(ops)
            is StructureKind.MAP -> DynamicObjectEncoder(ops)
            else -> throw Exception("What encoder do we use for this?: ${descriptor.kind} - $descriptor")
        }
        nestedEncoders[currentTag] = nestedEncoder
        return nestedEncoder
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        println("Ending Structure: $descriptor")
        // Merge any nested encoders that were created over the course of this structure into the structure itself
        if (nestedEncoders.isNotEmpty()) {
            for ((neKey, neVal) in nestedEncoders) {
                mapBuilder[neKey] = neVal.getResult()
            }
        }
    }

    override fun encodeString(value: String) {
        if (shortCircuitKey) {
            currentTag = value
        } else {
            mapBuilder[currentTag] = ops.createString(value)
        }
    }

    override fun encodeInt(value: Int) {
        if (shortCircuitKey) {
            throw SerializationException("Currently, only strings can be map keys!")
        } else {
            mapBuilder[currentTag] = ops.createInt(value)
        }
    }

    override fun encodeBoolean(value: Boolean) {
        mapBuilder[currentTag] = ops.createBoolean(value)
    }

    override fun encodeDouble(value: Double) {
        mapBuilder[currentTag] = ops.createDouble(value)
    }

    override fun getResult(): T {
        return ops.createMap(mapBuilder.map { entry -> ops.createString(entry.key) to entry.value }.toMap())
    }

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        if (descriptor.kind == StructureKind.MAP) {
            shortCircuitKey = (index % 2 == 0) // Every even index will short circuit key
            return true
        }
        currentTag = descriptor.getElementName(index)
        return true
    }
}