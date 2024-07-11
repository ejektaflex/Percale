import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class SimpleListTest {

    val jimothy = TestData.Person("Jimothy", 36)
    val alice = TestData.Person("Alice", 32)
    val ops = JsonOps.INSTANCE

    @Test fun testEncodePrimitiveList() {
        val result = ops.serialize(listOf(33, 32, 31))
        expectThat(result.toString()) {
            isEqualTo("""
                [33,32,31]
            """.trimIndent())
        }
    }

    @Test fun testDecodePrimitiveList() {
        val results = ops.deserialize<JsonElement, List<Int>>(JsonParser.parseString("""
            [33,32,31]
        """.trimIndent()))
        expectThat(results).isEqualTo(listOf(33, 32, 31))
    }

    // Array consisting of primitives
    @Test fun testEncodeListOfObjects() {
        val result = ops.serialize(listOf(jimothy, alice))
        expectThat(result.toString()) {
            isEqualTo("""
                [{"name":"Jimothy","age":36},{"name":"Alice","age":32}]
            """.trimIndent())
        }
    }

    @Test fun testDecodeListOfObjects() {
        val result = ops.deserialize<JsonElement, List<TestData.Person>>(JsonParser.parseString("""
            [{"name":"Jimothy","age":36},{"name":"Alice","age":32}]
        """.trimIndent()))
        expectThat(result).isEqualTo(listOf(jimothy, alice))
    }

    // Array consisting of arrays of primitives
    @Test fun testEncode2dList() {
        val result = ops.serialize(listOf(
            listOf(99, 98, 97),
            listOf(69, 68, 67),
            listOf(39, 38, 37)
        ))
        expectThat(result.toString()) {
            isEqualTo("""
                [[99,98,97],[69,68,67],[39,38,37]]
            """.trimIndent())
        }
    }

    @Test fun testEncodePrimActualArray() {
        val result = ops.serialize(
            intArrayOf(33, 32, 31)
        )
        expectThat(result.toString()) {
            isEqualTo("""
                [33,32,31]
            """.trimIndent())
        }
    }

}