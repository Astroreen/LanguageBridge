package me.astroreen.languagebridge.compatibility;

import me.astroreen.languagebridge.LanguageBridge;
import me.astroreen.languagebridge.compatibility.luckperms.BRLuckPermsIntegrator;
import me.astroreen.languagebridge.compatibility.placeholderapi.PlaceholderAPIIntegrator;
import me.astroreen.languagebridge.listener.ListenerManager;
import me.astroreen.languagebridge.exception.HookException;
import lombok.CustomLog;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Compatibility with other plugins
 */
@CustomLog
public class Compatibility implements Listener {

    private static Compatibility instance;
    private final Map<CompatiblePlugin, Integrator> integrators = new HashMap<>();
    private final List<CompatiblePlugin> hooked = new ArrayList<>();

    public Compatibility() {
        instance = this;

        integrators.put(CompatiblePlugin.PLACEHOLDERAPI, new PlaceholderAPIIntegrator());
        integrators.put(CompatiblePlugin.LUCKPERMS, new BRLuckPermsIntegrator());

        // hook into already enabled plugins in case Bukkit messes up the loading order
        for (final Plugin hook : Bukkit.getPluginManager().getPlugins()) {
            hook(hook);
        }

        ListenerManager.register("Compatibility", this);
        new BukkitRunnable() {
            @Override
            public void run() {
                // log which plugins have been hooked
                if (!hooked.isEmpty()) {
                    final StringBuilder string = new StringBuilder();
                    for (final CompatiblePlugin plugin : hooked) {
                        string.append(plugin.name).append(", ");
                    }
                    final String plugins = string.substring(0, string.length() - 2);
                    LOG.info("Hooked into " + plugins + "!");
                }
            }
        }.runTask(LanguageBridge.getInstance());

    }

    /**
     * @return the list of hooked plugins
     */
    public static List<CompatiblePlugin> getHooked() {
        return instance.hooked;
    }

    public static void reload() {
        for (final CompatiblePlugin hooked : getHooked()) {
            instance.integrators.get(hooked).reload();
        }
    }

    public static void disable() {
        if (instance != null) {
            for (final CompatiblePlugin hooked : getHooked()) {
                final Integrator integrator= instance.integrators.get(hooked);
                if(integrator != null) integrator.close();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPluginEnable(final @NotNull PluginEnableEvent event) {
        hook(event.getPlugin());
    }
    @EventHandler(ignoreCancelled = true)
    public void onPluginDisable(final @NotNull PluginDisableEvent event) {
        for (CompatiblePlugin plugin : CompatiblePlugin.values()) {
            if(event.getPlugin().getName().equals(plugin.name)) {
                if(event.getPlugin().isEnabled() && Compatibility.getHooked().contains(plugin)) {
                    final Integrator integrator= instance.integrators.get(plugin);
                    if(integrator != null) integrator.close();
                }
                integrators.remove(plugin);
                break;
            }
        }
    }

    private void hook(final @NotNull Plugin hookedPlugin) {
        CompatiblePlugin compatible = null;
        for (CompatiblePlugin plugin : CompatiblePlugin.values()) {
            if(hookedPlugin.getName().equals(plugin.name)) {
                compatible = plugin;
                break;
            }
        }

        if(compatible == null) return;

        // don't want to hook twice
        if (hooked.contains(compatible)) return;

        // don't want to hook into disabled plugins
        if (!hookedPlugin.isEnabled()) return;

        final String name = hookedPlugin.getName();
        final Integrator integrator = integrators.get(compatible);

        // this plugin is not an integration
        if (integrator == null) return;

        // hook into the plugin if it's enabled in the config
        if (LanguageBridge.getInstance().getPluginConfig().getBoolean("hook." + name.toLowerCase(Locale.ROOT))) {
            LOG.info("Hooking into " + name);

            // log important information in case of an error
            try {
                integrator.hook();
                hooked.add(compatible);
            } catch (final HookException exception) {
                final String message = String.format("Could not hook into %s %s! %s",
                        hookedPlugin.getName(),
                        hookedPlugin.getPluginMeta().getVersion(),
                        exception.getMessage());
                LOG.warn(message, exception);
                LOG.warn("Bridge will work correctly, except for that single integration. "
                        + "You can turn it off by setting 'hook." + name.toLowerCase(Locale.ROOT)
                        + "' to false in config.yml file.");
            } catch (final RuntimeException | LinkageError exception) {
                final String message = String.format("There was an unexpected error while hooking into %s %s (Bridge %s, Spigot %s)! %s",
                        hookedPlugin.getName(),
                        hookedPlugin.getPluginMeta().getVersion(),
                        LanguageBridge.getInstance().getPluginMeta().getVersion(),
                        Bukkit.getVersion(),
                        exception.getMessage());
                LOG.error(message, exception);
                LOG.warn("Bridge will work correctly, except for that single integration. "
                        + "You can turn it off by setting 'hook." + name.toLowerCase(Locale.ROOT)
                        + "' to false in config.yml file.");
            }
        }
    }
}
