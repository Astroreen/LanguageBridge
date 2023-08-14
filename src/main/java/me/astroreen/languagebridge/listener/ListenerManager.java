package me.astroreen.languagebridge.listener;

import lombok.CustomLog;
import me.astroreen.languagebridge.LanguageBridge;
import me.astroreen.languagebridge.version.ClassFinder;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@CustomLog
public class ListenerManager {

    private static LanguageBridge plugin;
    private static final HashMap<String, Listener> registered = new HashMap<>();


    public static void setup(final @NotNull LanguageBridge plugin) {
        ListenerManager.plugin = plugin;

        //todo: check
        //registering default listeners
        final Set<Class> listeners = new ClassFinder().findAllClasses(ListenerManager.class.getPackageName());
        listeners.remove(ListenerManager.class);
        listeners.forEach(event -> {
            final String[] rawName = event.getName().split("\\.");
            final String name = rawName[rawName.length - 1];
            try {
                register(name, (Listener) event.getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                LOG.error(String.format("Could not register listener '%s'!", name), e);
            }
        });

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
