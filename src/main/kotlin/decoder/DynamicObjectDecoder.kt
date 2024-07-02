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

    // If inputMap is null, then it was not a map and thus is a primitive
    override val currentValue: T
        get() {
            return inputMap?.get(currentKey) ?: input
        }

    private val nestedDecoders = mutableMapOf<T, AbstractOpDecoder<T>>()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        // Assign keys to iterate over based on descriptor element order
        mapKeys = (0..<descriptor.elementsCount).map { descriptor.getElementName(it) }
        return this
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        currentIndex += 1
        if (currentIndex >= mapKeys.size) {
            return CompositeDecoder.DECODE_DONE
        }
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