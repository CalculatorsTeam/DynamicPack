# Kotlin Modstitch Stonecutter Template

This repository provides a **multi-loader Minecraft mod development template written in Kotlin**, designed for use with the **Modstitch** and **Stonecutter** Gradle plugins. It supports **Fabric**, **Forge**, and **NeoForge** targets across multiple Minecraft versions

> 🛠️ **Based on**: [isXander/modstitch-stonecutter-template](https://github.com/isXander/modstitch-stonecutter-template) — then modified to ensure it builds and runs successfully

## 🚀 Features

- **Multi-Loader Support**: build for Fabric, Forge, and NeoForge from one codebase
- **Multi-Version Support**: 1.20.1 and 1.21.1 (easily extensible)
- **Kotlin-Ready**: JVM toolchain, compiler settings, and Kotlin integrations are fully preconfigured
- **CI-Ready**: GitHub Actions workflow for multiplatform builds
- **Stonecutter Integration**: powerful Gradle preprocessor and environment-specific configuration via properties
- **Modstitch-Powered**: leverages Modstitch to abstract loader-specific tasks
- **Mixin Support**: set up mixins with proper configuration

```
ExampleMod/
├── .github/workflows/build.yml    # CI/CD configuration
├── build.gradle.kts               # Main build configuration
├── stonecutter.gradle.kts         # Multi-version build setup
├── settings.gradle.kts            # Gradle settings
├── gradle.properties              # Base project properties
├── src/main/
│   ├── kotlin/                    # Kotlin source code
│   │   └── com/author/examplemod/
│   │       ├── ExampleMod.kt      # Main mod class
│   │       └── platform/          # Platform-specific code
│   ├── java/                      # Java source (for Mixins)
│   │   └── com/author/examplemod/mixins/
│   ├── resources/                 # Resources and configuration
│   └── templates/                 # Platform-specific templates
└── versions/                      # Version-specific configurations
    ├── 1.20.1-fabric/
    ├── 1.20.1-forge/
    ├── 1.21.1-fabric/
    └── 1.21.1-neoforge/
```

## ⚙️ Quick Start

### Setup

1. **Use this template** or clone the repository:
   ```bash
   git clone https://github.com/Riflusso/modstitch-kotlin-stonecutter-template.git
   cd modstitch-kotlin-stonecutter-template
   ```

2. **Configure your mod** by editing `gradle.properties`:
   ```properties
   modId=your_mod_id
   modName=Your Mod Name
   modDescription=Your mod description
   modAuthor=Your Name
   modGroup=com.yourname
   ```

3. **Update package structure**:
    - Rename packages from `com.author.examplemod` to match your `modGroup`
    - Update imports and references accordingly

4. **Test the setup** by running any version:
   ```bash
   # You can run any version regardless of which is active:
   ./gradlew :1.21.1-fabric:runClient
   ./gradlew :1.20.1-forge:runClient
   ./gradlew :1.21.1-neoforge:runClient
   ```
5. **Build project** for every version:
    ```bash
    # Build all versions
    ./gradlew build
   ```
   Artifacts will appear under `versions/[version]/build/libs/`

## 📋 Customization Guide

### Adding New Minecraft Versions

1. **Update `settings.gradle.kts`**:
   ```kotlin
   mc("1.22.0", loaders = listOf("fabric", "neoforge"))
   ```

2. **Create version directory**:
   ```
   # Version folders example
   versions/1.22.0-fabric/gradle.properties
   versions/1.22.0-neoforge/gradle.properties
   ```

3. **Configure dependencies** in the new `gradle.properties`

### Adding New Features

1. **Main logic**: Add to `ExampleMod.kt`
2. **Platform/Version-specific code**: Use conditional compilation as in `PlatformEntrypoint.kt`
3. **Mixins**: Add to `src/main/java/.../mixins/`
4. **Resources**: Add to `src/main/resources/`

## 📚 Resources

- [Fabric Documentation](https://fabricmc.net/wiki/)
- [NeoForge Documentation](https://docs.neoforged.net/)
- [Forge Documentation](https://docs.minecraftforge.net/)
- [Stonecutter Documentation](https://stonecutter.kikugie.dev/)
- [Modstitch Documentation](https://isXander.github.io/modstitch-docs/)
- [KotlinForForge](https://github.com/TheDarkColour/KotlinForForge)
- [Fabric Language Kotlin](https://github.com/FabricMC/fabric-language-kotlin)