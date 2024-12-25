package json

import TestValidation
import ValidationTestList
import com.google.gson.JsonElement
import com.mojang.serialization.JsonOps
import io.ejekta.percale.reverse.PercaleJson
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import strikt.api.expectThat

class NestedObjectTest : ValidationTestList<JsonElement>() {
    override val ops: JsonOps = JsonOps.INSTANCE

    val jimothy = TestData.Person("Jimothy", 36)
    val alice = TestData.Person("Alice", 32)
    val tombolin = TestData.Person("Tombolin", 34)
    val sam = TestData.Person("Sam", 37)

    val groupA = TestData.PersonGroup(jimothy, alice)
    val groupB = TestData.PersonGroup(tombolin, sam)
    val partyBus = TestData.Vehicle(groupA, groupB)


    // Objects consisting of primitives
    val nestedObject = TestValidation(partyBus, TestData.Vehicle.serializer(), """
        {
            "frontSeat": {"personA":{"name":"Jimothy","age":36},"personB":{"name":"Alice","age":32}},
            "rearSeat": {"personA":{"name":"Tombolin","age":34},"personB":{"name":"Sam","age":37}}
        }
    """.trimIndent())
    @Test fun testEncodeNestedObjects() { nestedObject.encode() }
    @Test fun testDecodeNestedObjects() { nestedObject.decode() }

    val testRandom = TestValidation(groupA, TestData.PersonGroup.serializer(), """{"personA":{"name":"Jimothy","age":36},"personB":{"name":"Alice","age":32}}""")

    @Test fun dootTestRandom() {

        val fmt = PercaleJson(ops, Json.Default)

        val decoded = fmt.dynamicDecodeFromString(testRandom.str, testRandom.serial)

        expectThat(decoded == testRandom.obj)
    }


}