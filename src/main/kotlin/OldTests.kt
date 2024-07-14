import com.google.gson.JsonParser
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.Encoder
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import kotlinx.serialization.*
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import java.util.stream.Stream
import kotlinx.serialization.encoding.Encoder as KEncoder
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import percale.encoder.PassEncoder
import reverse.codec
import reverse.toGenericSer
import reverse.toKotlinJsonSerializer
import com.google.gson.JsonElement as GsonElement

data class MyPerson(val name: String, val age: Int)

val MyPersonCodec : Codec<MyPerson> = RecordCodecBuilder.create { instance ->
    instance.group(
        Codec.STRING.fieldOf("name").forGetter { it.name },
        Codec.INT.fieldOf("age").forGetter { it.age }
    ).apply(instance, ::MyPerson)
}

@Serializable
data class MyParty(val size: Int, val organizer: @Contextual MyPerson)



fun main() {

    val d = MyPersonCodec.toGenericSer(RegistryOps.of(JsonOps.INSTANCE, server.registryManager))

    val ourJson = Json {
        serializersModule = SerializersModule {
            contextual(MyPersonCodec.toKotlinJsonSerializer())
            codec(MyPersonCodec)
        }
        prettyPrint = true
    }

    // a Codec is a set of instructions explaining how to encode or decode an object
    // as such, it's the same as a Kotlin KSerializer

    // KSerializer is our version of Codec
    val ser = MyPersonCodec.toKotlinJsonSerializer(ourJson)

    // Abstract[En/De]coder is our version of DynamicOps
    // Json is our version of JsonOps
    val result = ourJson.encodeToString(ser, MyPerson("Jimothy", 36))

    println(result)



//    val party = MyParty(33, MyPerson("Jimothy", 36))
//
//    val partyEncoded = ourJson.encodeToJsonElement(MyParty.serializer(), party)
//
//    println(partyEncoded)


}


