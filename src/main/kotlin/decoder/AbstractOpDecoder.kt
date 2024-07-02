package decoder

import com.mojang.serialization.DynamicOps
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.AbstractEncoder

@OptIn(ExperimentalSerializationApi::class)
abstract class AbstractOpDecoder<T>(open val ops: DynamicOps<T>) : AbstractDecoder() {
    abstract fun decodeFunc(func: () -> T)
    abstract fun push(result: T)
}