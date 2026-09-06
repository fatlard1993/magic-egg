# Magic Egg - Development Guide

For what the mod is and how it plays, see [README.md](README.md).

## Installation

Install server-side alongside its declared dependencies (see `fabric.mod.json`); connecting clients need only Pandorical. Version targets live in `gradle.properties` (Minecraft, loader, Fabric API) and `fabric.mod.json` (Java).

## Building from Source

Magic Egg builds against Pandorical's live source, not a published artifact: `settings.gradle` includes `../pandorical`. Clone both side by side or the build fails before it starts.

```bash
git clone https://github.com/fatlard1993/pandorical.git
git clone https://github.com/fatlard1993/magic-egg.git
cd magic-egg
./gradlew build
```

The built jar will be in `build/libs/`.
