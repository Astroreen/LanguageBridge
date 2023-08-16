package me.astroreen.languagebridge.listener;

import lombok.CustomLog;
import me.astroreen.languagebridge.LanguageBridge;
import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Set;

@CustomLog
public class ListenerManager {

    private static LanguageBridge plugin;
    private static final HashMap<String, Listener> registered = new HashMap<>();
    private static EventPriority priority = EventPriority.LOWEST;

    //todo: create listeners to translate native placeholders
    //for books, anvils, chat, nicknames(tags), boss bar, tab list, titles
    public static void setup(final @NotNull LanguageBridge plugin) {
        ListenerManager.plugin = plugin;

        //update priority for events
        final String eventPriority = plugin.getPluginConfig().getString("settings.event-priority", "LOWEST");
        try {
            ListenerManager.priority = EventPriority.valueOf(eventPriority);
        } catch (IllegalArgumentException e) {
            ListenerManager.priority = EventPriority.LOWEST;
            LOG.error("Could not set value from config (" + eventPriority + ") as event priority. Set to 'LOWEST' as default");
        }

        //registering default listeners
        //empty for now

        //broadcasting registered listeners on start
        new BukkitRunnable() {
            @Override
            public void run() {
                // log which listeners have been registered
                if (!registered.isEmpty()) {
                    final StringBuilder builder = new StringBuilder();
                    for (final String name : registered.keySet())
                        builder.append(name).append(", ");

                    final String plugins = builder.substring(0, builder.length() - 2);
                    LOG.debug("Registered listeners on start: " + plugins + ".");
                }
            }
        }.runTaskAsynchronously(LanguageBridge.getInstance());
    }

    public static void register(final @NotNull String name, final @NotNull Listener listener) {
        if(registered.containsKey(name) || registered.containsValue(listener)) return;
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        registered.put(name, listener);
        //LOG.debug("Listener '" + name + "' was registered.");
    }
}
