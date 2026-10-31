# AoH2 Unofficial Bugfix

[中文说明](README.zh-CN.md) · [Repository](https://github.com/Hiromichi123/AOH2-fixed) · [Releases](https://github.com/Hiromichi123/AOH2-fixed/releases)

A Windows code patch for **the original Age of History II, Steam app 603850**. Not for Definitive Edition (3381680). Current status: **1.0.0 release candidate**; logic and installer tests passed, full in-game and long-campaign validation is pending.

## Fixes and limits

- **Population overflow:** adds exact 64-bit national/alliance totals for primary displays and guards selected legacy integer calculations against negative wraparound. Some old graphs, world/continent totals, history, rankings and percentages still have integer limits. Individual province populations remain `int`.
- **Diplomacy corruption:** rejects NaN/Infinity, corrects country-ID iteration and unbounded relation-list growth. Existing invalid values use a healthy reverse relationship where available, otherwise neutral 0. This is a recovery rule, not an exact reconstruction of lost values.
- **Economy stuck at 99:** prevents the low-economy recovery bonus from becoming a population-dependent negative penalty in normal and spectator turns. Intentional penalties for insufficient investment or goods remain.

## For players

Download the player ZIP from [Releases](https://github.com/Hiromichi123/AOH2-fixed/releases) when a release is available. This repository push alone does not create a downloadable release.

1. Close the game and back up campaign saves.
2. Extract the package and run `Install.cmd`. Select the directory containing `AoC2.exe` if automatic Steam-library detection fails.
3. Start the game normally through Steam or the original shortcut.
4. To remove the patch, close the game and run `Uninstall.cmd` from the package.

Installation only replaces the matching EXE, after backing it up as `AoC2.exe.aoc2bugfix-original.bak`; it does not read or write saves. Corrupted diplomacy repaired during play may subsequently be saved by the game. Removing the EXE patch does not undo saved campaign changes. Steam updates/file verification may overwrite the patch.

Required original `AoC2.exe` SHA-256:

```text
5300d13579f51e8e8dc69f0a24cf6bafe22297754cfebb4701f7a92e6b5ebe66
```

Other executable versions and other code mods are refused. Standalone `AoC2.jar` and `AoC2_Old.exe` are not supported installer inputs. Workshop subscription alone cannot activate this package; installation is manual.

## Build from source

Use Windows, Python **3.9+**, and **JDK 17**. The patch helper is compiled for Java 8. Javassist **3.30.2-GA** is a build dependency: download its JAR from [Maven Central](https://repo.maven.apache.org/maven2/org/javassist/javassist/3.30.2-GA/javassist-3.30.2-GA.jar) and place it at `tools/javassist-3.30.2-GA.jar`. The dependency is not vendored; see the [Javassist project](https://github.com/jboss-javassist/javassist) for its license.

Supply your own unmodified EXE matching the hash above. If you already patched your game, use your original backup as input. From the repository root:

```powershell
$env:AOC2_BUILD_JDK = 'C:\Program Files\Java\jdk-17'
python tools/prepare_original.py 'D:\SteamLibrary\steamapps\common\AoCII\AoC2.exe'
python patch/build.py
python tools/build_launcher.py
python tools/run_tests.py
```

To verify with the game's Java 8 runtime, set `AOC2_TEST_JAVA` to its `jre\bin\java.exe` before running tests. The tests use a test-only CFG initializer to avoid loading graphics/assets, while exercising real patched game methods. They do not simulate a full campaign.

Build a differential player package, then test installation in an isolated fixture directory:

```powershell
python tools/PackageRelease.py
python tools/WriteReleaseDocs.py
powershell -NoProfile -ExecutionPolicy Bypass -File tools/TestRelease.ps1
```

`dist/AoC2-fixed.exe` and `dist/AoC2-Bugfix.jar` are local build outputs. `release/` contains generated differential packages, not complete game binaries. `tools/TestRelease.ps1` writes only its isolated `release/tests/` fixtures; no installed game is changed. Build scripts use the JDK's internal ASM with explicit exports, so the documented JDK version matters.

## Repository contents

| Path | Purpose |
|---|---|
| `patch/src/` | Patch helper and isolated logic tests |
| `patch/build.py` | Compile helpers and replace selected methods |
| `tools/` | Bytecode transforms, packaging and verification |
| `installer/` | Readable PowerShell/CMD installer sources |
| `docs/` | Release status and verification notes |

Original game binaries, game assets, decompiled sources, personal saves, downloaded dependencies and generated outputs are excluded by `.gitignore`. This repository contains patch source and tools; it is **not** a standalone game or a fully reconstructed original-source project.

This is an unofficial project. Changes to existing game code remain dependent on the user's own game installation.
