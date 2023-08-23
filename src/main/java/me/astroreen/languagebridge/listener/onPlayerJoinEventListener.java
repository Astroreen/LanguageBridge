package me.astroreen.languagebridge.listener;

import me.astroreen.astrolibs.api.listener.EventListener;
import me.astroreen.languagebridge.LanguageBridge;
import me.astroreen.languagebridge.config.Config;
import me.astroreen.languagebridge.database.Connector;
import me.astroreen.languagebridge.database.UpdateType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class onPlayerJoinEventListener extends EventListener {

    final Connector connector;

    public onPlayerJoinEventListener(final @NotNull LanguageBridge plugin) throws NoSuchMethodException {
        super(plugin);
        this.connector = new Connector(plugin.getDatabase());
    }


    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoinEvent(final @NotNull PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        connector.updateSQL(UpdateType.UPDATE_PLAYER_LANGUAGE, Config.getDefaultLanguage(), player.getUniqueId().toString());
    }

    @Override
    public String getName() {
        return "player-join";
    }
}
