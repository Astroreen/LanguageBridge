package me.astroreen.languagebridge.database;

import me.astroreen.languagebridge.LanguageBridge;
import me.astroreen.languagebridge.database.Database;
import lombok.CustomLog;
import org.jetbrains.annotations.NotNull;

import java.sql.*;

/**
 * Connects to and uses a MySQL database
 */
@CustomLog
public class MySQL extends Database {

    private final String user;
    private final String database;
    private final String password;
    private final String port;
    private final String hostname;

    /**
     * Creates a new MySQL instance
     *
     * @param plugin   Plugin instance
     * @param hostname Name of the host
     * @param port     Port number
     * @param database Database color
     * @param username Username
     * @param password Password
     */
    public MySQL(final @NotNull LanguageBridge plugin,
                 final String hostname,
                 final String port,
                 final String database,
                 final String username,
                 final String password) {
        super(plugin.getPluginConfig().getString("mysql.prefix", ""));
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.user = username;
        this.password = password;
    }

    @Override
    public Connection openConnection() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + this.hostname + ":" + this.port + "/" + this.database + "?&useSSL=false", this.user, this.password);
        } catch (ClassNotFoundException | SQLException e) {
            LOG.warn("MySQL says: " + e.getMessage(), e);
        }
        return connection;
    }

    @Override
    public void createColumn(String tableName, String columnName, String columnDataType) {
        try {
            final Statement statement = getConnection().createStatement();
            try {
                ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
                        + prefix + tableName + "' AND COLUMN_NAME = '" + columnName + "'");

                rs.next();
                if (rs.getInt(1) == 0) {
                    statement.executeUpdate("ALTER TABLE " + prefix
                            + tableName + " ADD COLUMN `" + columnName + "` " + columnDataType);
                }
            } catch (SQLException e) {
                statement.executeUpdate("ALTER TABLE " + prefix
                        + tableName + " ADD COLUMN `" + columnName + "` " + columnDataType);
            }
        } catch (SQLException e) {
            LOG.error("There was an exception with SQL", e);
        }
    }
}
