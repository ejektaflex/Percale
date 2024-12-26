plugins {
    // see https://fabricmc.net/develop/ for new versions
    id("fabric-loom") version "1.9-SNAPSHOT" apply false
    // see https://projects.neoforged.net/neoforged/moddevgradle for new versions
    id("net.neoforged.moddev") version "0.1.110" apply false
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.serialization") version "2.0.21"
}

repositories {
    mavenCentral()
}


subprojects {
    plugins.apply("org.jetbrains.kotlin.jvm")
    plugins.apply("org.jetbrains.kotlin.plugin.serialization")

    dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    }

    // Loader specific
    if (path != ":common") {
        tasks.withType<JavaCompile> {
            source(project(":common").sourceSets.main.get().allSource)
        }

        // For now, just skip javadoc
        tasks.withType<Javadoc>().all {
            enabled = false
        }

        // We also have no Java test files to compile
        tasks.named("compileTestJava") {
            enabled = false
        }

        tasks.withType<ProcessResources> {
            from(project(":common").sourceSets.main.get().resources)
        }
    }
}