# Compatibility matrix

Stable Minecraft Java releases checked against the official Mojang manifest and public Spigot/Paper API metadata on 2026-09-13.

These rows describe the pinned API build/test coverage. They do not claim all historical server binaries have been tested in-game.

| Minecraft | Bukkit/Spigot API build | Dedicated Paper API build | JAR Java target |
| --- | --- | --- | --- |
| 1.11 | Yes | No published target in this matrix | 8 |
| 1.11.1 | Yes | No published target in this matrix | 8 |
| 1.11.2 | Yes | No published target in this matrix | 8 |
| 1.12 | Yes | No published target in this matrix | 8 |
| 1.12.1 | Yes | No published target in this matrix | 8 |
| 1.12.2 | Yes | No published target in this matrix | 8 |
| 1.13 | Yes | No published target in this matrix | 8 |
| 1.13.1 | Yes | No published target in this matrix | 8 |
| 1.13.2 | Yes | No published target in this matrix | 8 |
| 1.14 | Yes | No published target in this matrix | 8 |
| 1.14.1 | Yes | No published target in this matrix | 8 |
| 1.14.2 | Yes | No published target in this matrix | 8 |
| 1.14.3 | Yes | No published target in this matrix | 8 |
| 1.14.4 | Yes | No published target in this matrix | 8 |
| 1.15 | Yes | No published target in this matrix | 8 |
| 1.15.1 | Yes | No published target in this matrix | 8 |
| 1.15.2 | Yes | No published target in this matrix | 8 |
| 1.16.1 | Yes | No published target in this matrix | 8 |
| 1.16.2 | Yes | No published target in this matrix | 8 |
| 1.16.3 | Yes | No published target in this matrix | 8 |
| 1.16.4 | Yes | No published target in this matrix | 8 |
| 1.16.5 | Yes | Yes | 8 |
| 1.17 | Yes | Yes | 16 |
| 1.17.1 | Yes | Yes | 16 |
| 1.18 | Yes | Yes | 17 |
| 1.18.1 | Yes | Yes | 17 |
| 1.18.2 | Yes | Yes | 17 |
| 1.19 | Yes | Yes | 17 |
| 1.19.1 | Yes | Yes | 17 |
| 1.19.2 | Yes | Yes | 17 |
| 1.19.3 | Yes | Yes | 17 |
| 1.19.4 | Yes | Yes | 17 |
| 1.20 | Yes | Yes | 17 |
| 1.20.1 | Yes | Yes | 17 |
| 1.20.2 | Yes | Yes | 17 |
| 1.20.3 | Yes | Yes | 17 |
| 1.20.4 | Yes | Yes | 17 |
| 1.20.5 | Yes | Yes | 21 |
| 1.20.6 | Yes | Yes | 21 |
| 1.21 | Yes | Yes | 21 |
| 1.21.1 | Yes | Yes | 21 |
| 1.21.2 | Yes | No published target in this matrix | 21 |
| 1.21.3 | Yes | Yes | 21 |
| 1.21.4 | Yes | Yes | 21 |
| 1.21.5 | Yes | Yes | 21 |
| 1.21.6 | Yes | Yes | 21 |
| 1.21.7 | Yes | Yes | 21 |
| 1.21.8 | Yes | Yes | 21 |
| 1.21.9 | Yes | Yes | 21 |
| 1.21.10 | Yes | Yes | 21 |
| 1.21.11 | Yes | Yes | 21 |
| 26.1 | Yes | No published target in this matrix | 25 |
| 26.1.1 | Yes | No published target in this matrix | 25 |
| 26.1.2 | Yes | Yes | 25 |
| 26.2 | Yes | Yes | 25 |

## Gaps and exclusions

- Minecraft 1.16: no 1.16-R0.1 API in the official Spigot index. Minecraft 1.16.1 and later have their own targets.
- Before Minecraft 1.11: shulker boxes do not exist.
- Paper API coverage is only listed where a stable-version API is publicly resolvable. The Bukkit artifact is not independently certified for Paper versions missing a dedicated target.
- Snapshots, pre-releases, release candidates, Bedrock, unmodified Mojang servers, and Folia are excluded.

## Java

The table lists the plugin class-file target, not a promise that any server version can run on every newer Java release. Use the Java runtime supported by the server. The build itself uses JDK 25 for all targets.

## Evidence

- Exact dependency coordinates and plugin API declarations: release-targets.json.
- Per-artifact compilation/test counts and SHA-256: BUILD_REPORT.json in the release.
- SHA256SUMS.txt verifies all distributed artifacts.
- Source lists: [Mojang release manifest](https://piston-meta.mojang.com/mc/game/version_manifest_v2.json), [Spigot API versions](https://hub.spigotmc.org/nexus/content/repositories/snapshots/org/spigotmc/spigot-api/maven-metadata.xml), [Paper API versions](https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/maven-metadata.xml).
