import com.mojang.serialization.JsonOps
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class SimpleObjectTest {

    val jimothy = TestData.Person("Jimothy", 36)
    val alice = TestData.Person("Alice", 32)
    val ops = JsonOps.INSTANCE

    // Objects consisting of primitives
    @Test fun testSimpleObject() {
        val result = ops.serialize(jimothy)
        expectThat(result.toString()) {
            isEqualTo("""
                {"name":"Jimothy","age":36}
            """.trimIndent())
        }
    }

    // Objects consisting of other objects
    @Test fun testPersonGroup() {
        val result = ops.serialize(TestData.PersonGroup(jimothy, alice))
        expectThat(result.toString()) {
            isEqualTo("""
                {"personA":{"name":"Jimothy","age":36},"personB":{"name":"Alice","age":32}}
            """.trimIndent())
        }
    }

}