package me.astroreen.languagebridge;

import lombok.Getter;
import me.astroreen.astrolibs.api.compatibility.Compatibility;
import me.astroreen.astrolibs.api.compatibility.CompatiblePlugin;
import me.astroreen.astrolibs.api.compatibility.Integrator;
import me.astroreen.astrolibs.api.config.ConfigurationFile;
import me.astroreen.astrolibs.api.listener.Listener;
import me.astroreen.astrolibs.api.listener.ListenerManager;
import me.astroreen.astrolibs.api.logger.Logger;
import me.astroreen.astrolibs.api.logger.LoggerFactory;
import me.astroreen.astrolibs.module.logger.DebugHandlerConfig;
import me.astroreen.languagebridge.commands.LanguageBridgeCommand;
import me.astroreen.languagebridge.commands.LanguageCommand;
import me.astroreen.languagebridge.compatibility.luckperms.BRLuckPermsIntegrator;
import me.astroreen.languagebridge.compatibility.luckperms.LPPermissionManager;
import me.astroreen.languagebridge.compatibility.placeholderapi.PlaceholderAPIIntegrator;
import me.astroreen.languagebridge.config.Config;
import me.astroreen.languagebridge.database.*;
import me.astroreen.languagebridge.listener.onPlayerEditBookEventListener;
import me.astroreen.languagebridge.listener.onPlayerJoinEventListener;
import me.astroreen.languagebridge.listener.onPrepareAnvilEventListener;
import me.astroreen.languagebridge.permissions.DefaultPermissionManager;
import me.astroreen.languagebridge.permissions.Permission;
import me.astroreen.languagebridge.permissions.PermissionManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Stream;

public final class LanguageBridge extends JavaPlugin {
    /**
     * Get the plugin's instance.
     */
    private static @Getter LanguageBridge instance;
    /**
     * Returns the permission manager
     */
    private @Getter PermissionManager permissionManager;
    /**
     * Returns the database instance
     */
    private @Getter Database database;
    /**
     * Returns the placeholder manager
     */
    private @Getter PlaceholderManager placeholderManager;
    private Logger log;
    private ConfigurationFile config;
    private AsyncSaver saver;
    private boolean isMySQLUsed;
    /**
     * Get event priority defined in config.
     *
     * @return {@link EventPriority}
     */
    @Getter
    private static EventPriority eventPriority;

    /**
     * Get main configuration file.
     *
     * @return {@link ConfigurationFile}
     */
    public @NotNull ConfigurationFile getPluginConfig() {
        return config;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this; //when plugin is available for use
        log = LoggerFactory.create(this);

        // Set up permissions
        for (Permission permission : Permission.values())
            Bukkit.getPluginManager().addPermission(permission.convert());

        // Create config file
        try {
            config = ConfigurationFile.create(new File(getDataFolder(), "config.yml"), this, "config.yml");
        } catch (InvalidConfigurationException | FileNotFoundException e) {
            getLogger().log(Level.SEVERE, "Could not load the config.yml file! Shutting down plugin...", e);
            Bukkit.getPluginManager().disablePlugin(this);
        }
        DebugHandlerConfig.setup(config);

        // connect to database
        openDatabaseConnection();

        // event priority
        LanguageBridge.eventPriority = reloadEventPriority();

        // Initialize
        //todo: replace values on language switch (maybe use protocollib or nms instead of regular minecraft events?)
        //todo: create listeners to translate native placeholders
        //for chat, nicknames(tags), boss bar, tab list, titles, inventory titles, holograms, NPC names, kick/ban messages
        //done: anvils, books
        ListenerManager.setup(this, getListeners(), log);                         //listeners
        new Compatibility(this, getIntegrators(), config, log);                   //compatibility with other plugins
        Config.setup(this);                                                             //messages
        permissionManager = Compatibility.getHooked()                                   //permission manager
                .contains(CompatiblePlugin.LUCKPERMS) ?
                LPPermissionManager.getInstance() : new DefaultPermissionManager();
        placeholderManager = new PlaceholderManager(this);                        //placeholder manager
        new LanguageBridgeCommand();                                                    //commands
        new LanguageCommand();

        // create tables in the database
        database.createTables(isMySQLUsed);

        // create and start the saver object,
        // which handles correct asynchronous
        // saving to the database
        saver = new AsyncSaver();
        saver.start();


        // Done!
        new StartScreen(this.getServer().getConsoleSender()).BridgeImage();
        log.info("LanguageBridge successfully enabled!");

        //refreshing db connection
        final long updateTime = 30 * 1200; //minutes
        final Runnable runnable = () -> {
            new Connector(database).refresh();
        };
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, runnable, updateTime, updateTime);
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
        ListenerManager.setup(this, getListeners(), log);
        onPlayerJoinEventListener.reload();
        DebugHandlerConfig.setup(config);
        LanguageBridge.eventPriority = reloadEventPriority();

