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
    val data = mutableListOf(
        listOf(1, 2, 3),
        listOf(4, 5, 6),
        listOf(7, 8, 9)
    )
    val data2 = mutableListOf(
        1, 2, 3
    )
    val encodedData = JsonOps.INSTANCE.serialize(data)
    println("Encoded Data: $encodedData")
}