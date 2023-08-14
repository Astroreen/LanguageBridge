package me.astroreen.languagebridge.version;

public enum PluginVersion {
    NOT_FOUND(0),
    V1_19_4(1194),
    V1_20(1200),
    V1_20_1(1201),
    ;

    public final int weight;
    PluginVersion(int weight) {
        this.weight = weight;
    }
}
