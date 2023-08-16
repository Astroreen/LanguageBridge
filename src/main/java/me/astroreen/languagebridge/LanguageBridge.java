package me.astroreen.languagebridge;

import lombok.Getter;
import me.astroreen.languagebridge.commands.LanguageBridgeCommand;
import me.astroreen.languagebridge.compatibility.Compatibility;
import me.astroreen.languagebridge.compatibility.CompatiblePlugin;
import me.astroreen.languagebridge.compatibility.luckperms.LPPermissionManager;
import me.astroreen.languagebridge.config.Config;
import me.astroreen.languagebridge.database.AsyncSaver;
import me.astroreen.languagebridge.database.Database;
import me.astroreen.languagebridge.database.MySQL;
import me.astroreen.languagebridge.database.SQLite;
import me.astroreen.languagebridge.listener.ListenerManager;
import me.astroreen.languagebridge.module.config.ConfigurationFile;
import me.astroreen.languagebridge.module.logger.BRLogger;
import me.astroreen.languagebridge.module.logger.DebugHandlerConfig;
import me.astroreen.languagebridge.module.permissions.DefaultPermissionManager;
import me.astroreen.languagebridge.module.permissions.Permission;
import me.astroreen.languagebridge.module.permissions.PermissionManager;
import me.astroreen.languagebridge.utils.StartScreen;
import me.clip.placeholderapi.libs.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
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
    private BRLogger log;
    private ConfigurationFile config;
    private Database database;
    private AsyncSaver saver;
    private static PermissionManager permManager;
    private boolean isMySQLUsed;

    @NotNull
    public ConfigurationFile getPluginConfig() {
        return config;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this; //when plugin is available for use
        log = BRLogger.create(this);

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

        // Initialize
        ListenerManager.setup(this);//listeners
        new Compatibility();        //compatibility with other plugins
        Config.setup(this);         //messages
                                    //permission manager
        permManager = Compatibility.getHooked().contains(CompatiblePlugin.LUCKPERMS) ?
                LPPermissionManager.getInstance() : new DefaultPermissionManager();
        new LanguageBridgeCommand();//commands
        //todo: make command to select different language (f.e. /language)

        // connect to database
        openDatabaseConnection();

        // create tables in the database
        database.createTables(isMySQLUsed);

        // create and start the saver object,
        // which handles correct asynchronous
        // saving to the database
        saver = new AsyncSaver();
        saver.start();


        // Done!
        new StartScreen(this.getServer().getConsoleSender()).BridgeImage();
        log.info("Bridge successfully enabled!");

        //refreshing db connection
        final long updateTime = config.getLong("mysql.updateTime", 30) * 1200; //minutes
        final Runnable runnable = () -> {
            try {
                database.getConnection().prepareStatement("SELECT 1").executeQuery().close();
            } catch (final SQLException e) {
                log.warn("Refreshing the database...", e);
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
        Compatibility.reload();
        permManager = Compatibility.getHooked().contains(CompatiblePlugin.LUCKPERMS) ?
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

    /**
     * Returns the database instance
     *
     * @return {@link Database} instance
     */
    public Database getDatabase() {
        return database;
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
}
