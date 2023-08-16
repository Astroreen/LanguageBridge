package me.astroreen.languagebridge.database;

import me.astroreen.languagebridge.LanguageBridge;
import lombok.CustomLog;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * Connects to and uses a SQLite database
 */
@SuppressWarnings("PMD.CommentRequired")
@CustomLog
public class SQLite extends Database {

    private final String dbLocation;
    private final LanguageBridge plugin;

    /**
     * Creates a new SQLite instance
     *
     * @param plugin     Plugin instance
     * @param dbLocation Location of the Database (Must end in .db)
     */
    public SQLite(final @NotNull LanguageBridge plugin, final @NotNull String dbLocation) {
        super(plugin.getPluginConfig().getString("mysql.prefix", ""));
        this.plugin = plugin;
        this.dbLocation = dbLocation;
    }
    @Override
    public Connection openConnection() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        final File file = new File(plugin.getDataFolder(), dbLocation);
        if (!(file.exists())) {
            try {
                file.createNewFile();
            } catch (final IOException e) {
                LOG.error("Unable to create database!", e);
            }
        }
        Connection connection = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager
                    .getConnection("jdbc:sqlite:" + plugin.getDataFolder().toPath() + "/" + dbLocation);
        } catch (ClassNotFoundException | SQLException e) {
            LOG.error("There was an exception with SQL", e);
        }
        return connection;
    }

    @Override
    public void createColumn(String tableName, String columnName, String columnDataType) {
        try {
            final Statement statement = getConnection().createStatement();
            try {
                statement.executeQuery("SELECT `" + columnName + "` FROM '" + prefix + tableName + "'");
            } catch (SQLException e) {
                statement.executeUpdate("ALTER TABLE " + prefix + tableName + " ADD COLUMN `"
                        + columnName + "` " + columnDataType);
            }
        } catch (SQLException e) {
            LOG.error("There was an exception with SQL", e);
        }
    }
}
