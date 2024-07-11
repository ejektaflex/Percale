
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


// Decoding a JsonElement back to a Kotlin object
val decodedDataJson = JsonOps.INSTANCE.deserialize<JsonElement, MyPerson>(encodedDataJson!!)
//=> MyPerson(name=John, age=35, jobStatus=JobStatus(isWorking=false))

// Decoding an NbtElement back to a Kotlin object
val decodedDataNbt = NbtOps.INSTANCE.deserialize<NbtElement, MyPerson>(encodedData!!)
//=> MyPerson(name=John, age=35, jobStatus=JobStatus(isWorking=false))
```

As you can see, we can use this wrapper to encode and decode data to any provided DynamicOps format.

## Codecs

Since a Codec is just a pair of methods for encoding and decoding, we can automatically generate a Codec from a KSerializer:

```kotlin
@Serializable
data class MyPerson(val name: String, val age: Int, val jobStatus: JobStatus)

val PERSON_CODEC: Codec<MyPerson> = MyPerson.serializer().toCodec()
```

## Minecraft Components

Since we can automatically generate a Codec for any basic Kotlin object, it's fairly trivial to create Components in Minecraft without the extra baggage. For example:

```kotlin
// Create a new component record
@Serializable @JvmRecord
data class BonusDamage(val amount: Float)

// Create a component type from that component
val BONUS_DAMAGE = ComponentType.builder<BonusDamage>().codec(
    BonusDamage.serializer().toCodec()
).build()

// Register the component when Minecraft loads..
Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of("mymod", "bonus_damage"), BONUS_DAMAGE)


// Later, lets say you have an item...
val sword = ItemStack(Items.IRON_SWORD)

// You can set the data on it like this:
sword.set(BONUS_DAMAGE, BonusDamage(5f))

// Or retrieve it, like this!:
val bonusDamage = sword.get(BONUS_DAMAGE)
```


