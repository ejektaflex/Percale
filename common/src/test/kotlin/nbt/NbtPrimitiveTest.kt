package nbt

import io.ejekta.percale.deserialize
import io.ejekta.percale.serialize
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.DoubleTag
import net.minecraft.nbt.FloatTag
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.LongTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

class NbtPrimitiveTest {

    val ops = NbtOps.INSTANCE

    // Array consisting of primitives
    @Test
    fun testEncodeNbtPrimitives() {
        expectThat(ops.serialize(1)).isEqualTo(IntTag.valueOf(1))
        expectThat(ops.serialize(1L)).isEqualTo(LongTag.valueOf(1L))
        expectThat(ops.serialize(1.1f)).isEqualTo(FloatTag.valueOf(1.1f))
        expectThat(ops.serialize(2.2)).isEqualTo(DoubleTag.valueOf(2.2))
        expectThat(ops.serialize(true)).isEqualTo(ByteTag.valueOf(true))
        expectThat(ops.serialize("str")).isEqualTo(StringTag.valueOf("str"))
    }

    @Test
    fun testDecodePrimitives() {
        expectThat(ops.deserialize<Tag, Int>(IntTag.valueOf(1))).isEqualTo(1)
        expectThat(ops.deserialize<Tag, Long>(LongTag.valueOf(1L))).isEqualTo(1L)
        expectThat(ops.deserialize<Tag, Float>(FloatTag.valueOf(1.1f))).isEqualTo(1.1f)
        expectThat(ops.deserialize<Tag, Double>(DoubleTag.valueOf(2.2))).isEqualTo(2.2)
        expectThat(ops.deserialize<Tag, Boolean>(ByteTag.valueOf(true))).isTrue()
        expectThat(ops.deserialize<Tag, String>(StringTag.valueOf("str"))).isEqualTo("str")
    }

    @Test
    fun testEncodeEnum() {
        expectThat(ops.serialize(TestData.DogBreed.POMERANIAN)).isEqualTo(StringTag.valueOf("POMERANIAN"))
    }

    @Test
    fun testDecodeEnum() {
        expectThat(ops.deserialize<Tag, TestData.DogBreed>(StringTag.valueOf("POMERANIAN"))).isEqualTo(TestData.DogBreed.POMERANIAN)
    }

}