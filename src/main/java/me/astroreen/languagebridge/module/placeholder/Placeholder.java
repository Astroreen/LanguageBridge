package me.astroreen.languagebridge.module.placeholder;

public record Placeholder(String key, String value) {
    public Placeholder(String key, String value){
        this.key = key == null ? "" : key.trim();
        this.value = value == null ? "" : value;
    }
}
