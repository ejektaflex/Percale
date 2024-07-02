package decoder

import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule

class DynamicMapDecoder<T>(override val ops: DynamicOps<T>, private val input: T) : AbstractOpDecoder<T>(ops) {
    private val entries: List<T> = ops.getMap(input).orThrow.entries().map { listOf(it.first, it.second) }.toList().flatten()

    override val serializersModule = EmptySerializersModule()

    private var currentIndex = -1

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        currentIndex += 1
        return if (currentIndex < entries.size) currentIndex else CompositeDecoder.DECODE_DONE
    }

    override fun decodeString(): String {
        return ops.getStringValue(entries[currentIndex]).orThrow
    }

    override fun decodeInt(): Int {
        return ops.getNumberValue(entries[currentIndex]).orThrow.toInt()
    }

    override fun decodeNotNullMark(): Boolean {
        return true
    }

    override fun decodeNull(): Nothing? {
        return null
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return this
    }

    override fun <V> decodeFunc(func: () -> DataResult<V>): V {
        //println("MapDecoding $currentIndex - $currentKey (${currentValue!!::class.java})")
        val dataResult = func()
        return dataResult.orThrow
    }

}