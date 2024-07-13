
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

## Generating Codecs

Since a Codec is just a pair of methods for encoding and decoding, we can automatically generate a Codec from a KSerializer:

```kotlin
@Serializable
data class MyPerson(val name: String, val age: Int, val jobStatus: JobStatus)

val PERSON_CODEC: Codec<MyPerson> = MyPerson.serializer().toCodec()
```

## Generating Serializers (Experimental)

Percale also has the (very experimental) ability to generate a KotlinX Serializer from a Mojang Codec, effectively doing the reverse. There is one major limitation, and that is that it only works for JSON/JsonOps and no other formats.

Normally, this would *not* be feasible because Mojang Codecs do not store any equivalent of Kotlin's serial descriptor information, making static analysis and encoding/decoding of objects difficult (especially when you consider JVM type erasure). However, if we rely on JsonOps to do the encoding and decoding for us, then pass that to KotlinX Serialization to re-encode/decode the output. 

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

// Register the component when Minecraft loads..
Registry.register(Registries.DATA_COMPONENT_TYPE, Identifier.of("mymod", "bonus_damage"), BONUS_DAMAGE)

// Later, lets say you have an item...
val sword = ItemStack(Items.IRON_SWORD)

// You can set the data on it like this:
sword[BONUS_DAMAGE] = BonusDamage(5f)

// Or retrieve it, like this!:
val bonusDamage = sword[BONUS_DAMAGE]
```


