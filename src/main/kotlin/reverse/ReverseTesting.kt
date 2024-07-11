package reverse

import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import percale.decoder.PassDecoder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

//@OptIn(ExperimentalSerializationApi::class)
//fun <A> Codec<A>.toSerializer(): KSerializer<A> {
//    return object : KSerializer<A> {
//        override val descriptor: SerialDescriptor
//            // TODO not always string lol
//            get() = PrimitiveSerialDescriptor("CustomTest", StructureKind.CLASS)
//
//        override fun deserialize(decoder: Decoder): A {
//            val percaleDecoder = decoder as PassDecoder<*>
//            return this@toSerializer.decode(decoder.ops as DynamicOps<Any>, decoder.currentValue)
//        }
//
//        override fun serialize(encoder: Encoder, value: A) {
//            TODO("Not yet implemented")
//        }
//
//    }
//}