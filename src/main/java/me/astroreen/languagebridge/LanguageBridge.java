package me.astroreen.languagebridge;

import lombok.Getter;
import me.astroreen.astrolibs.api.config.ConfigurationFile;
import me.astroreen.astrolibs.api.logger.Logger;
import me.astroreen.astrolibs.api.logger.LoggerFactory;
import me.astroreen.astrolibs.compatibility.Compatibility;
import me.astroreen.astrolibs.compatibility.CompatiblePlugin;
import me.astroreen.astrolibs.compatibility.Integrator;
import me.astroreen.astrolibs.listener.ListenerManager;
import me.astroreen.astrolibs.module.logger.DebugHandlerConfig;
import me.astroreen.languagebridge.commands.LanguageBridgeCommand;
import me.astroreen.languagebridge.compatibility.luckperms.BRLuckPermsIntegrator;
import me.astroreen.languagebridge.compatibility.luckperms.LPPermissionManager;
import me.astroreen.languagebridge.compatibility.placeholderapi.PlaceholderAPIIntegrator;
import me.astroreen.languagebridge.config.Config;
import me.astroreen.languagebridge.database.AsyncSaver;
import me.astroreen.languagebridge.database.Database;
import me.astroreen.languagebridge.database.MySQL;
import me.astroreen.languagebridge.database.SQLite;
import me.astroreen.languagebridge.module.permissions.DefaultPermissionManager;
import me.astroreen.languagebridge.module.permissions.Permission;
import me.astroreen.languagebridge.module.permissions.PermissionManager;
import me.astroreen.languagebridge.module.placeholder.PlaceholderManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public final class LanguageBridge extends JavaPlugin {
    /**
     * Get the plugin's instance.
     *
     * @return {@link LanguageBridge}'s instance.
     */
    @Getter
    private static LanguageBridge instance;
    private Logger log;
    private ConfigurationFile config;
    /**
     * Returns the database instance
     *
     * @return {@link Database}'s instance
     */
    @Getter
    private Database database;
    private AsyncSaver saver;
    /**
     * Returns the permission manager
     *
     * @return {@link DefaultPermissionManager} instance
     */
    @Getter
    private static PermissionManager permissionManager;
    private boolean isMySQLUsed;
    @Getter
    private PlaceholderManager placeholderManager;
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
    public @Nullable ConfigurationFile getPluginConfig() {
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
            getLogger().log(Level.SEVERE, "Could not load the config.yml file!", e);
            return;
        }
        DebugHandlerConfig.setup(config);

        // connect to database
        openDatabaseConnection();

        // event priority
        LanguageBridge.eventPriority = reloadEventPriority();

        // Initialize
        //todo: create listeners to translate native placeholders
        //for books, anvils, chat, nicknames(tags), boss bar, tab list, titles
        ListenerManager.setup(this, null, log);                            //listeners
        new Compatibility(this, getIntegrators(), config, log);                   //compatibility with other plugins
        Config.setup(this);                                                             //messages
        permissionManager = Compatibility.getHooked()                                   //permission manager
                .contains(CompatiblePlugin.LUCKPERMS) ?
                LPPermissionManager.getInstance() : new DefaultPermissionManager();
        placeholderManager = new PlaceholderManager(this);                        //placeholder manager
        new LanguageBridgeCommand();                                                    //commands
        //todo: make command to select different language (f.e. /language)

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
            try {
                database.getConnection().prepareStatement("SELECT 1").executeQuery().close();
            } catch (final SQLException e) {
                log.error("Could not refresh the database! Is connection lost?", e);
            }
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
        DebugHandlerConfig.setup(config);
        LanguageBridge.eventPriority = reloadEventPriority();
        Compatibility.reload();
        permissionManager = Compatibility.getHooked().contains(CompatiblePlugin.LUCKPERMS) ?
                LPPermissionManager.getInstance() : new DefaultPermissionManager();

        //updating database connection configuration
        if (database != null) database.closeConnection();
        openDatabaseConnection();
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
}
