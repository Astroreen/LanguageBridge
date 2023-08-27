package me.astroreen.languagebridge.listener;

import me.astroreen.astrolibs.api.listener.EventListener;
import me.astroreen.languagebridge.LanguageBridge;
import me.astroreen.languagebridge.PlaceholderManager;
import me.astroreen.languagebridge.config.Config;
import me.astroreen.languagebridge.permissions.Permission;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

public class onPlayerEditBookEventListener extends EventListener {

    private final LanguageBridge plugin;

    public onPlayerEditBookEventListener(final @NotNull LanguageBridge plugin) throws NoSuchMethodException {
        super(
                plugin,
                PlayerEditBookEvent.class,
                onPlayerEditBookEventListener.class.getMethod("onPlayerEditBookEvent", PlayerEditBookEvent.class),
                LanguageBridge.getEventPriority()
        );
        this.plugin = plugin;
    }

    public void onPlayerEditBookEvent(final @NotNull PlayerEditBookEvent event){
        final Player player = event.getPlayer();

        if(Config.noPermission(player, Permission.PLACEHOLDER_BOOK)) return;

        BookMeta meta = event.getNewBookMeta();

        final String title = meta.getTitle();
        if(meta.hasTitle() && title != null) {
            meta = meta.title(Config.parseText(player, title));
        }

        final PlaceholderManager manager = plugin.getPlaceholderManager();
        for(int i = 1; i <= meta.getPageCount(); i++)
            meta.page(i, manager.translate(player, meta.page(i)));

        event.setNewBookMeta(meta);
    }

    @Override
    public String getName() {
        return "edit-book";
    }
}
