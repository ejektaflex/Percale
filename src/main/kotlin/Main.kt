import com.mojang.serialization.DynamicOps
import com.mojang.serialization.JsonOps
import io.ejekta.kambrikx.serial.DynamicEncoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement

fun <T> serializeWithDynamicOps(obj: Person, ops: DynamicOps<T>): T? {
    val encoder = DynamicEncoder(ops)
    val json = Json { encodeDefaults = true }
    encoder.encodeSerializableValue(Person.serializer(), obj)
    return encoder.getResult()
}

fun main() {
    val data = Person("Abe", 25, 20.0, JobWork("Salesman"), JobWork("Pottery"))
    val encodedData = serializeWithDynamicOps(data, JsonOps.INSTANCE)
    println("Encoded Data: $encodedData")
}