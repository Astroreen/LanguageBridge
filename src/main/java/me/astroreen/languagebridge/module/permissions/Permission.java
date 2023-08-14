package me.astroreen.languagebridge.module.permissions;

import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public enum Permission {

    ALL("languagebridge.*", getAllPermissions()),
    ADMIN("languagebridge.admin");

    private static @NotNull Map<String, Boolean> getAllPermissions() {
        Map<String, Boolean> children = new HashMap<>(1);
        children.put(ADMIN.name, true);
        return children;
    }

    public final String name;
    public String description = null;
    public PermissionDefault permDefault = null;
    public Map<String, Boolean> children = null;

    Permission(@NotNull String name) {
        this(name, (String) null);
    }

    Permission(@NotNull String name, @Nullable String description) {
        this(name, description, (PermissionDefault) null);
    }

    Permission(@NotNull String name, @Nullable PermissionDefault permDefault) {
        this(name, null, permDefault);
    }

    Permission(@NotNull String name, @Nullable String description, @Nullable PermissionDefault permDefault) {
        this(name, description, permDefault, null);
    }

    Permission(@NotNull String name, @Nullable Map<String, Boolean> children) {
        this(name, null, null, children);
    }

    Permission(@NotNull String name, @Nullable String description, @Nullable Map<String, Boolean> children) {
        this(name, description, null, children);
    }

    Permission(@NotNull String name, @Nullable PermissionDefault permDefault, @Nullable Map<String, Boolean> children) {
        this(name, null, permDefault, children);
    }

    Permission(@NotNull String name, @Nullable String description, @Nullable PermissionDefault permDefault, @Nullable Map<String, Boolean> children) {
        this.name = name;
        this.description = description;
        this.permDefault = permDefault;
        this.children = children;
    }

    public org.bukkit.permissions.Permission convert() {
        return new org.bukkit.permissions.Permission(name, description, permDefault, children);
    }

}
