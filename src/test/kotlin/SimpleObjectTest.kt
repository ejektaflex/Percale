import com.mojang.serialization.JsonOps
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class SimpleObjectTest {

    val jimothy = TestData.Person("Jimothy", 36)
    val alice = TestData.Person("Alice", 32)
    val ops = JsonOps.INSTANCE

    // Objects consisting of primitives
    @Test fun testEncodeSimpleObject() {
        val result = ops.serialize(jimothy)
        expectThat(result.toString()) {
            isEqualTo("""
                {"name":"Jimothy","age":36}
            """.trimIndent())
        }
    }

    // Just a simple map object
    @Test fun testEncodeSimpleMap() {
        val result = ops.serialize(mapOf(
            "dog" to "Sammy",
            "cat" to "Nancy",
            "emu" to "Jimmy"
        ))
        expectThat(result.toString()) {
            isEqualTo("""
                {"dog":"Sammy","cat":"Nancy","emu":"Jimmy"}
            """.trimIndent())
        }
    }

    // Objects consisting of other objects
    @Test fun testEncodePersonGroup() {
        val result = ops.serialize(TestData.PersonGroup(jimothy, alice))
        expectThat(result.toString()) {
            isEqualTo("""
                {"personA":{"name":"Jimothy","age":36},"personB":{"name":"Alice","age":32}}
            """.trimIndent())
        }
    }

    // Anonymous object serialization
    @Test fun testEncodeAnonObject() {
        val result = ops.serialize(Dog("Sammy", "Spitz"))
        expectThat(result.toString()) {
            isEqualTo("""
                {"name":"Sammy","breed":"Spitz"}
            """.trimIndent())
        }
    }

}