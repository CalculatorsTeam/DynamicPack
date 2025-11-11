# 🌍 DynamicPack
*A lightweight mod that monitors your resource pack version and updates it automatically.*

<p align="center">
  <img src="https://img.shields.io/badge/Enviroment-Client-purple" alt="Environment: Client">
  <a href="https://github.com/CalculatorsTeam/DynamicPack">
    <img src="https://img.shields.io/badge/Github-gray?logo=github" alt="GitHub Repository">
  </a>
  <a href="https://github.com/CalculatorsTeam/DynamicPack/actions/workflows/build.yml">
    <img src="https://github.com/CalculatorsTeam/DynamicPack/actions/workflows/build.yml/badge.svg" alt="Build Status">
  </a>
</p>

<p align="center">
  <a href="https://modrinth.com/mod/dynamicpack">
    <img src="https://github.com/RushanM/Minecraft-Mods-Russian-Translation/blob/beta/Ассеты/dynamicpack_cozy_vector.svg?raw=true" width="250" alt="DynamicPack Banner">
  </a>
</p>

---

## 📖 Documentation
See the [**GitHub Wiki**](https://github.com/CalculatorsTeam/DynamicPack/wiki) for full developer documentation, advanced features, and examples.

---

## 🎨 Resource Packs Using DynamicPack
These resource packs already use automatic DynamicPack updates:

- [CursedEveryday (GitHub)](https://github.com/AdamCalculator/CursedEveryday)
- [ModsRU](https://modrinth.com/resourcepack/mods-ru)
- [Zelda Music](https://modrinth.com/resourcepack/zelda-music)
- [PawTotems (SMP, GitHub)](https://github.com/1NFERR/PawTotems/)
- [SPPack (SMP, GitHub)](https://github.com/aladairmaxwell/SP)
- [Essentially Tweaked](https://modrinth.com/resourcepack/essentially-tweaked)
- [Vanilla Leaves](https://modrinth.com/resourcepack/vanilla-leaves)

---

## ⚙️ How It Works
Resource pack developers include a metadata file named `dynamicmcpack.json` inside their pack.  
At game launch, DynamicPack checks for updates and automatically downloads the latest version if necessary.

---

## 🧩 For Players
Install the mod and enjoy — DynamicPack will handle updates in the background.

---

## 🛠 For Developers
If you’d like your pack to **update itself automatically**, you can choose one of the integration options below.

<details>
<summary>🔗 <b>Option 1 — Update from Modrinth</b></summary>

Create a file named `dynamicmcpack.json` in your pack:

```json5
{
  "current": {
    "version_number": "7.1"                                   // Current version of your resource pack
  },
  "remote": {
    "game_version": "1.21.1",                                 // Compatible Minecraft version
    "modrinth_project_id": "better-leaves",                   // Your Modrinth project slug or ID
    "type": "modrinth"                                        // Update source type (Modrinth)
  },
  "formatVersion": 1                                          // Internal config format version
}
```

</details>

<details>
<summary>🐙 <b>Option 2 — Update from GitHub</b></summary>

Create a file named `dynamicmcpack.json` in your pack:

```json5
{
  "current": {},
  "remote": {
    "sign_no_required": true,                                 // Optional digital signature — set to true to disable verification
    "type": "dynamic_repo",                                   // Update source type (Dynamic Repository)
    "url": "https://adamcalculator.github.io/CursedEveryday/" // Remote repository URL
  },
  "formatVersion": 1                                          // Internal configuration format version
}
```

Next, create a `dynamicmcpack.repo.json` file like this:

```json5
{
  "formatVersion": 1,                                         // Internal schema version
  "build": 10,                                                // Build number of your current release
  "name": "CursedEveryday",                                   // Display name of your resource pack
  "contents": [
    {
      // Metadata block (main information about pack components)
      "url": "path/to/content.json",
      "hash": "5e4d4ad1e9714487263c51f5f83c448c0708773a",     // SHA1 checksum of content.json
      "id": "meta",
      "hidden": true,                                         // Hides this component from user‑facing lists
      "required": true                                        // Marks this component as mandatory
    },
    {
      // Example of an additional content section
      "url": "path/to/content.json",
      "hash": "6e6739297dac80078bbc4890567f05d4015553db",
      "id": "cursed",
      "name": "Cursed content"                                // Optional friendly display name
    }
  ]
}
```

Next, create a separate `content.json` file for each content section:

```json5
{
  "formatVersion": 1,                                         // Schema version
  "content": {
    "parent": "path/to/parent",                               // Local folder containing the files
    "remote_parent": "remote/path/to/parent",                 // Corresponding path in your Git repo
    "files": {
      "path/to/file": {
        "hash": "ca55daeef2e2d84ccd64608cf889ac321c18d4c2",   // SHA1 hash of file
        "size": 4773                                          // File size in bytes
      },
      "path/to/file2": {
        "hash": "336e9f4e6d7d1400d8a308a33e703e8b33ea5434",
        "size": 54665
      },
      "path/to/file3": {
        "hash": "09dae0777de1705e8acd0ff84c0bb6b7e3e22f63",
        "size": 98592
      }
    }
  }
}
```

</details>

> ⚠️ **Important:** These examples use `json5`, which allows comments (`//`).  
> The actual files must be valid `.json`, so remove comments before use or validate them with [this JSON checker](https://jsonformatter.curiousconcept.com/#).

Advanced features — such as **dynamic repositories** — are documented in the [**GitHub Wiki**](https://github.com/CalculatorsTeam/DynamicPack/wiki).

---

## 💖 Support the Project
DynamicPack is open‑source and completely free under the **MIT License**.  
If you’d like to support the original author:

**Bitcoin:**  
`bc1qpc0q9ym7rnfatdh43c4jyf68znj8x2jae5j4cz`

Every satoshi helps keep things dynamic (and maybe buys some extra coffee ☕).
