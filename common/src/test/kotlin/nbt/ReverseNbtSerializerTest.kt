package nbt

import io.ejekta.percale.deserialize
import io.ejekta.percale.reverse.TagSerializer
import io.ejekta.percale.serialize
import net.minecraft.nbt.ByteArrayTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.DoubleTag
import net.minecraft.nbt.FloatTag
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.LongArrayTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ReverseNbtSerializerTest {

    private val ops = NbtOps.INSTANCE
    private val serializer = TagSerializer

    @Test
    fun testRoundTripFloatTag() {
        val input = FloatTag.valueOf(1.5f)
        expectThat(ops.deserialize(ops.serialize(input, serializer), serializer)).isEqualTo(input)
    }

    @Test
    fun testRoundTripDoubleTag() {
        val input = DoubleTag.valueOf(2.25)
        expectThat(ops.deserialize(ops.serialize(input, serializer), serializer)).isEqualTo(input)
    }

    @Test
    fun testRoundTripByteArrayTag() {
        val input = ByteArrayTag(byteArrayOf(1, 2, 3))
        expectThat(ops.deserialize(ops.serialize(input, serializer), serializer)).isEqualTo(input)
    }

    @Test
    fun testRoundTripLongArrayTag() {
        val input = LongArrayTag(longArrayOf(4L, 5L, 6L))
        expectThat(ops.deserialize(ops.serialize(input, serializer), serializer)).isEqualTo(input)
    }

    @Test
    fun testRoundTripCompoundWithMixedSupportedTags() {
        val input = CompoundTag().apply {
            put("name", StringTag.valueOf("Percale"))
            put("ratio", FloatTag.valueOf(3.5f))
            put("worth", DoubleTag.valueOf(8.75))
            put("flags", ByteArrayTag(byteArrayOf(0, 1, 1)))
            put("points", LongArrayTag(longArrayOf(7L, 8L, 9L)))
            put("count", IntTag.valueOf(4))
        }

        expectThat(ops.deserialize(ops.serialize(input, serializer), serializer)).isEqualTo(input)
    }
}
