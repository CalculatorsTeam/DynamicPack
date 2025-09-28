plugins {
    base
    kotlin("jvm") version "2.2.20" apply false

    id("dev.kikugie.stonecutter")

    val modstitchVersion = "0.7.0-unstable"
    id("dev.isxander.modstitch.base") version modstitchVersion apply false
    id("dev.isxander.modstitch.shadow") version modstitchVersion apply false
}
stonecutter active "1.21.8-fabric"

allprojects {
    repositories {
        maven("https://maven.terraformersmc.com/")
        maven("https://maven.isxander.dev/releases")
        maven("https://maven.isxander.dev/snapshots")
        exclusiveContent {
            forRepository { maven("https://thedarkcolour.github.io/KotlinForForge/") }
            filter { includeGroup("thedarkcolour") }
        }
    }
}