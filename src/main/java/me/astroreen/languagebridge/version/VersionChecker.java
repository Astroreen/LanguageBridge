package me.astroreen.languagebridge.version;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class VersionChecker {

    public static PluginVersion getVersion() {
        final String version = getRawVersion();
        return switch (version) {
            case "1.20.1" -> PluginVersion.V1_20_1;
            case "1.20" -> PluginVersion.V1_20;
            case "1.19.4" -> PluginVersion.V1_19_4;
            default -> PluginVersion.NOT_FOUND;
        };
    }

    @NotNull
    public static String getRawVersion() {
        return Bukkit.getBukkitVersion().split("-")[0];
    }
}
