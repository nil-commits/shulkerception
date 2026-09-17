# Shulkerception 1.1.0

Author: **nil-commits**
Release date: **2026-09-13**

## Multi-version compatibility release

- 86 version-specific builds across 55 stable Minecraft Java releases, from 1.11 through 26.2.
- 55 Bukkit/Spigot API targets and 31 separately published Paper API targets.
- Java 8 output for legacy releases, with Java 16, 17, 21 and 25 targets for newer server generations.
- Removed references to newer-only Material constants and click actions from legacy code.
- Read nested item-state inventories through the API already present in 1.11.
- Preserved nesting behavior, metadata, configuration and insertion limits.
- Pinned every API dependency and included per-artifact test/checksum reports.
- Paper 1.20.5 uses released Adventure 4.17.0 on its build/test classpath because its original transitive snapshot is no longer fully available from the upstream repositories. No Adventure library is included in the plugin JAR.
- Existing v1.0.0 release remains available.

Choose the artifact matching your exact server version and edition. Install only one Shulkerception JAR and retain your existing config.

## Coverage limits

Minecraft releases before 1.11 have no shulker boxes. The stable Minecraft 1.16 release has no matching public Spigot API in the official index; no 1.16 artifact is represented as verified. Use a supported server release such as 1.16.1 with its own matching artifact.

Some older/intermediate versions have a Bukkit target but no independently published Paper API target. The compatibility table makes this explicit. Snapshots, pre-releases, release candidates, Bedrock, Mojang vanilla servers and Folia are not included.

Each published target must pass API compilation, mocked inventory tests, plugin metadata checks and Java bytecode-level checks. These checks are not a substitute for in-game verification on all historical servers.

## Verification

The **Paper 26.2** build (`shulkerception-1.1.0-mc26.2-paper.jar`) has been verified in-game on a live Paper 26.2 server: nesting, insertion via click / shift-click / number-key / offhand, and persistence across a server restart. All other targets remain compilation- and mock-tested only.
