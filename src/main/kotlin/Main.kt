import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import io.ejekta.kambrikx.serial.DynamicObjectEncoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

fun <T, U : Any> serializeWithDynamicOps(obj: U, serializer: KSerializer<U>, ops: DynamicOps<T>): T? {
    val encoder = DynamicObjectEncoder(ops)
    val json = Json { encodeDefaults = true }
    encoder.encodeSerializableValue(serializer, obj)
    return encoder.getResult()
}

fun main() {
    //val data = Person("Abe", 25, 20.0, JobWork("Salesman"), JobWork("Pottery"))
    val data = Vehicle(listOf(1.0, 2.0, 3.0, 4.0), JobWork("Delivery"))
    val encodedData = serializeWithDynamicOps(data, Vehicle.serializer(), JsonOps.INSTANCE)
    println("Encoded Data: $encodedData")
}