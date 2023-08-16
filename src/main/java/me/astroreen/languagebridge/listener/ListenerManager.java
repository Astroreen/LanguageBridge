package me.astroreen.languagebridge.listener;

import lombok.CustomLog;
import me.astroreen.languagebridge.LanguageBridge;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Set;

@CustomLog
public class ListenerManager {

    private static LanguageBridge plugin;
    private static final HashMap<String, Listener> registered = new HashMap<>();


    public static void setup(final @NotNull LanguageBridge plugin) {
        ListenerManager.plugin = plugin;

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
