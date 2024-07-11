import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import org.junit.jupiter.api.Test

class SimpleObjectTest : ValidationTestList<JsonElement>() {
    override val ops: JsonOps = JsonOps.INSTANCE

    val jimothy = TestData.Person("Jimothy", 36)
    val alice = TestData.Person("Alice", 32)
    val group = TestData.PersonGroup(jimothy, alice)

    // Objects consisting of primitives
    val simpleObject = TestValidation(jimothy, TestData.Person.serializer(), """{"name":"Jimothy","age":36}""")
    @Test fun testEncodeSimpleObject() { simpleObject.encode() }
    @Test fun testDecodeSimpleObject() { simpleObject.decode() }

    // Objects with reversed keys
    val simpleObjectReversed = TestValidation(jimothy, TestData.Person.serializer(), """{"age":36,"name":"Jimothy"}""")
    @Test fun testDecodeSimpleObjectReversed() { simpleObjectReversed.decode() }

    // Objects consisting of other objects
    val personGroup = TestValidation(group, TestData.PersonGroup.serializer(), """
            {"personA":{"name":"Jimothy","age":36},"personB":{"name":"Alice","age":32}}
        """.trimIndent())
    @Test fun testEncodePersonGroup() { personGroup.encode() }
    @Test fun testDecodePersonGroup() { personGroup.decode() }

}