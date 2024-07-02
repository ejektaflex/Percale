package decoder

import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlin.jvm.optionals.getOrNull

@OptIn(ExperimentalSerializationApi::class)
class DynamicObjectDecoder<T>(override val ops: DynamicOps<T>, private val input: T) : AbstractOpDecoder<T>(ops) {

    override val serializersModule = EmptySerializersModule()

    private val inputMap = ops.getMap(input).result().getOrNull()
    private var currentIndex = -1
    private var mapKeys = emptyList<String>()

    private val currentKey: String?
        get() {
            if (currentIndex < 0) return null
            return mapKeys[currentIndex]
        }

    private fun getMapKey(descriptor: SerialDescriptor): String {
        return descriptor.getElementName(currentIndex)
    }

    private fun getCurrentKey(descriptor: SerialDescriptor): T? {
        if (currentIndex < 0) return null
        return inputMap?.get(getMapKey(descriptor))
    }

    // If inputMap is null, then it was not a map and thus is a primitive
    private val currentValue: T
        get() {
            return inputMap?.get(currentKey) ?: input
        }

    private val nestedDecoders = mutableMapOf<T, AbstractOpDecoder<T>>()

    //private var shortCircuitKey = false

    override fun decodeSequentially(): Boolean {
        return false
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        val mapKeyNames = (0..<descriptor.elementsCount).map { descriptor.getElementName(it) }
        println("MAPKEYNAMES: $mapKeyNames")
        mapKeys = mapKeyNames
        println("Beginning Structure: $descriptor (${descriptor.kind}) with start key: ${getCurrentKey(descriptor)} - $currentIndex")
        // Root decoder will have no tag name
        return this
//        val nestedDecoder = pickDecoder(descriptor, ops, currentValue)
//        println("Was not root decoder, using based on ${descriptor.kind}")
//        nestedDecoders[currentKey] = nestedDecoder
//        return nestedDecoder
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        currentIndex += 1
        if (currentIndex >= mapKeys.size) {
            return CompositeDecoder.DECODE_DONE
        }
        val descName = descriptor.getElementName(currentIndex)
        println("STRT EL DEC: $currentIndex ${mapKeys.size} - $descName ||| ${inputMap?.entries()?.toList()?.size}")
        return currentIndex
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
        return decodeFunc { ops.getStringValue(currentValue) }
    }

    override fun decodeInt(): Int {
        return decodeFunc { ops.getNumberValue(currentValue) }.toInt()
    }

    override fun decodeBoolean(): Boolean {
        return decodeFunc { ops.getBooleanValue(currentValue) }
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

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        return enumDescriptor.getElementIndex(decodeString())
    }

    override fun <V> decodeFunc(func: () -> DataResult<V>): V {
        println("Decoding $currentIndex - $currentKey - $currentValue")
        val dataResult = func()
        return dataResult.orThrow
    }

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        println("Decoding element: $descriptor $index")
        return super.decodeInlineElement(descriptor, index)
    }

}