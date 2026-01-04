# Magic Egg

A Fabric mod for Minecraft 1.21.1 that adds a Magic Egg - a throwable item that converts mobs into their spawn eggs.

![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-green)
![Fabric](https://img.shields.io/badge/Mod%20Loader-Fabric-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

## Features

- **Magic Egg**: A throwable projectile that instantly converts entities into spawn eggs
  - Throw it at any mob to remove them and drop their spawn egg
  - Works on any entity that has a spawn egg in vanilla Minecraft
  - Does NOT work on players (for obvious reasons)
  - Stacks up to 16

## Crafting Recipe

Surround an egg with alternating lapis lazuli and ender pearls:

```
L E L
E G E
L E L
```

| Symbol | Item |
|--------|------|
| L | Lapis Lazuli |
| E | Ender Pearl |
| G | Egg |

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.21.1
2. Download and install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download the latest release of Magic Egg
4. Place the jar file in your `mods` folder

## Requirements

- Minecraft 1.21.1
- Fabric Loader 0.18.0+
- Fabric API

## Building from Source

```bash
git clone https://github.com/user/magic-egg.git
cd magic-egg
./gradlew build
```

The built jar will be in `build/libs/`.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
