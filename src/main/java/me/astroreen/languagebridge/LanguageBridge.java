package me.astroreen.languagebridge;

import lombok.Getter;
import me.astroreen.languagebridge.commands.BridgeCommand;
import me.astroreen.languagebridge.compatibility.Compatibility;
import me.astroreen.languagebridge.compatibility.CompatiblePlugin;
import me.astroreen.languagebridge.compatibility.luckperms.LPPermissionManager;
import me.astroreen.languagebridge.config.Config;
import me.astroreen.languagebridge.module.config.ConfigurationFile;
import me.astroreen.languagebridge.module.logger.BRLogger;
import me.astroreen.languagebridge.module.logger.DebugHandlerConfig;
import me.astroreen.languagebridge.module.permissions.DefaultPermissionManager;
import me.astroreen.languagebridge.module.permissions.Permission;
import me.astroreen.languagebridge.module.permissions.PermissionManager;
import me.astroreen.languagebridge.utils.StartScreen;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

public final class LanguageBridge extends JavaPlugin {
    /**
     * The Bridge Plugin instance.
     * <p>
     * -- GETTER --
     * <p>
     * Get the plugin's instance.
     */
    @Getter
    public static LanguageBridge instance;
    private static BRLogger log;
    private ConfigurationFile config;
    private PermissionManager permManager;

    @NotNull
    public ConfigurationFile getPluginConfig() {
        return config;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this; //when plugin is available for use
        log = BRLogger.create(this);
        try {
            config = ConfigurationFile.create(new File(getDataFolder(), "config.yml"), this, "server/config.yml");
        } catch (InvalidConfigurationException | FileNotFoundException e) {
            getLogger().log(Level.SEVERE, "Could not load the config.yml file!", e);
            return;
        }
        DebugHandlerConfig.setup(config);

        // Initialize
        Config.setup(this);  //messages
        new Compatibility(); //compatibility with other plugins
        new BridgeCommand(); //commands

        //permissions
        for (Permission permission : Permission.values())
            Bukkit.getPluginManager().addPermission(permission.convert());
        permManager = Compatibility.getHooked().contains(CompatiblePlugin.LUCKPERMS) ?
                LPPermissionManager.getInstance() : new DefaultPermissionManager();


        // Done!
        new StartScreen(this.getServer().getConsoleSender()).BridgeImage();
        log.info("Bridge successfully enabled!");
    }

    public void reload() {
        // reload the configuration
        log.debug("Reloading configuration");
        try {
            config.reload();
        } catch (final IOException e) {
            log.warn("Could not reload config! " + e.getMessage(), e);
        }
        Config.setup(this);
        DebugHandlerConfig.setup(config);
        Compatibility.reload();
        permManager = Compatibility.getHooked().contains(CompatiblePlugin.LUCKPERMS) ?
                LPPermissionManager.getInstance() : new DefaultPermissionManager();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        Compatibility.disable();

        Bukkit.getScheduler().cancelTasks(this);

        // done
        log.info("Bridge successfully disabled!");
    }

    /**
     * Returns the permission manager
     *
     * @return {@link DefaultPermissionManager} instance
     */
    public PermissionManager getPermissionManager() {
        return permManager;
    }
}
