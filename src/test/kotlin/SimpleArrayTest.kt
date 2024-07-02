import com.mojang.serialization.JsonOps
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class SimpleArrayTest {

    val jimothy = TestData.Person("Jimothy", 36)
    val alice = TestData.Person("Alice", 32)
    val ops = JsonOps.INSTANCE

    // Array consisting of primitives
    @Test fun testEncodeSimpleArray() {
        val result = ops.serialize(listOf(jimothy, alice))
        expectThat(result.toString()) {
            isEqualTo("""
                [{"name":"Jimothy","age":36},{"name":"Alice","age":32}]
            """.trimIndent())
        }
    }

    // Array consisting of arrays of primitives
    @Test fun testEncode2dArray() {
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


}