package me.astroreen.languagebridge.listener;

import lombok.CustomLog;
import me.astroreen.astrolibs.api.listener.EventListener;
import me.astroreen.languagebridge.LanguageBridge;
import me.astroreen.languagebridge.config.Config;
import me.astroreen.languagebridge.database.Connector;
import me.astroreen.languagebridge.database.QueryType;
import me.astroreen.languagebridge.database.UpdateType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@CustomLog
public class onPlayerJoinEventListener extends EventListener {

    private static final Set<UUID> exists = new HashSet<>();
    private static final Set<UUID> check = new HashSet<>();
    private static boolean starting;
    private static LanguageBridge plugin;
    private static Connector connector;

    public onPlayerJoinEventListener(final @NotNull LanguageBridge plugin) throws NoSuchMethodException {
        super(plugin);
        onPlayerJoinEventListener.plugin = plugin;
        onPlayerJoinEventListener.connector = new Connector(plugin.getDatabase());

        //as setup
        reload();
    }

    public static void reload() {
        exists.clear();
        check.clear();

        starting = true;

        new BukkitRunnable() {
            @Override
            public void run() {
                try (final ResultSet rs = connector.querySQL(QueryType.LOAD_ALL_PLAYERS_UUID)) {
                    while (rs.next()) exists.add(UUID.fromString(rs.getString("playerID")));
                } catch (SQLException e) {
                    LOG.error("There was an exception with SQL", e);
                }

                //if player joins when exists-list is not loaded, he will be added to the check-list.
                //after uuids were loaded, filtering ones that don't exist in database and adding them
                check.stream()
                        .filter(uuid -> !exists.contains(uuid))
                        .forEach(uuid -> {
                            connector.updateSQL(UpdateType.ADD_PLAYER, uuid.toString(), Config.getDefaultLanguage());
                            exists.add(uuid);
                        });

                check.clear();
                starting = false;
            }
        }.runTaskAsynchronously(plugin);

    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoinEvent(final @NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();

        //check if player is in database
        if (!exists.contains(uuid)) {
            if (starting) check.add(uuid);
            else {
                connector.updateSQL(UpdateType.ADD_PLAYER, uuid.toString(), Config.getDefaultLanguage());
                exists.add(uuid);
            }
        }

        //check if player's language is valid
        Config.getPlayerLanguage(uuid).ifPresentOrElse(lang -> {
            if(!Config.getLanguages().contains(lang))
                connector.updateSQL(UpdateType.UPDATE_PLAYER_LANGUAGE, Config.getDefaultLanguage(), uuid.toString());
        }, () -> connector.updateSQL(UpdateType.UPDATE_PLAYER_LANGUAGE, Config.getDefaultLanguage(), uuid.toString()));
    }

    @Override
    public String getName() {
        return "player-join";
    }
}
