import com.mojang.serialization.DynamicOps
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

abstract class ValidationTestList<T> {
    abstract val ops: DynamicOps<T>

    fun TestValidation<*>.encode(serialMod: SerializersModule = EmptySerializersModule()) {
        encode(ops, serialMod)
    }

    fun TestValidation<*>.decode(serialMod: SerializersModule = EmptySerializersModule()) {
        decode(ops, serialMod)
    }

}