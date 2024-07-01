import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import io.ejekta.kambrikx.serial.DynamicObjectEncoder
import kotlinx.serialization.KSerializer

fun <T, U : Any> serializeWithDynamicOps(obj: U, serializer: KSerializer<U>, ops: DynamicOps<T>): T? {
    val encoder = DynamicObjectEncoder(ops)
    encoder.encodeSerializableValue(serializer, obj)
    return encoder.getResult()
}

fun main() {
    //val data = Person("Abe", 25, 20.0, JobWork("Salesman"), JobWork("Pottery"))
    val data = Vehicle(listOf("a", "b", "c"), Person(
        "Bob", 32, 20.0, JobWork("Salesman"), JobWork("Potterman")
    ))
    val encodedData = serializeWithDynamicOps(data, Vehicle.serializer(), JsonOps.INSTANCE)
    println("Encoded Data: $encodedData")
}