package decoder

import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import encoder.AbstractOpEncoder
import encoder.DynamicListEncoder
import encoder.DynamicObjectEncoder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.AbstractEncoder

@OptIn(ExperimentalSerializationApi::class)
abstract class AbstractOpDecoder<T>(open val ops: DynamicOps<T>) : AbstractDecoder() {
    abstract fun <V> decodeFunc(func: () -> DataResult<V>): V

    companion object {
        fun <V> pickDecoder(descriptor: SerialDescriptor, ops: DynamicOps<V>, input: V): AbstractOpDecoder<V> {
            return when (descriptor.kind) {
                StructureKind.CLASS, is PrimitiveKind, SerialKind.ENUM -> DynamicObjectDecoder(ops, input)
                StructureKind.MAP -> DynamicMapDecoder(ops, input)
                //StructureKind.LIST -> DynamicListEncoder(ops)
                else -> throw SerializationException("Unsupported descriptor type for our DynamicOps encoder: ${descriptor.kind}, ${descriptor.kind::class}")
            }
        }
    }
}