        //updating database connection configuration if it is null
        if (database != null && database.getConnection() == null) {
            database.closeConnection();
            this.database = null;
            openDatabaseConnection();
        }

        Compatibility.reload();
        permissionManager = Compatibility.getHooked().contains(CompatiblePlugin.LUCKPERMS) ?
                LPPermissionManager.getInstance() : new DefaultPermissionManager();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Compatibility.disable();

        Bukkit.getScheduler().cancelTasks(this);

        //close database connection
        if (saver != null) saver.end();
        if (database != null) database.closeConnection();

        // done
        log.info("LanguageBridge successfully disabled!");
    }

    private void openDatabaseConnection() {
        final boolean mySQLEnabled = config.getBoolean("mysql.enabled", true);
        if (mySQLEnabled) {
            log.debug("Connecting to MySQL database...");
            this.database = new MySQL(this, config.getString("mysql.host"),
                    config.getString("mysql.port"),
                    config.getString("mysql.database"),
                    config.getString("mysql.user"),
                    config.getString("mysql.password"));
            if (database.getConnection() != null) {
                isMySQLUsed = true;
                log.info("Successfully connected to MySQL database!");
            }
        }
        if (!mySQLEnabled || !isMySQLUsed) {
            this.database = new SQLite(this, "database.db");
            if (mySQLEnabled) {
                log.warn("No connection to the mySQL Database! Using SQLite for storing data as fallback!");
            } else {
                log.info("Using SQLite for storing data!");
            }
        }
    }

    private Map<CompatiblePlugin, Integrator> getIntegrators() {
        final Map<CompatiblePlugin, Integrator> integrators = new HashMap<>();

        integrators.put(CompatiblePlugin.LUCKPERMS, new BRLuckPermsIntegrator());
        integrators.put(CompatiblePlugin.PLACEHOLDERAPI, new PlaceholderAPIIntegrator());

        return integrators;
    }

    private EventPriority reloadEventPriority() {
        final String rawPriority = config.getString("settings.event-priority", "LOWEST");
        try {
            return EventPriority.valueOf(rawPriority);
        } catch (IllegalArgumentException e) {
            log.error("Config value of 'event-priority' must be defined correctly!", e);
            return EventPriority.LOWEST;
        }
    }

    private @NotNull Set<Listener> getListeners() {
        final Set<Listener> listeners = new HashSet<>();

        try {
            //listeners active by default
            Stream.of(
                    new onPlayerJoinEventListener(this)
            ).forEach(listeners::add);

            final ConfigurationSection section = config.getConfigurationSection("settings.check-placeholder-on");
            if (section == null) {
                log.warn("Could not register conditional listeners!");
                return listeners;
            }

            //conditional listeners
            if (section.getBoolean("rename-item-on-anvil-event", true))
                listeners.add(new onPrepareAnvilEventListener(this));

            if (section.getBoolean("edit-book-event", true))
                listeners.add(new onPlayerEditBookEventListener(this));

        } catch (NoSuchMethodException e) {
            log.error("Could not register listeners!", e);
        }

        return listeners;
    }
}
