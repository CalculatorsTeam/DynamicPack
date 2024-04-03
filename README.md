# DynamicPack
A mod that will monitor the current version of your resource pack and download automatically!

![https://img.shields.io/badge/Enviroment-Client-purple](https://img.shields.io/badge/Enviroment-Client-purple)  
[![Static Badge](https://img.shields.io/badge/Github-gray?logo=github)
](https://github.com/AdamCalculator/DynamicPack)

## Documentation
[**Available here**](https://github.com/AdamCalculator/DynamicPack/wiki)

## Packs using it mod
* [CursedEveriday](https://github.com/AdamCalculator/CursedEveryday) by Adam - pack from the creator, on it you can learn how it works
* [better-moss](https://modrinth.com/resourcepack/better-moss)
* [bettertables](https://modrinth.com/resourcepack/bettertables)

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
      "game_version": "1.20.1", // game version
      "project_id": "better-leaves", // your project identifier
      "type": "modrinth"
    },
    "formatVersion": 1
}
```
**⚠️⚠️⚠️ JSON Specification not support //comments! Delete it before use.**

You can validate your JSON [here](https://jsonformatter.curiousconcept.com/#)


For other features (dynamic repos) visit [github wiki!](https://github.com/AdamCalculator/DynamicPack/wiki)


## Thanks
* [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template)
* [Bouncy Castle](https://github.com/bcgit/bc-java)
