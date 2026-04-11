import kotlinx.serialization.Serializable

object TestData {
    @Serializable
    data class Person(val name: String, val age: Int)

    @Serializable
    data class PersonGroup(val personA: Person, val personB: Person)

    @Serializable
    data class Vehicle(val frontSeat: PersonGroup, val rearSeat: PersonGroup)

    @Serializable
    data class NullablePerson(val name: String?, val age: Int?)

    @Serializable
    data class NullableContainer(
        val primary: NullablePerson?,
        val aliases: List<String?>,
        val scores: Map<String, Int?>
    )

    enum class DogBreed {
        POMERANIAN,
        HUSKY
    }
}
