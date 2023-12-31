package me.astroreen.languagebridge.permissions;

import lombok.Getter;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public enum Permission {
        PLACEHOLDER_BOOK("placeholder.book", "Permission to use placeholders in books", PermissionDefault.FALSE),
        PLACEHOLDER_ANVIL("placeholder.anvil", "Permission to use placeholders in anvils", PermissionDefault.FALSE),
    ALL_PLACEHOLDERS("placeholder.*",
            "General permission to write and use all placeholders", PermissionDefault.OP,
            setChildren(PLACEHOLDER_ANVIL, PLACEHOLDER_BOOK)),
        LANGUAGE_COMMAND("command.language", "Permission for command to choose language", PermissionDefault.TRUE),
        MAIN_COMMAND("command.languagebridge", "Permission for main command", PermissionDefault.OP),
    All_COMMANDS("command.*", PermissionDefault.OP, setChildren(MAIN_COMMAND, LANGUAGE_COMMAND)),
        CHANGE_DEFAULT_LANGUAGE(
                "language.change.default",
                "Permission to change language that sets automatically to newly joined players",
                PermissionDefault.FALSE),
        DEBUG("debug", "Permission for controlling debug state", PermissionDefault.FALSE),
        RELOAD("reload", "Permission to reload plugin", PermissionDefault.FALSE),
    ADMIN(  "admin", "Permission for admins", PermissionDefault.OP,
            setChildren(All_COMMANDS, RELOAD, DEBUG, CHANGE_DEFAULT_LANGUAGE, ALL_PLACEHOLDERS)),
    ALL("*", PermissionDefault.OP, setChildren(ADMIN)),

    ;
    private static @NotNull Map<String, Boolean> setChildren(Permission... permissions) {
        final Map<String, Boolean> map = new HashMap<>(permissions.length);
        for (Permission perm : permissions) map.put(perm.name, true);
        return map;
    }

    private final String PREFIX = "languagebridge.";

    @Getter
    private final String name;
    @Getter
    private String description = null;
    @Getter
    private PermissionDefault permDefault = null;
    private Map<String, Boolean> children = null;

    Permission(@NotNull String name) {
        this(name, PermissionDefault.FALSE);
    }
    Permission(@NotNull String name, @Nullable PermissionDefault permDefault) {
        this(name, null, permDefault);
    }
    Permission(@NotNull String name, @Nullable String description, @Nullable PermissionDefault permDefault) {
        this(name, description, permDefault, null);
    }
    Permission(@NotNull String name, @Nullable PermissionDefault permDefault, @Nullable Map<String, Boolean> children) {
        this(name, null, permDefault, children);
    }
    Permission(@NotNull String name, @Nullable String description, @Nullable PermissionDefault permDefault, @Nullable Map<String, Boolean> children) {
        this.name = PREFIX + name;
        this.description = description;
        this.permDefault = permDefault;
        this.children = children;
    }

    @Contract(" -> new")
    public org.bukkit.permissions.@NotNull Permission convert() {
        return new org.bukkit.permissions.Permission(name, description, permDefault, children);
    }

    public @NotNull Set<Permission> getChildren(final Permission permission){
        if(permission == null) return Collections.emptySet();

        final Set<String> keys = permission.children.keySet();
        final Set<Permission> permissions = new HashSet<>(keys.size());

        //converting
        for(final String key : keys) permissions.add(Permission.valueOf(key));

        return permissions;
    }

}
