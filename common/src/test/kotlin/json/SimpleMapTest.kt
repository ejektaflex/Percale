package json

import TestValidation
import ValidationTestList
import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import io.ejekta.percale.reverse.CompoundTagSerializer
import io.ejekta.percale.reverse.TagSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import net.minecraft.nbt.CompoundTag
import org.junit.jupiter.api.Test

class SimpleMapTest : ValidationTestList<JsonElement>() {
    override val ops: JsonOps = JsonOps.INSTANCE

    val jimothy = TestData.Person("Jimothy", 36)
    val alice = TestData.Person("Alice", 32)

    // Objects consisting of primitives
    val simpleMap = TestValidation(
        mapOf(
            "dog" to "Sammy",
            "cat" to "Nancy",
            "emu" to "Jimmy"
        ),
        MapSerializer(String.serializer(), String.serializer()),
        """{"dog":"Sammy","cat":"Nancy","emu":"Jimmy"}"""
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

    val compTest = TestValidation(
        CompoundTag().apply {
            putInt("a", 1)
        },
        CompoundTagSerializer,
        """{"a":1}"""
    )
    @Test fun testEncodeSimpleCompound() { compTest.encode() }
    @Test fun testDecodeSimpleCompound() { compTest.decode() }

}