
# Percale

Percale is a library that can be used to convert Kotlin objects to other formats, as defined by implementations of DynamicOps. This works by working as a wrapper around a DynamicOps object and making encode/decode calls.

Example:

```kotlin
@Serializable
data class MyPerson(val name: String, val age: Int, val jobStatus: JobStatus)
@Serializable
data class JobStatus(val isWorking: Boolean)

val john = MyPerson("John", 35, JobStatus(false))

// Encoding a Kotlin object to JSON
val encodedDataJson = JsonOps.INSTANCE.serialize(john) 
//=> {"name":"John","age":35,"jobStatus":{"isWorking":false}}

// Encoding a Kotlin object to NBT
val encodedDataNbt = NbtOps.INSTANCE.serialize(john) 
//=> {age:35,jobStatus:{isWorking:0b},name:"John"}


// Decoding a JsonElement or an NbtElement back to a Kotlin object
val decodedDataJson = JsonOps.INSTANCE.deserialize<JsonElement, MyPerson>(encodedDataJson!!)
val decodedDataNbt = NbtOps.INSTANCE.deserialize<NbtElement, MyPerson>(encodedDataNbt!!)
//=> MyPerson(name=John, age=35, jobStatus=JobStatus(isWorking=false))
```

As you can see, we can use this wrapper to encode and decode data to any provided DynamicOps format.

## Generating Codecs from KSerializers

Since a Codec is just a pair of methods for encoding and decoding, we can automatically generate a Codec from a KSerializer:

```kotlin
@Serializable
data class MyPerson(val name: String, val age: Int, val jobStatus: JobStatus)

val PERSON_CODEC: Codec<MyPerson> = MyPerson.serializer().toCodec()
```

## Generating Serializers from Codecs (Limited, Experimental)

Percale also has the (very experimental) ability to generate a KotlinX Serializer from a Mojang Codec, effectively doing the reverse. There is one major limitation, and that is that it only works for JSON/JsonOps and no other formats.

Normally, this would *not* be feasible because Mojang Codecs do not store any equivalent of Kotlin's serial descriptor information, making static analysis and encoding/decoding of objects difficult (especially when you consider JVM type erasure). However, we can rely on JsonOps to do the encoding and decoding for us, then pass that to KotlinX Serialization to re-encode/decode the output. 

This means that, effectively, we can create a KSerializer from a Codec, as long as we are using KSX Json and JsonOps:

```kotlin

// Let's say that we have a data class (note: No @Serializable annotation!)
data class MyPerson(val name: String, val age: Int)

// And a manually created codec (or a codec provided by, say, Mojang)
val MyPersonCodec : Codec<MyPerson> = RecordCodecBuilder.create { instance ->
    instance.group(
        Codec.STRING.fieldOf("name").forGetter { it.name },
        Codec.INT.fieldOf("age").forGetter { it.age }
    ).apply(instance, ::MyPerson)
}

// Generating the KSerializer from a Codec
val personSerializer = MyPersonCodec.toKotlinJsonSerializer()

// Convert a MyPerson to JSON (using the generated serializer)
val result = Json.encodeToJsonElement(ser, MyPerson("Jimothy", 36))
//=> {"name":"Jimothy","age":36}
```

## Minecraft

Since DynamicOps are most popularly used in Minecraft, it makes sense to show how Percale can be utilized for Minecraft mods.

### Components

Since we can automatically generate a Codec for any basic Kotlin object, it's fairly trivial to create Components in Minecraft without the extra baggage. For example:

```kotlin
// Create a new component record
@Serializable @JvmRecord
data class BonusDamage(val amount: Float)

// Create a component type from that component
val BONUS_DAMAGE = ComponentType.builder<BonusDamage>().codec(
    BonusDamage.serializer().toCodec()
).build()

// Register the component when Minecraft loads
Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of("mymod", "bonus_damage"), BONUS_DAMAGE)

// Later, lets say you have an item...
val sword = ItemStack(Items.IRON_SWORD)

// You can set the data on it like this:
sword[BONUS_DAMAGE] = BonusDamage(5f)

// Or retrieve it, like this!:
val bonusDamage = sword[BONUS_DAMAGE]
```

We can also easily serialize components to JSON for various uses, even if they don't have an associated KSerializer:

```kotlin
val itemRarity = stack[DataComponentTypes.RARITY]
val raritySerializer = DataComponentTypes.RARITY.codec!!.toKotlinJsonSerializer()

val encodedRarity = Json.encodeToString(raritySerializer, itemRarity)
//=> "common"

```


### Writing Minecraft classes to config files

One limitation of using KotlinX Serialization within Minecraft is that there's no way of writing Minecraft classes (like ItemStack, BlockPos, Vec3, etc) easily to JSON, which means that you can't use these classes directly in datapacks or config files without writing your own custom KSerializer for each and every class. However, it's possible using the (experimental) Codec to Serializer feature, as long as a codec exists for that class.

```kotlin
val pos = BlockPos(33, 32, 31)

// Convert the codec into a serializer
val posSerializer = BlockPos.CODEC.toKotlinJsonSerializer()

val encoded = Json.encodeToJsonElement(posSerializer, pos)
//=> [33,32,31]
```

But what if we are serializing an object that *contains* a Minecraft class? Then you'll have to add the codec as a contextual serializer. Here's an example:

```kotlin
// A data class containing a Minecraft BlockPos; External classes must be marked as Contextual
data class Treasure(val amount: Int, val location: @Contextual BlockPos)

val ourJson = Json {
    serializersModule = SerializersModule {
        // A nice shorthand that registers the codec as a contextual serializer
        codec(BlockPos.CODEC)
    }
}

val encoded = ourJson.encodeToJsonElement(
    Treasure(1000, BlockPos(123, 64, 96))
)
// => {"amount":1000,"location":[123,64,96]}

```
### Serializing Registry-Sensitive Minecraft classes

The above method for serializing Minecraft objects usually works great. However, some codecs will fail, since they can't access the registry. Notably, The Minecraft Enchantments component will fail to serialize and deserialize because the DynamicOps it uses needs access to a registry. For example:

```kotlin
val enchantsType = DataComponentsType.ENCHANTMENTS
val enchantsCodec = enchantsType.codec!!

// Grab the enchantments from an itemstack
val enchants = stack[enchantsType]!! // we know this item has enchantments
// Grab the enchantments codec
val enchantsSerializer = enchantsCodec.toKotlinJsonSerializer()

// This code will fail, because the Enchantments component requires a registry wrapper context:
val enchantsJson = Json.encodeToString(enchantsSerializer, enchants)
//=> ERROR!
```

To get around this, we can wrap the ops in a RegistryOps and use that, like so:

```kotlin
// Wrap the JsonOps in a RegistryOps that has a registry context
val regOps = RegistryOps.of(JsonOps.INSTANCE, server.registryManager)
// Create a new KSerializer that uses the wrapped DynamicOps
val enchantsSerializer = enchantsCodec.toWrappedJsonSerializer(regOps)

// Now you can freely use the Serializer to encode/decode Registry-sensitive classes!
val enchantsJson = Json.encodeToString(enchantsSerializer, stack[enchantsType]!!)
//=> {"levels":{"minecraft:sharpness":5}}
```


