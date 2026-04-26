plugins {
    base
    kotlin("jvm") version "2.3.+" apply false

    id("dev.kikugie.stonecutter")

    val modstitchVersion = "0.8.+"
    id("dev.isxander.modstitch.base") version modstitchVersion apply false
    id("dev.isxander.modstitch.publishing") version modstitchVersion apply false
}
stonecutter active "26.1-fabric"

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