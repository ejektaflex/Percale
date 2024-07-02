import com.google.gson.JsonElement
import com.google.gson.JsonParser
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

    @Test fun testDecodeSimpleObject() {
        val result = ops.deserialize<JsonElement, TestData.Person>(JsonParser.parseString("""
            {"name":"Jimothy","age":36}
        """.trimIndent()))
        expectThat(result).isEqualTo(jimothy)
    }

    @Test fun testDecodeSimpleObjectReversed() {
        val result = ops.deserialize<JsonElement, TestData.Person>(JsonParser.parseString("""
            {"age":36,"name":"Jimothy"}
        """.trimIndent()))
        expectThat(result).isEqualTo(jimothy)
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

    // A 2d map object of primitives
    @Test fun testEncode2dMap() {
        val result = ops.serialize(mapOf(
            "hello" to mapOf(
                "a" to 1,
                "b" to 2,
            ),
            "goodbye" to mapOf(
                "c" to 3,
                "d" to 4
            )
        ))
        expectThat(result.toString()) {
            isEqualTo("""
                {"hello":{"a":1,"b":2},"goodbye":{"c":3,"d":4}}
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

}