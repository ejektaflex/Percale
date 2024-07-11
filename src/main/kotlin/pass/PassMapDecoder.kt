package pass

import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlin.jvm.optionals.getOrNull

@OptIn(ExperimentalSerializationApi::class)
class PassMapDecoder<T>(override val ops: DynamicOps<T>, private val input: T, level: Int) : PassDecoder<T>(ops, level) {

    init {
        debug("CREATED OBJ DECODER WITH IN: $input")
    }

    override val serializersModule = EmptySerializersModule()

    private val inputMap = ops.getMap(input).result().getOrNull()
    private var currentIndex = 0
    private var mapKeys = emptyList<String>()
    private val inputEntries = inputMap?.entries()?.toList() ?: emptyList()

    val inputSize: Int by lazy {
        inputEntries.size
    }

    private val currentKey: T?
        get() {
            if (currentIndex < 0) return null
            return inputEntries[currentIndex].first
        }

    // If inputMap is null, then it was not a map and thus is a primitive
    override val currentValue: T?
        get() {
            return inputMap?.get(currentKey)
        }

    private val nestedDecoders = mutableMapOf<Int, PassDecoder<T>>()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        // Assign keys to iterate over based on descriptor element order
        debug("Beginning structure $descriptor -  at $currentIndex")
        debug("will be decoding: '$currentKey' to '$currentValue'; MapKeys: $mapKeys")
        // Nested decode should be doing a handoff
        if (currentIndex < 0) {
            mapKeys = (0..<descriptor.elementsCount).map { descriptor.getElementName(it) }
            return this
        }
        debug("Doing a decoder pick")
        val pickedDecoder = pickDecoder(descriptor, ops, currentValue!!, level)
        debug("Picked decoder of type ${pickedDecoder::class.simpleName} for structure ${descriptor.kind}")

        //pickedDecoder.decodeElementIndex(descriptor.getElementDescriptor(currentIndex))

        nestedDecoders[currentIndex] = pickedDecoder
        return pickedDecoder
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        currentIndex += 1
        if (currentIndex >= inputSize) {
            debug("Calling for decode finish!!!!! $descriptor after $currentIndex for input $input")
            return CompositeDecoder.DECODE_DONE
        }
        return currentIndex
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        println("Ending Obj Structure: $descriptor with input: $input")
    }



    override fun <V> decodeFunc(func: () -> DataResult<V>): V {
        debug("OBJ $input is Decoding $currentIndex - $currentKey - $currentValue")
        val dataResult = func()
        return dataResult.orThrow
    }

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        println("Decoding element: $descriptor $index")
        return super.decodeInlineElement(descriptor, index)
    }

}