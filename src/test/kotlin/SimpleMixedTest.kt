import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.PairSerializer
import kotlinx.serialization.builtins.serializer
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@OptIn(ExperimentalSerializationApi::class)
class SimpleMixedTest : ValidationTestList<JsonElement>() {
    override val ops: JsonOps = JsonOps.INSTANCE

    val jimothy = TestData.Person("Jimothy", 36)
    val alice = TestData.Person("Alice", 32)


    // Array consisting of primitives
    val arrayOfMaps = TestValidation(
        arrayOf(
            mapOf("a" to 5, "b" to 10),
            mapOf("c" to 15, "d" to 20),
        ),
        ArraySerializer(MapSerializer(String.serializer(), Int.serializer())),
        """[{"a":5,"b":10},{"c":15,"d":20}]"""
    )
    @Test fun testEncodeArrayOfMaps() { arrayOfMaps.encode() }
    @Test fun testDecodeArrayOfMaps() { arrayOfMaps.decode() }


    // Map consisting of array of primitives
    val mapOfArrays = TestValidation(
        mapOf(
            "a" to arrayOf(99, 98, 87),
            "b" to arrayOf(39, 38, 37),
        ),
        MapSerializer(String.serializer(), ArraySerializer(Int.serializer())),
        """{"a":[99,98,87],"b":[39,38,37]}"""
    )
    @Test fun testEncodeMapOfArrays() { mapOfArrays.encode() }
    @Test fun testDecodeMapOfArrays() { arrayOfMaps.decode() }


    // A pair of primitives
    val pairObject = TestValidation(
        33 to 30,
        PairSerializer(Int.serializer(), Int.serializer()),
        """{"first":33,"second":30}"""
    )
    @Test fun testEncodePair() { pairObject.encode() }
    @Test fun testDecodePair() { pairObject.decode() }

}