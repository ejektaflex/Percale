package io.ejekta.percale.decoder

import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import io.ejekta.percale.Percale
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

class PassMapDecoder<T>(override val ops: DynamicOps<T>, override val input: T, level: Int, serialMod: SerializersModule) : PassDecoder<T>(ops, level, serialMod) {

    private val inputMap =
        ops.getMap(input).result().get()
    // Flatten map into a 1d array
    private val entries = inputMap.entries().map { listOf(it.first, it.second) }.toList().flatten()
    private val keyCount = entries.size
    private var currentIndex = -1

    override val currentValue: T?
        get() {
            if (currentIndex !in entries.indices) return null
            //Percale.syslog(level, "GETTING current value index $currentIndex from $entries")
            return entries[currentIndex]
        }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        Percale.syslog(level, "BEGINNING structure of $input")
        if (currentIndex < 0) {
            return this
        }
        val pickedDecoder = pickDecoder(descriptor, ops, currentValue!!, level, serializersModule)
        return pickedDecoder
    }

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        currentIndex += 1
        Percale.syslog(level, "Decoding element index.. is now $currentIndex")
        return if (currentIndex < keyCount) currentIndex else CompositeDecoder.DECODE_DONE.also { Percale.syslog(level, "(surpassed key count, decoding done)") }
    }

    override fun <V> decodeFunc(func: () -> DataResult<V>): V {
        //debug("Decoding $currentIndex - $currentKey - $currentValue")
        val dataResult = func()
        return dataResult.orThrow
    }

}