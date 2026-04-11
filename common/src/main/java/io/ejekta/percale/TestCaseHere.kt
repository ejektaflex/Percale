package io.ejekta.percale

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import io.ejekta.percale.reverse.toSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import net.minecraft.DetectedVersion
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import net.minecraft.util.ExtraCodecs
import net.minecraft.util.GsonHelper
import java.io.File

fun main() {
    println("Bootstrapping..")
    //SharedConstants.setVersion(DetectedVersion.BUILT_IN)
    //Bootstrap.bootStrap()

    println("Here: ${File("").absolutePath}")

    //val file = File("src/test/resources/test2.json")
    val file = File("common/src/test/resources/test2.json")
    val text = file.readText()
    val ops = JsonOps.INSTANCE

    val gs = GsonHelper.parse(text)

    //val js =

    val serMod = SerializersModule {
//        contextualCodec<JsonObject>(
//            ExtraCodecs.JSON as Codec<JsonObject>
//        )
        //contextual(JsonElement::class, ExtraCodecs.JSON.toSerializer())
        contextual(JsonObject::class, ExtraCodecs.JSON.toSerializer() as KSerializer<JsonObject>)
    }


    val con = PoolEntry.serializer().toCodec(serMod)

    val deser = con.decode(ops, gs)

    println(deser)

    val done = deser.result().get().first

    val reser = con.encodeStart(ops, done)

    println(reser)


}