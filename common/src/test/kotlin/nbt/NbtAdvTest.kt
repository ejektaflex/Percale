package nbt

import TestValidation
import ValidationTestList
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import io.ejekta.percale.reverse.GsonElementSerializer
import io.ejekta.percale.reverse.GsonIntSerializer
import io.ejekta.percale.reverse.GsonObjectSerializer
import io.ejekta.percale.reverse.toSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.util.ExtraCodecs
import org.junit.jupiter.api.Test

class NbtAdvTest : ValidationTestList<Tag>() {
    override val ops: NbtOps = NbtOps.INSTANCE

    val jimothy = TestData.Person("Jimothy", 36)
    val alice = TestData.Person("Alice", 32)

    // Objects consisting of primitives
    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    val simpleMap = TestValidation(
        JsonObject().apply {
            addProperty("a", 1)
        },
        ExtraCodecs.JSON.toSerializer(kotlinx.serialization.json.JsonElement.serializer()),
        """{a:1b}"""
    )
    @Test fun testEncodeSimpleMap() { simpleMap.encode() }
    @Test fun testDecodeSimpleMap() { simpleMap.decode() }

    // A 2d map of primitives
    val twoDimPrimMap = TestValidation(
        mapOf(
            "hello" to mapOf(
                "a" to 1,
                "b" to 2,
            ),
            "goodbye" to mapOf(
                "c" to 3,
                "d" to 4
            )
        ),
        MapSerializer(String.serializer(), MapSerializer(String.serializer(), Int.serializer())),
        """{"hello":{"a":1,"b":2},"goodbye":{"c":3,"d":4}}"""
    )
    @Test fun testEncode2dMap() { twoDimPrimMap.encode() }
    @Test fun testDecode2dMap() { twoDimPrimMap.decode() }

}