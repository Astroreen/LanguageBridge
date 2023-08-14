package me.astroreen.languagebridge.compatibility;

public enum CompatiblePlugin {

    PLACEHOLDERAPI("PlaceholderAPI"),
    LUCKPERMS("LuckPerms"),
    ;

    public final String name;
    CompatiblePlugin(final String name) {
        this.name = name;
    }
}
