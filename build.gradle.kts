import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Locale

plugins {
    id("dev.kikugie.stonecutter")
    id("dev.isxander.modstitch.base")
    id("dev.isxander.modstitch.publishing")
    kotlin("jvm")
    `maven-publish`
}

// ========== Versions & Project Info ==========
val mcVersion: String by project
val versionWithoutMC = property("modVersion")!!.toString()

val isDev = resolveProp("isDev")
val isAlpha = "-a" in versionWithoutMC
val isBeta = "-b" in versionWithoutMC

val isFabric = modstitch.isLoom
val isNeoforge = modstitch.isModDevGradleRegular
val isForge = modstitch.isModDevGradleLegacy
val isForgeLike = modstitch.isModDevGradle

val loader = when {
    isFabric -> "fabric"
    isNeoforge -> "neoforge"
    isForge -> "forge"
    else -> error("Unknown loader")
}

val javaTargetVersion = if (stonecutter.eval(mcVersion, ">1.20.4")) 21 else 17
val resolvedModId = resolveProp("modId") ?: error("modId is required")
val gitHash = gitHash()

// ========== ModStitch Setup ==========
modstitch {
    minecraftVersion = mcVersion
    javaVersion = javaTargetVersion

    if (!isForge) {
        unitTesting()
    }

    kotlin {
        jvmToolchain(javaVersion.get())
        compilerOptions.jvmTarget.set(JvmTarget.fromTarget(javaVersion.get().toString()))
    }

    parchment {
        mappingsVersion = resolveProp("parchment.version")
        minecraftVersion = resolveProp("parchment.minecraft")
    }

    metadata {
        modId = resolvedModId
        modName = resolveProp("modName")
        modVersion = "$versionWithoutMC${if (isAlpha || isBeta) "." else "+"}${stonecutter.current.project}${if (isDev?.lowercase(Locale.getDefault()) == "true" || isDev == "1") "-DEV-$gitHash" else ""}"
        modGroup = resolveProp("modGroup")
        modDescription = resolveProp("modDescription")
        modLicense = resolveProp("modLicense")
        modAuthor = resolveProp("modAuthor")

        replacementProperties.put(
            "pack_format", when (mcVersion) {
                "1.19.2" -> 9
                "1.20.1" -> 15
                "1.21.1" -> 34
                "1.21.3" -> 57
                "1.21.4" -> 61
                "1.21.5" -> 71
                "1.21.8" -> 81
                else -> throw IllegalArgumentException("Please store the resource pack version for $mcVersion in build.gradle.kts! https://minecraft.wiki/w/Pack_format")
            }.toString()
        )

        replacementProperties.put("gitHead", gitHash)

        // replacementProperties DSL
        fun setReplace(key: String, property: String) {
            resolveProp(property)?.let { replacementProperties.put(key, it) }
        }

        setReplace("github", "githubProject")
        setReplace("mc", "meta.mcDep")
        setReplace("fabricLoader", "deps.fabricLoader")
        setReplace("fabricLangKotlin", "deps.fabricLangKotlin")
        setReplace("fapi", "deps.fabricApi")
    }

    loom {
        resolveProp("deps.fabricLoader")?.let { fabricLoaderVersion = it }
    }

    moddevgradle {
        resolveProp("deps.neoforge")?.let { neoForgeVersion = it }
        resolveProp("deps.forge")?.let { forgeVersion = it }
        defaultRuns()

        configureNeoForge {
            runs.all {
                jvmArguments.add("-Dmixin.debug.export=true")
            }
        }

        modstitch.onEnable {
            tasks.named("createMinecraftArtifacts") {
                dependsOn("stonecutterGenerate")
            }
        }
    }

    mixin {
        addMixinsToModManifest = true
        configs.register(resolvedModId)
    }
}

// ========== Stonecutter ==========
stonecutter {
    constants {
        put("fabric", isFabric)
        put("neoforge", isNeoforge)
        put("forge", isForge)
        put("forgelike", isForgeLike)
    }

    dependencies {
        put("fapi", resolveProp("deps.fabricApi") ?: "0.0.0")
    }
}

