package me.astroreen.languagebridge.config;

public enum SoundType {
    PING("sounds.ping");

    final String path;

    SoundType(final String path) {
        this.path = path;
    }
}
