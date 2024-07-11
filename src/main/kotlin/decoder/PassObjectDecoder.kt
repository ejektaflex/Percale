package decoder

import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
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
            return inputMap?.get(currentKey)
        }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        debug("Beginning structure ### $descriptor ### - at $currentIndex for kind: ${descriptor.kind} and input $input with els ${descriptor.elementsCount}")

        inputKeys = (0..<descriptor.elementsCount).map { descriptor.getElementName(it) }.toMutableList()

        debug("Input keys for $input are: $inputKeys")

        // Nested decode should be doing a handoff
        if (currentIndex < 0) {
            debug("Handing off to self decoder")
            return this
        }
        // Assign keys to iterate over based on descriptor element order
        debug("will be decoding: '$currentKey' to '$currentValue'")

        throw Exception("No!") // TODO why don't we ever hit this?
        //debug("Doing a decoder pick with value: $currentValue")
        val pickedDecoder = pickDecoder(descriptor, ops, currentValue!!, level)
        //debug("Picked decoder of type ${pickedDecoder::class.simpleName} for structure ${descriptor.kind}")

        return pickedDecoder
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        currentIndex += 1
        //debug("Decoding index $currentIndex")
        return if (currentIndex < descriptor.elementsCount) currentIndex else CompositeDecoder.DECODE_DONE
    }

    override fun <V> decodeFunc(func: () -> DataResult<V>): V {
        val dataResult = func()
        return dataResult.orThrow
    }

    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        return deserializer.deserialize(PassObjectDecoder(ops, currentValue!!, level + 1))
    }

}