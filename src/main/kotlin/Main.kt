import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import io.ejekta.kambrikx.serial.DynamicArrayEncoder
import io.ejekta.kambrikx.serial.DynamicObjectEncoder
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.serializer

@OptIn(ExperimentalSerializationApi::class)
fun <T, U : Any> serializeWithDynamicOps(serializer: KSerializer<U>, obj: U, ops: DynamicOps<T>): T? {
    println("Picking kind: ${serializer.descriptor.kind}")
    val encoder = when (serializer.descriptor.kind) {
        StructureKind.LIST -> DynamicArrayEncoder(ops)
        else -> DynamicObjectEncoder(ops)
    }
    encoder.encodeSerializableValue(serializer, obj)
    return encoder.getResult()
}

inline fun <T, reified U : Any> DynamicOps<T>.serialize(obj: U): T? {
    return serializeWithDynamicOps(serializer<U>(), obj, this)
}

fun main() {
    val result = JsonOps.INSTANCE.serialize(mapOf(
        "dog" to "Sammy",
        "cat" to "Nancy"
    ))
    println("Encoded Data: $result")
}