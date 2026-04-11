plugins {
    id("multiloader-common-module")
}

neoForge {
    addModdingDependenciesTo(sourceSets.test.get())
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("io.strikt:strikt-core:0.34.0")
    testImplementation("com.mojang:datafixerupper:8.0.16")
}
