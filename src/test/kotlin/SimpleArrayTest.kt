import com.mojang.serialization.JsonOps
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class SimpleArrayTest {

    val jimothy = TestData.Person("Jimothy", 36)
    val alice = TestData.Person("Alice", 32)
    val ops = JsonOps.INSTANCE

    // Objects consisting of primitives
    @Test fun testSimpleArray() {
        val result = ops.serialize(listOf(jimothy, alice))
        expectThat(result.toString()) {
            isEqualTo("""
                [{"name":"Jimothy","age":36},{"name":"alice","age":32}]
            """.trimIndent())
        }
    }


}