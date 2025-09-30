# DynamicPack
A mod that will monitor the current version of your resource pack and download automatically!

![https://img.shields.io/badge/Enviroment-Client-purple](https://img.shields.io/badge/Enviroment-Client-purple)  
[![Static Badge](https://img.shields.io/badge/Github-gray?logo=github)
](https://github.com/CalculatorsTeam/DynamicPack)

[![Java CI with Gradle](https://github.com/CalculatorsTeamAdamCalculator/DynamicPack/actions/workflows/gradle.yml/badge.svg)](https://github.com/AdamCalculator/DynamicPack/actions/workflows/gradle.yml)

[![Banner](https://api.mcbanners.com/banner/saved/wveMrFfPsqwXbV.png)](https://modrinth.com/mod/dynamicpack)

## Documentation
[**Available here**](https://github.com/CalculatorsTeam/DynamicPack/wiki)

## Packs using it mod
* [Better Tables](https://modrinth.com/resourcepack/bettertables)
* [CursedEveryday (github)](https://github.com/AdamCalculator/CursedEveryday/releases/tag/day4)
* [ModsRU](https://modrinth.com/resourcepack/mods-ru)
* [zelda-music](https://modrinth.com/resourcepack/zelda-music)
* [PawTotems (smp) (github)](https://github.com/1NFERR/PawTotems/)
* [SPPack (smp) (github)](https://github.com/aladairmaxwell/SP)
* [Essentially Tweaked](https://modrinth.com/resourcepack/essentially-tweaked)
* [Vanilla Leaves](https://modrinth.com/resourcepack/vanilla-leaves)

## Plans
The mod is planned to support **any bootloader** and any version, but I don't have enough time right now. **I'm always happy to see new contributors!**

## How it works
Resource pack developers need to create a `dynamicmcpack.json` file inside the resource pack, which will save some information, and the mod will update when the game starts if resource pack files are required.

## For users
Install and it will automatically update supported resource packs.

⚠️ Since the mod is being actively developed, it is not yet possible to check resource packs for updates, but this will be added in the future.


## For developers
If you want your package to update itself from **Modrinth**, you need to add the `dynamicmcpack.json` file to the following content:
```json5
{
    "current": {
      "version_number": "7.1" // version of the current pack
    },
    "remote": {
      "game_version": "1.21.1", // game version
      "modrinth_project_id": "better-leaves", // your project identifier
      "type": "modrinth"
    },
    "formatVersion": 1
}
```
**⚠️⚠️⚠️ JSON Specification not support //comments! Delete it before use.**

You can validate your JSON [here](https://jsonformatter.curiousconcept.com/#)


For other features (dynamic repos) visit [github wiki!](https://github.com/CalculatorsTeam/DynamicPack/wiki)


## Donate
The project is completely free and open under the MIT licence. You can help original author by donating ❤️

**Bitcoin:** `bc1qpc0q9ym7rnfatdh43c4jyf68znj8x2jae5j4cz`
