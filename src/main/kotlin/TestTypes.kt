import kotlinx.serialization.Serializable

@Serializable
data class JobWork(val title: String)

@Serializable
data class Person(
    val name: String,
//    val age: Int = 30,
//    val cash: Double,
//    val jobWork: JobWork = JobWork("No Job"),
//    val jobHobby: JobWork = JobWork("No Hobby")
)

@Serializable
data class Vehicle(val passengers: List<Person>)

@Serializable
open class Dog(val name: String, val breed: String)
