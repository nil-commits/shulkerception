# Shulkerception

A server plugin for **Minecraft 26.2 with Java 25**, available in Paper and Bukkit/CraftBukkit/Spigot editions, that lets players put shulker boxes into placed shulker boxes. Players use ordinary, unmodified Minecraft Java clients. A Mojang vanilla server cannot load plugins.

## Install and use

1. Stop your server.
2. Copy the appropriate JAR into the server's `plugins` folder: `shulkerception-1.0.0-bukkit.jar` for CraftBukkit/Spigot 26.2, or `shulkerception-1.0.0-paper.jar` for Paper 26.2. Both are included in the release bundle and in `dist/`. The filename `dist/shulkerception-1.0.0.jar` is a compatibility copy of the current Paper edition. Install **only one** of these files; they all use the plugin name `Shulkerception` and the same configuration folder.
3. Start the server. Check the console for `Shulker nesting enabled`.
4. Place and open a shulker box. Click another shulker box into a slot, shift-click it from your inventory, or use a number key / offhand swap over a slot.

All 17 colors are supported, including undyed boxes. All players can use the feature; no commands, permissions, client mods, or additional plugins are required. Inner boxes can contain items or further boxes. To open an inner box, take it out and place it in the world.

The plugin writes directly to the placed box's inventory and clones complete item stacks, retaining their contents and metadata. Storage and block drops use the server's normal item persistence. It does not maintain a separate database or replace the shulker interface.

## Limits and scope

The first start creates `plugins/Shulkerception/config.yml`:

```yaml
max-depth: 5
max-contained-shulkers: 256
```

Depth includes the outer box. For example, depth 2 allows an outer box containing inner boxes that do not themselves contain boxes. The second limit counts every contained shulker, including descendants, in one outer box. Restart after editing configuration. Supported ranges are 2–16 for depth and 1–4096 for contained boxes. These limits prevent excessive nesting; they are not a general item-data size validator. They apply to new insertions, so existing boxes can always be removed.

- Click placement, shift-click insertion, number-key swaps, and offhand swaps are supported.
- Hopper/dropper insertion and drag placement keep vanilla restrictions. Use clicks to insert boxes.
- The listener skips events already cancelled by protection plugins and skips spectators. Custom portable-shulker interfaces are outside this plugin's scope. Compatibility with a particular protection/inventory plugin should be checked in-game.
- Shulkers supplied as non-vanilla stacks are split into individual slots by clicks/shift-clicks. Hotbar swaps require a single box.
- Both editions use public Bukkit APIs without server internals. The Bukkit edition is compiled against Spigot's maintained Bukkit API with no Paper dependency. Folia is unsupported.

## Build

Install JDK 25 and Maven 3.9+, then run from this directory:

```text
mvn package
mvn -P bukkit package
```

The first command builds `target/paper/shulkerception-1.0.0-paper.jar`; the second builds `target/bukkit/shulkerception-1.0.0-bukkit.jar`. Output directories are separate so builds cannot reuse classes compiled against the other API. Paper API is pinned to `26.2.build.123-stable`; the Bukkit edition pins Spigot API to `26.2-R0.1-20260816.205300-12`. Each API is provided by the server; dependencies are not bundled into the plugin. Select one profile per build. Plugin version and author are inserted into `plugin.yml` from Maven properties.

Run `./release.ps1` in PowerShell to test/build both editions, verify their embedded version/author, and regenerate the release folder, checksums, and ZIP. It uses Maven from PATH or the downloaded Maven in this workspace. This prepares local release files; publishing is a separate step.

For this workspace, the downloaded Maven is also available:

```powershell
& .\.tools\apache-maven-3.9.11\bin\mvn.cmd '-Dmaven.repo.local=.m2' package
& .\.tools\apache-maven-3.9.11\bin\mvn.cmd '-Dmaven.repo.local=.m2' -P bukkit package
```

## Verification

Both editions build successfully on Java 25, with all 31 JUnit/Mockito test cases passing against each API. They cover all 17 colors, preservation of nested metadata, cursor and hotbar swaps, offhand swaps, partial/full inventories, cancelled events, spectators, custom interfaces, aggregate/depth limits, and removal above limits. Tests use mocked server API objects; they do not verify Minecraft packet behavior or world persistence on a running server. Neither edition has been tested in-game here.

Before using valuable items, perform this in-game check:

1. Nest two differently colored boxes, with named items inside the inner one. Try click, shift-click, hotbar, and offhand insertion.
2. Remove the inner box and verify its contents. Try inserting into a full outer box.
3. In survival, break the outer box, pick it up, place it again, and verify both boxes and their items.
4. Restart the server and verify the contents again.
5. Test simultaneous access by two players and any installed protection plugins.

Paper's [project setup documentation](https://docs.papermc.io/paper/dev/project-setup/) specifies the 26.2 API and Java 25. The implementation follows the cancellation-and-apply guidance in [InventoryClickEvent](https://jd.papermc.io/paper/26.2/org/bukkit/event/inventory/InventoryClickEvent.html).

The Bukkit edition follows [Spigot's Maven guidance](https://www.spigotmc.org/wiki/spigot-maven/) and uses the shared methods in its [26.2 Bukkit API](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/block/Container.html).
