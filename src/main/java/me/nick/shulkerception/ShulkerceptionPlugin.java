package me.nick.shulkerception;

import org.bukkit.plugin.java.JavaPlugin;

public final class ShulkerceptionPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        int depth = bounded("max-depth", 5, 2, 16);
        int boxes = bounded("max-contained-shulkers", 256, 1, 4096);
        getServer().getPluginManager().registerEvents(
                new NestingListener(new NestingPolicy(depth, boxes)), this);
        getLogger().info("Shulker nesting enabled (depth " + depth + ", contained boxes " + boxes + ").");
    }

    private int bounded(String key, int fallback, int min, int max) {
        int configured = getConfig().getInt(key, fallback);
        int value = Math.max(min, Math.min(configured, max));
        if (value != configured) {
            getLogger().warning(key + " is outside " + min + "-" + max + "; using " + value + ".");
        }
        return value;
    }
}
