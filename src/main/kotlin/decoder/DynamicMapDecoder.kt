package decoder

import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule

@OptIn(ExperimentalSerializationApi::class)
class DynamicMapDecoder<T>(override val ops: DynamicOps<T>, private val input: T) : AbstractOpDecoder<T>(ops) {
    private val entries: List<T> = ops.getMap(input).orThrow.entries().map { listOf(it.first, it.second) }.toList().flatten()

    override val serializersModule = EmptySerializersModule()

    private var currentIndex = -1

    override val currentValue: T
        get() = entries[currentIndex]

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        currentIndex += 1
        println("MAP gotta decode $currentIndex: ${descriptor.getElementDescriptor(currentIndex)}")
        return if (currentIndex < entries.size) currentIndex else CompositeDecoder.DECODE_DONE
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        println("STRUCT IS: $entries")
        return this
    }

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T {
        //println("Decoding a serval: ${descriptor.getElementDescriptor(index)}")
        return super.decodeSerializableElement(descriptor, index, deserializer, previousValue)
    }

    override fun <V> decodeFunc(func: () -> DataResult<V>): V {
        //println("MapDecoding $currentIndex - $currentKey (${currentValue!!::class.java})")
        val dataResult = func()
        return dataResult.orThrow
    }

}