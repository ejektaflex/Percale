import com.google.gson.JsonObject
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item

typealias GsonObject = JsonObject

@Serializable
class PoolEntry private constructor() {
    //var type: @Contextual ResourceLocation = ResourceLocation.fromNamespaceAndPath(Bountiful.ID, "null_pool")
    var content = "Nope"
    var name: String? = null
    var icon: @Contextual ResourceLocation? = null
        private set
    var amount = EntryRange(-1, -1)
    var unitWorth = -1000.0
    var weightMult = 1.0
    var timeMult = 1.0
    var repRequired = 0.0
    private val forbids: MutableList<ForbiddenContent> = mutableListOf()

    @Transient lateinit var id: String


    val conditions: @Contextual GsonObject? = null
    var components: @Contextual GsonObject? = null

    var mystery: Boolean = false

    //var nbt: @Contextual CompoundTag? = null

    val worthSteps: List<Double>
        get() = (amount.min..amount.max).map { it * unitWorth }

    val maxWorth: Double
        get() = amount.max * unitWorth


    @Transient
    private var relatedItemCache: List<Item>? = null


    private val worthRange: Pair<Double, Double>
        get() = (amount.min * unitWorth) to (amount.max * unitWorth)


    @Serializable
    class EntryRange(val min: Int, val max: Int) {
        fun pick(): Int = (min..max).random()
        override fun toString() = "[$min - $max]"
    }

    @Serializable
    class ForbiddenContent(val type: @Contextual ResourceLocation, val content: String)

}