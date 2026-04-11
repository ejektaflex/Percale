package io.ejekta.percale.decoder

import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import io.ejekta.percale.Percale
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.SerializersModule
import kotlin.jvm.optionals.getOrNull
import kotlin.streams.asSequence

@OptIn(ExperimentalSerializationApi::class)
class PassObjectDecoder<T>(override val ops: DynamicOps<T>, override val input: T, level: Int, serialMod: SerializersModule) : PassDecoder<T>(ops, level, serialMod) {

    private var inputMap =
        ops.getMap(input).result().getOrNull()
    private var inputKeys = mutableListOf<String>()
    private var currentIndex = -1

//    init {
//        Percale.syslog(level, "Init obj decoder for obj: $input")
//        Percale.syslog(level, "Created input map: $inputMap")
//    }

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
            val key = currentKey ?: return null
            val tokenValue = inputMap?.get(ops.createString(key))
            val stringValue = inputMap?.get(key)
            return tokenValue ?: stringValue ?: if (key in inputKeys) ops.empty() else null
        }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        val presentKeys = ops.getMapValues(input).result().getOrNull()
            ?.asSequence()
            ?.mapNotNull { entry -> ops.getStringValue(entry.first).result().getOrNull() }
            ?.toSet()
            .orEmpty()
        inputKeys = (0..<descriptor.elementsCount)
            .map { descriptor.getElementName(it) }
            .filter { it in presentKeys }
            .toMutableList()
        Percale.syslog(level, "Input keys are: $inputKeys")
        return this // maybe only if currentIndex < 0 ?
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        currentIndex += 1
        if (currentIndex >= inputKeys.size) {
            Percale.syslog(level, "Decode done! Input keys were: $inputKeys and we just hit index $currentIndex")
            return CompositeDecoder.DECODE_DONE
        }

        Percale.syslog(level, "Curr key for decode: $currentKey ($currentIndex, $inputKeys)")
        val descIndex = descriptor.getElementIndex(currentKey!!)
        Percale.syslog(level, "Desc index is $descIndex which corresponds with ${descriptor.getElementName(descIndex)}")

        return descIndex
    }

    override fun <V> decodeFunc(func: () -> DataResult<V>): V {
        val dataResult = func()
        return dataResult.orThrow
    }
}
