# Shulkerception 1.0.0

Author: **nil-commits**  
Release date: **2026-09-13**

Initial release for Minecraft **26.2**, requiring **Java 25**.

## Downloads

- `shulkerception-1.0.0-paper.jar`: Paper edition.
- `shulkerception-1.0.0-bukkit.jar`: Bukkit/CraftBukkit/Spigot edition.

Install one edition in your server's `plugins` folder and restart. When replacing a development build, remove its JAR first and keep the existing `plugins/Shulkerception/config.yml`.

## Features

- Put all 17 shulker box colors inside placed shulker boxes.
- Insert through normal clicks, shift-clicks, number keys, or offhand swaps.
- Retain nested contents and item metadata.
- Configure maximum nesting depth and total contained boxes. Defaults: 5 levels including the outer box, and 256 contained boxes.
- Use ordinary Minecraft clients without mods or additional plugins.

## Scope and validation

Hopper/dropper insertion and drag placement retain vanilla restrictions. Inner boxes must be removed and placed before opening. Folia and custom portable-shulker interfaces are unsupported.

Both editions pass all 31 automated inventory-logic tests. The tests use mocked API objects; live gameplay, packet behavior, world persistence, and third-party plugin compatibility have not been verified here. See README.md for the in-game verification steps.

SHA256SUMS.txt contains checksums for the two JARs and accompanying documentation.
