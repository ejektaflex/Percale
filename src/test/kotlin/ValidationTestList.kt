import com.mojang.serialization.DynamicOps

abstract class ValidationTestList<T> {
    abstract val ops: DynamicOps<T>

    fun TestValidation<*>.encode() { encode(ops) }
    fun TestValidation<*>.decode() { decode(ops) }
}