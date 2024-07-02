import com.mojang.serialization.JsonOps
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class SimpleMixedTest {

    val jimothy = TestData.Person("Jimothy", 36)
    val alice = TestData.Person("Alice", 32)
    val ops = JsonOps.INSTANCE

    // Array consisting of primitives
    @Test fun testEncodeArrayOfMaps() {
        val result = ops.serialize(listOf(
            mapOf("a" to 5, "b" to 10),
            mapOf("c" to 15, "d" to 20),
        ))
        expectThat(result.toString()) {
            isEqualTo("""
                [{"a":5,"b":10},{"c":15,"d":20}]
            """.trimIndent())
        }
    }

    @Test fun testEncodeMapOfArrays() {
        val result = ops.serialize(mapOf(
            "a" to listOf(99, 98, 87),
            "b" to listOf(39, 38, 37),
        ))
        expectThat(result.toString()) {
            isEqualTo("""
                {"a":[99,98,87],"b":[39,38,37]}
            """.trimIndent())
        }
    }

    @Test fun testEncodePair() {
        val result = ops.serialize(33 to 30)
        expectThat(result.toString()) {
            isEqualTo("""
                {"first":33,"second":30}
            """.trimIndent())
        }
    }

}