package pass

import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.PrimitiveKind
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

    private val inputMap =
        ops.getMap(input).result().get()
    // Flatten map into a 1d array
    private val entries = inputMap.entries().map { listOf(it.first, it.second) }.toList().flatten()
    private val keyCount = entries.size
    private var currentIndex = -1

    override val currentValue: T?
        get() {
            return entries[currentIndex]
        }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        debug("Beginning structure ### $descriptor ### - at $currentIndex for kind: ${descriptor.kind} and input $input")
        debug("Input keys for $input are: $entries")

        // Nested decode should be doing a handoff
        if (currentIndex < 0) {
            debug("Handing off to self decoder")
            return this
        }
        // Assign keys to iterate over based on descriptor element order
        //debug("will be decoding: '$currentKey' to '$currentValue'")

        throw Exception("No!") // TODO why don't we ever hit this?
        //debug("Doing a decoder pick with value: $currentValue")
        val pickedDecoder = pickDecoder(descriptor, ops, currentValue!!, level)
        //debug("Picked decoder of type ${pickedDecoder::class.simpleName} for structure ${descriptor.kind}")

        return pickedDecoder
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        currentIndex += 1
        return if (currentIndex < keyCount) currentIndex else CompositeDecoder.DECODE_DONE.also { println("(done)") }
    }

    override fun <V> decodeFunc(func: () -> DataResult<V>): V {
        //debug("Decoding $currentIndex - $currentKey - $currentValue")
        val dataResult = func()
        return dataResult.orThrow
    }

//    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
//        debug("DecSerVal ${deserializer.descriptor} - $currentValue")
//        return when (deserializer.descriptor.kind) {
//            is PrimitiveKind -> deserializer.deserialize(PassPrimitiveDecoder(ops, currentValue!!, level + 1))
//            else -> deserializer.deserialize(PassMapDecoder(ops, currentValue!!, level + 1))
//        }
//    }

//    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
//        println("Using: ${deserializer.descriptor}")
//        return deserializer.deserialize(PassObjectDecoder(ops, currentValue!!, level + 1))
//    }

}