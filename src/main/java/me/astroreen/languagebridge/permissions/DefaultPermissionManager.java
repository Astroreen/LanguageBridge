package me.astroreen.languagebridge.permissions;

import lombok.CustomLog;
import me.astroreen.astrolibs.utils.PlayerConverter;
import me.astroreen.languagebridge.LanguageBridge;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@CustomLog
public class DefaultPermissionManager extends PermissionManager {

    public DefaultPermissionManager() {
    }

    @Override
    public boolean hasPermission(@NotNull UUID uuid, @NotNull String permission) {
        Player player = PlayerConverter.getPlayer(uuid);
        if (player == null) return false;
        return hasPermission(player, permission);
    }

    @Override
    public boolean hasPermission(final @NotNull Player player, final @NotNull String permission) {
        return player.hasPermission(permission);
    }

    @Override
    public void addPermission(final @NotNull UUID uuid, final @NotNull String permission, final boolean value) {
        final Player player = PlayerConverter.getPlayer(uuid);
        if(player == null) { // player not online
            LOG.warn("Couldn't set permission '"
                    + permission +
                    "' to the player, because he was not online.");
            return;
        }
        player.addAttachment(LanguageBridge.getInstance(), permission, value);
    }
}