// ========== Dependencies ==========
dependencies {
    if (isFabric) {
        modstitchModImplementation("net.fabricmc.fabric-api:fabric-api:${resolveProp("deps.fabricApi")}")
        modstitchModImplementation("net.fabricmc:fabric-language-kotlin:${resolveProp("deps.fabricLangKotlin")}")

        modstitchModImplementation("com.terraformersmc:modmenu:${resolveProp("deps.modmenu")}")
    }

    if (isNeoforge) {
        modstitchModRuntimeOnly("thedarkcolour:kotlinforforge-neoforge:${resolveProp("deps.kotlinForForge")}")
    }

    if (isForge) {
        modstitchModRuntimeOnly("thedarkcolour:kotlinforforge:${resolveProp("deps.kotlinForForge")}")
        compileOnly("org.jetbrains:annotations:20.1.0")
    }

    modstitchModApi("dev.isxander:yet-another-config-lib:${resolveProp("deps.yacl")}") {
        exclude(group = "thedarkcolour")
    }
}

msPublishing {
    maven {
        repositories {
            mavenLocal()
        }
    }

    fun versionList(prop: String) = findProperty(prop)?.toString()
        ?.split(',')
        ?.map { it.trim() }
        ?: emptyList()

    val stableMCVersions = versionList("pub.stableMC")

    mpp {
        displayName.set("$versionWithoutMC for $loader $mcVersion")

        changelog.set(
            file("../../CHANGELOG.md")
                .takeIf { it.exists() }
                ?.readText()
                ?: "No changelog provided."
        )

        type = when {
            isAlpha -> ALPHA
            isBeta -> BETA
            else -> STABLE
        }

        val modrinthId: String by project
        if (modrinthId.isNotBlank() && hasProperty("MODRINTH_TOKEN")) {
            modrinth {
                projectId.set(modrinthId)
                accessToken.set(findProperty("MODRINTH_TOKEN")?.toString())
                minecraftVersions.addAll(stableMCVersions)
                minecraftVersions.addAll(versionList("pub.modrinthMC"))

                if (isFabric) {
                    requires { slug.set("fabric-language-kotlin") }
                }
                if (isForgeLike) {
                    requires { slug.set("kotlin-for-forge") }
                }
                optional { slug.set("yacl") }
            }
        }

        val curseforgeId: String by project
        if (curseforgeId.isNotBlank() && hasProperty("CURSEFORGE_TOKEN")) {
            curseforge {
                projectId = curseforgeId
                projectSlug = findProperty("curseforgeSlug")?.toString() ?: error("curseforgeSlug property not found")
                accessToken = findProperty("CURSEFORGE_TOKEN")?.toString()
                minecraftVersions.addAll(stableMCVersions)
                minecraftVersions.addAll(versionList("pub.curseMC"))

                if (isFabric) {
                    requires { slug.set("fabric-language-kotlin") }
                }
                if (isForgeLike) {
                    requires { slug.set("kotlin-for-forge") }
                }
                optional { slug.set("yacl") }
            }
        }

        val active = stonecutter.active?.project
        var current = "$mcVersion-$loader"

        val githubProject: String by project
        if (githubProject.isNotBlank() && hasProperty("GH_TOKEN") && active == current) {
            github {
                repository.set(githubProject)
                accessToken.set(findProperty("GH_TOKEN")?.toString())
                commitish.set(gitBranch())
            }
        }

        if (hasProperty("DISCORD_WEBHOOK") && active == current) {
            discord {
                username.set("DynamicPack Updates")
                webhookUrl.set(findProperty("DISCORD_WEBHOOK")?.toString())
                content.set(changelog)
            }
        }
    }
}

// ========== Tasks ==========
tasks {
    withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget = modstitch.javaVersion.map { JvmTarget.fromTarget(it.toString()) }
        }
        dependsOn("stonecutterGenerate")
    }

    named("generateModMetadata") {
        dependsOn("stonecutterGenerate")
    }

    if (isForge) {
        named<Test>("test") {
            enabled = false
        }
    }
}

// ========== Helpers ==========
fun resolveProp(property: String): String? =
    System.getenv(property)?.takeIf { it.isNotBlank() }
        ?: findProperty(property)?.toString()?.takeIf { it.isNotBlank() }

fun gitHash(): String {
    val process = ProcessBuilder("git", "rev-parse", "--short", "HEAD")
        .redirectErrorStream(true) // combine stdout + stderr
        .start()

    val text = process.inputStream.bufferedReader().use { it.readText() }

    return text.trim()
}

fun gitBranch(): String {
    val process = ProcessBuilder("git", "rev-parse", "--abbrev-ref", "HEAD")
        .redirectErrorStream(true) // объединяем stdout и stderr
        .start()

    val text = process.inputStream.bufferedReader().use { it.readText() }

    return text.trim()
}