package pass

import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlin.jvm.optionals.getOrNull

@OptIn(ExperimentalSerializationApi::class)
class PassPrimitiveDecoder<T>(override val ops: DynamicOps<T>, private val input: T, level: Int) : PassDecoder<T>(ops, level) {

    init {
        debug("CREATED PRIM DECODER WITH IN: $input")
    }

    override val serializersModule = EmptySerializersModule()

    override val currentValue: T?
        get() = input

    private val nestedDecoders = mutableMapOf<Int, PassDecoder<T>>()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        throw SerializationException("Cannot do a structure on a primitive!")
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        throw SerializationException("Cannot decode index on a primitive!")
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        debug("Ending Obj Structure: $descriptor with input: $input")
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        return enumDescriptor.getElementIndex(decodeString())
    }

    override fun <V> decodeFunc(func: () -> DataResult<V>): V {
        val dataResult = func()
        return dataResult.orThrow
    }

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        println("Decoding element: $descriptor $index")
        return super.decodeInlineElement(descriptor, index)
    }

    override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        debug("WHOAD $deserializer")
        return deserializer.deserialize(PassPrimitiveDecoder(ops, currentValue!!, level + 1))
    }

}