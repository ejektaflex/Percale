package decoder

import com.mojang.serialization.DynamicOps
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlin.jvm.optionals.getOrNull

@OptIn(ExperimentalSerializationApi::class)
class DynamicObjectDecoder<T>(override val ops: DynamicOps<T>, private val input: T) : AbstractOpDecoder<T>(ops) {

    override val serializersModule = EmptySerializersModule()

    private val inputMap = ops.getMap(input).result().getOrNull()
    private var currentIndex = 0
    private val mapKeys = inputMap?.entries()?.map { entry -> entry.first }?.toList() ?: emptyList()

    private val currentKey: T
        get() = mapKeys[currentIndex]

    // If inputMap is null, then it was not a map and thus is a primitive
    private val currentValue: T
        get() {
            return inputMap?.get(currentKey) ?: input
        }

    private val nestedDecoders = mutableMapOf<T, AbstractOpDecoder<T>>()

    //private var shortCircuitKey = false

    override fun decodeSequentially(): Boolean {
        return true
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        println("Beginning Structure: $descriptor for key: $currentKey - (${descriptor.kind})")
        // Root decoder will have no tag name
        if (currentKey == "") {
            println("Is root decoder")
            return this
        }
        val nestedDecoder = pickDecoder(descriptor, ops, input)
        println("Was not root encoder, using based on ${descriptor.kind}")
        nestedDecoders[currentKey] = nestedDecoder
        return nestedDecoder
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return if (currentIndex < mapKeys.size) currentIndex++ else CompositeDecoder.DECODE_DONE
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        println("Ending Structure: $descriptor")
        // Merge any nested encoders that were created over the course of this structure into the structure itself
        if (nestedDecoders.isNotEmpty()) {
            for ((neKey, neVal) in nestedDecoders) {
                //mapBuilder[neKey] = neVal.getResult()
            }
        }
    }

    override fun decodeString(): String {
        return ops.getStringValue(currentValue).orThrow
    }

    override fun decodeInt(): Int {
        return ops.getNumberValue(currentValue).orThrow.toInt()
    }

    override fun decodeBoolean(): Boolean {
        return ops.getBooleanValue(currentValue).orThrow
    }

    override fun decodeLong(): Long {
        return ops.getNumberValue(currentValue).orThrow.toLong()
    }

    override fun decodeFloat(): Float {
        return ops.getNumberValue(currentValue).orThrow.toFloat()
    }

    override fun decodeDouble(): Double {
        return ops.getNumberValue(currentValue).orThrow.toDouble()
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        return enumDescriptor.getElementIndex(ops.getStringValue(currentValue).orThrow)
    }

    override fun decodeFunc(func: () -> T) {
        TODO("Not yet implemented")
    }

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        println("Decoding element: $descriptor $index")
        return super.decodeInlineElement(descriptor, index)
    }

}