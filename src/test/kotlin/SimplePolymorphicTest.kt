import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import io.ejekta.percale.serialize
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.junit.jupiter.api.Test

class SimplePolymorphicTest : ValidationTestList<JsonElement>() {
    override val ops: JsonOps = JsonOps.INSTANCE

    val jimothy = TestData.Person("Jimothy", 36)
    val alice = TestData.Person("Alice", 32)

    @Serializable
    open class PersonActivity(open val name: String)

    @Serializable
    class PersonJob() : PersonActivity("Diver")

    val diver = PersonJob()

    val serMod = SerializersModule {
        polymorphic(PersonActivity::class) {
            subclass(PersonJob::class)
        }
    }

    val json = Json {
        serializersModule = serMod
    }

    @OptIn(ExperimentalSerializationApi::class)
    @Test fun testEncodeSimpleMap() {
        println("Yeet")
        println(serMod.getPolymorphic(PersonActivity::class, diver))
        val enc = JsonOps.INSTANCE.serialize(diver, PolymorphicSerializer(PersonActivity::class), serMod)
        println(enc)
    }


}