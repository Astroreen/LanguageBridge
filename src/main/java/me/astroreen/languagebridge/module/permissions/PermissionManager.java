package me.astroreen.languagebridge.module.permissions;

import me.astroreen.languagebridge.utils.PlayerConverter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;


/**
 * Simple permission manager
 */
public abstract class PermissionManager{
    /**
     * Check if player have permission.
     *
     * @param uuid       the player uuid
     * @param permission permission to check
     * @return true if Player have permission
     */
    public boolean hasPermission(final @NotNull UUID uuid, final @NotNull String permission) {
        final Player player = PlayerConverter.getPlayer(uuid);
        if(player == null) return false;
        return hasPermission(player, permission);
    }

    /**
     * Check if player have permission.
     *
     * @param player     the player
     * @param permission permission to check
     * @return true if Player have permission
     */
    public abstract boolean hasPermission(final @NotNull Player player, final @NotNull String permission);

    /**
     * Add permission to player instance.
     *
     * @param uuid       uuid of the player
     * @param permission permission to check
     * @param value      is player available to use this permission
     */
    public abstract void addPermission(final @NotNull UUID uuid, final @NotNull String permission, final boolean value);
}
