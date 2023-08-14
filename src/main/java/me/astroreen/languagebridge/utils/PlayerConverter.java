package me.astroreen.languagebridge.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public final class PlayerConverter {

    private PlayerConverter() {
    }

    /**
     * Returns playerID of the player with passed color.
     *
     * @param name color of the player from which playerID needs to be extracted
     * @return playerID of the player
     */
    public static @NotNull UUID getID(final String name) {
        return Bukkit.getOfflinePlayer(name).getUniqueId();
    }


    /**
     * Returns the online Player object described by passed playerID.
     *
     * @param uuid player's {@link UUID}
     * @return the Player object or null if the player is not online
     */
    public static @Nullable Player getPlayer(final UUID uuid) {
        return Bukkit.getPlayer(uuid);
    }

    /**
     * Returns the online Player object described by passed playerID.
     *
     * @param name player's color
     * @return the Player object or null if the player is not online
     */
    public static @Nullable Player getPlayer(final String name) {
        return Bukkit.getPlayer(name);
    }

    /**
     * Returns the online Player object described by passed playerID.
     *
     * @param uuid player {@link UUID}
     * @return the Player object, wrapped in an optional
     */
    public static Optional<Player> getOptionalPlayer(final UUID uuid) {
        return Optional.ofNullable(getPlayer(uuid));
    }

    /**
     * Returns the online Player object described by passed playerID.
     *
     * @param name player color
     * @return the Player object, wrapped in an optional
     */
    public static Optional<Player> getOptionalPlayer(final String name) {
        return Optional.ofNullable(getPlayer(name));
    }

    public static String getName(final String playerID) {
        return playerID == null ? null : Bukkit.getOfflinePlayer(UUID.fromString(playerID)).getName();
    }
}
