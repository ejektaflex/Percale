package decoder

import com.mojang.serialization.DynamicOps
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

@OptIn(ExperimentalSerializationApi::class)
class DynamicObjectDecoder<T>(override val ops: DynamicOps<T>, private val input: T) : AbstractOpDecoder<T>(ops) {

    override val serializersModule = EmptySerializersModule()

    // To handle current tag (field name) context
    private var currentTag: String = ""

    private val mapBuilder = mutableMapOf<String, T>()
    private val nestedDecoders = mutableMapOf<String, AbstractOpDecoder<T>>()

    private var shortCircuitKey = false

    override fun decodeSequentially(): Boolean {
        return true
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        println("Beginning Structure: $descriptor for key: $currentTag - (${descriptor.kind})")
        // Root encoder will have no tag name
        if (currentTag == "") {
            return this
        }
        val nestedDecoder: AbstractOpDecoder<T> = when (descriptor.kind) {
            is StructureKind.CLASS, is StructureKind.MAP -> DynamicObjectDecoder(ops, input)
            //is StructureKind.LIST -> DynamicListEncoder(ops)
            else -> throw Exception("What encoder do we use for this?: ${descriptor.kind} - $descriptor")
        }
        nestedDecoders[currentTag] = nestedDecoder
        return nestedDecoder
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return CompositeDecoder.UNKNOWN_NAME
    }

    override fun endStructure(descriptor: SerialDescriptor) {
        println("Ending Structure: $descriptor")
        // Merge any nested encoders that were created over the course of this structure into the structure itself
        if (nestedDecoders.isNotEmpty()) {
            for ((neKey, neVal) in nestedDecoders) {
                mapBuilder[neKey] = neVal.getResult()
            }
        }
    }

    override fun decodeString(): String {
        return ops.getStringValue(input).orThrow
    }

    override fun decodeInt(): Int {
        return ops.getNumberValue(input).orThrow.toInt()
    }

    override fun decodeBoolean(): Boolean {
        return ops.getBooleanValue(input).orThrow
    }

    override fun getResult(): T {
        // We only ever encoded a single primitive if this happened
        if (mapBuilder.keys.intersect(setOf("")).size == 1) {
            return mapBuilder[""]!!
        }
        return ops.createMap(mapBuilder.map { entry -> ops.createString(entry.key) to entry.value }.toMap())
    }

    override fun push(result: T) {
        TODO("Not yet implemented")
    }

    override fun decodeFunc(func: () -> T) {
        TODO("Not yet implemented")
    }

    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        println("Decoding element: $descriptor $index")
        return super.decodeInlineElement(descriptor, index)
    }

}