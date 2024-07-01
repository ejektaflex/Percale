import kotlinx.serialization.Serializable

@Serializable
data class JobWork(val title: String)

@Serializable
data class Person(
    val name: String,
    val age: Int = 30,
    val cash: Double,
    val jobWork: JobWork = JobWork("No Job"),
    val jobHobby: JobWork = JobWork("No Hobby")
)

@Serializable
data class Vehicle(val tirePressures: List<Double>, val usage: JobWork)
