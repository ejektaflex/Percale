package io.ejekta.percale.encoder

import com.mojang.serialization.DynamicOps
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.modules.SerializersModule

@OptIn(ExperimentalSerializationApi::class)
class PassObjectEncoder<T>(override val ops: DynamicOps<T>, serialMod: SerializersModule) : PassEncoder<T>(ops, serialMod) {

    override fun encodeFunc(func: () -> T) {
        if (shortCircuitKey) {
            throw SerializationException("Currently, only strings can be map keys!")
        } else {
            push(func())
        }
    }

    override fun push(result: T) {
        mapBuilder[currentTag] = result
    }

    // To handle current tag (field name) context
    private var currentTag: String = ""

    private val mapBuilder = mutableMapOf<String, T>()
    private val nestedEncoders = mutableMapOf<String, PassEncoder<T>>()

    private var shortCircuitKey = false

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        // Root encoder will have no tag name
        if (currentTag == "") {
            return this
        }
        val nestedEncoder = pickEncoder(descriptor, ops, serializersModule)
        nestedEncoders[currentTag] = nestedEncoder
        return nestedEncoder
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        // Merge any nested encoders that were created over the course of this structure into the structure itself
        if (nestedEncoders.isNotEmpty()) {
            for ((neKey, neVal) in nestedEncoders) {
                mapBuilder[neKey] = neVal.getResult()
            }
        }
    }

    // We are okay with shortCircuitKey here since we want to encode map keys
    override fun encodeString(value: String) {
        if (shortCircuitKey) {
            currentTag = value
        } else {
            push(ops.createString(value))
        }
    }

    override fun getResult(): T {
        // We only ever encoded a single primitive if this happened
        if (mapBuilder.keys.intersect(setOf("")).size == 1) {
            return mapBuilder[""]!!
        }
        return ops.createMap(mapBuilder.map { entry -> ops.createString(entry.key) to entry.value }.toMap())
    }

    override fun encodeElement(descriptor: SerialDescriptor, index: Int): Boolean {
        if (!super.encodeElement(descriptor, index)) return false
        if (descriptor.kind == StructureKind.MAP) {
            shortCircuitKey = (index % 2 == 0) // Every even index will short circuit key
            return true
        }
        currentTag = descriptor.getElementName(index)
        return true
    }
}