import kotlinx.serialization.Serializable

object TestData {
    @Serializable
    data class Person(val name: String, val age: Int)

    @Serializable
    data class PersonGroup(val personA: Person, val personB: Person)

    enum class DogBreed {
        POMERANIAN,
        HUSKY
    }

}