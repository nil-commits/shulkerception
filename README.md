# Shulkerception

**Version 1.1.0 · Author: nil-commits**

Licensed under the [MIT License](LICENSE). Copyright (c) 2026 nil-commits.

Put shulker boxes inside placed shulker boxes using ordinary Minecraft Java clients.
Version-specific Bukkit/Spigot and Paper builds cover **55 stable Minecraft versions from 1.11 through 26.2**, with **86 pinned API build targets**. See [COMPATIBILITY.md](COMPATIBILITY.md) for the exact coverage and exceptions.

## Download and install

Get the JAR for your exact Minecraft version and server edition from the [GitHub releases](https://github.com/nil-commits/shulkerception/releases).

For example:

- Paper 26.2: `shulkerception-1.1.0-mc26.2-paper.jar`
- Spigot/CraftBukkit 1.20.4: `shulkerception-1.1.0-mc1.20.4-bukkit.jar`
- Spigot/CraftBukkit 1.12.2: `shulkerception-1.1.0-mc1.12.2-bukkit.jar`

Stop the server, remove the previous Shulkerception JAR, install **one** matching JAR in `plugins/`, and restart. Keep `plugins/Shulkerception/config.yml` when upgrading. Each artifact has the same plugin name and configuration format. The all-versions ZIP contains the complete selection; do not put all its JARs into a server.

Older Paper versions without a separately published Paper API have no independently compiled Paper edition here. A matching Bukkit build may work, but needs runtime verification against that server. Do not substitute a JAR for a different Minecraft version.

## Use

Place and open a shulker box. Click another box into a slot, shift-click it from your inventory, or use a number-key swap. Offhand swaps are supported where the server API exposes that click action.

All shulker colors available in the target version are supported: 16 on the legacy versions, with the undyed box available on newer versions. Inner boxes may contain items or further boxes. Remove and place an inner box to open it.

No commands, permissions, client mods, additional plugins, databases, or server internals are required. The listener modifies the placed inventory and clones complete item stacks. Normal server item persistence handles storage and drops.

## Configuration and scope

The first start creates:

```yaml
max-depth: 5
max-contained-shulkers: 256
```

Depth includes the outer box. Depth 2 permits an outer box with inner boxes that contain no further boxes. The second setting counts every contained shulker and descendant. Supported ranges are 2–16 for depth and 1–4096 for contained boxes. Restart after editing. Limits apply to insertion; removal remains possible.

- Click placement, shift-click insertion and number-key swaps are supported.
- Offhand swapping depends on the click action being present in the target server API.
- Hopper/dropper insertion and dragging keep vanilla restrictions.
- Already-cancelled protection events and spectator actions are skipped.
- Custom portable-shulker interfaces and Folia are outside the supported scope.
- Non-vanilla shulker stacks are split by clicks/shift-clicks; hotbar swaps require one box.
- Limits are not a general validator for arbitrary large item data.

## Build and verify

Use **JDK 25 and Maven 3.9+ to build**. The build emits the appropriate Java bytecode for each target; old servers do not need Java 25 to load their legacy artifact. Run each server on its own supported Java runtime.

`release-targets.json` locks Minecraft version, edition, exact API artifact, Java output level and plugin API declaration. Every target gets isolated compilation and test output.

Paper 1.20.5's published API imports an obsolete Adventure snapshot. Its build uses the archived BOM metadata from Stellardrift and explicitly selects released Adventure 4.17.0 for the compile/test classpath. The release script applies this exception only to that target. Shulkerception does not call or bundle Adventure. See `compatibility/paper-1.20.5-settings.xml` and the matching Maven profile.

```powershell
# Build/test/package all pinned targets.
./release.ps1

# One Minecraft version, both available editions.
./release.ps1 -Minecraft 1.20.4

# One edition, multiple versions.
./release.ps1 -Minecraft 1.12.2,1.16.5 -Edition bukkit

# Continue interrupted work, reusing only matching source and verified artifacts.
./release.ps1 -Resume

# Use already-cached dependencies.
./release.ps1 -Offline -Resume
```

The default Maven commands still build the current 26.2 editions:

```text
mvn -P paper package
mvn -P bukkit package
```

Artifacts are generated under `target/<edition>/<minecraft-version>/` and copied to `dist/v1.1.0/`. The complete package is `dist/shulkerception-1.1.0-all-versions.zip`. SHA256SUMS.txt covers the JARs and documentation. BUILD_REPORT.json records the exact API, test count, bytecode target and checksum for each artifact.

The release script verifies every JAR's embedded version/author, plugin API declaration and class-file Java level. Tests cover each available shulker color, contents/metadata retention, cursor/hotbar swaps, inventory capacity, limits and cancelled events. **Compilation and mocked API tests do not establish in-game compatibility on every historical server.** The release table records API coverage, not runtime certification.

Before using valuable items, check click/shift/hotbar insertion, full inventories, simultaneous players, protection plugins, block breaking/replacement, and persistence through a server restart on the actual server version.

Shulker boxes were introduced in the [Exploration Update](https://www.minecraft.net/de-de/article/block-week--shulker-box); versions before 1.11 cannot support this feature. Bukkit plugins also do not run on Bedrock, the unmodified Mojang server, or arbitrary snapshots.
