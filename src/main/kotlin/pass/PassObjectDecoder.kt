package pass

import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlin.jvm.optionals.getOrNull

@OptIn(ExperimentalSerializationApi::class)
class PassObjectDecoder<T>(override val ops: DynamicOps<T>, private val input: T, level: Int) : PassDecoder<T>(ops, level) {

    init {
        debug("CREATED OBJ DECODER WITH IN: $input")
    }

    override val serializersModule = EmptySerializersModule()

    private var inputMap =
        ops.getMap(input).result().getOrNull()
    private var inputKeys = mutableListOf<String>()
    private var currentIndex = -1
    private var currentElements = emptyList<String>()

    private val currentKey: String?
        get() {
            // If no input keys, is not a map and is just primitive input
            return if (inputKeys.isEmpty()) {
                null
            } else {
                inputKeys[currentIndex]
            }
        }

    override val currentValue: T?
        get() {
            debug("I: $input, M: $inputMap K: $currentKey, E: $currentElements, V: ${inputMap?.get(currentKey)}")
            return inputMap?.get(currentKey)
        }

    private val nestedDecoders = mutableMapOf<Int, PassDecoder<T>>()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        debug("Beginning structure $descriptor -  at $currentIndex for kind: ${descriptor.kind} and input $input with els ${descriptor.elementsCount}")

        inputKeys = (0..<descriptor.elementsCount).map { descriptor.getElementName(it) }.toMutableList()

        // Nested decode should be doing a handoff
        if (currentIndex < 0) {
            debug("Handing off to nested decoder")
            return this
        }
        // Assign keys to iterate over based on descriptor element order
        debug("will be decoding: '$currentKey' to '$currentValue'")

        debug("Doing a decoder pick")
        val pickedDecoder = pickDecoder(descriptor, ops, currentValue!!, level)
        debug("Picked decoder of type ${pickedDecoder::class.simpleName} for structure ${descriptor.kind}")

        //pickedDecoder.decodeElementIndex(descriptor.getElementDescriptor(currentIndex))

        nestedDecoders[currentIndex] = pickedDecoder
        return pickedDecoder
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        // TODO inputKeys should be ordered by descriptors


        debug("Indexes: ${(0..<descriptor.elementsCount).map { descriptor.getElementDescriptor(it) }}")
        currentIndex += 1
        debug("Decoding index $currentIndex")
        return if (currentIndex < descriptor.elementsCount) currentIndex else CompositeDecoder.DECODE_DONE
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        debug("Ending Obj Structure: $descriptor with input: $input")
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

    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        debug("WHOAD $deserializer")
        return super.decodeSerializableValue(deserializer)
    }

}