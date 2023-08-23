package me.astroreen.languagebridge.database;

import lombok.CustomLog;
import me.astroreen.languagebridge.LanguageBridge;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abstract Database class, serves as a base for any connection method (MySQL,
 * SQLite, etc.)
 */
@CustomLog
public abstract class Database {

    protected LanguageBridge plugin = LanguageBridge.getInstance();
    protected String prefix;
    protected Connection con;

    protected Database(final @NotNull String prefix) {
        this.prefix = prefix;
    }

    protected abstract Connection openConnection();

    public Connection getConnection() {
        try {
            if (con == null || con.isClosed()) con = openConnection();
        } catch (SQLException e){
            LOG.error("There was an exception with SQL", e);
        }

        return con;
    }

    public void closeConnection() {
        try {
            if(con != null && !con.isClosed()) con.close();
        } catch (final SQLException e) {
            LOG.error("There was an exception with SQL", e);
        }
    }

    /**
     * Modify existing table and add new column to it.
     *
     * @param tableName      color of table without prefix from config
     * @param columnName     color of new column
     * @param columnDataType data type of new column (e.g. VARCHAR(30), INT, TEXT)
     */
    public abstract void createColumn(final String tableName, final String columnName, final String columnDataType);

    public void createTables(final boolean isMySQLUsed) {
        final String autoIncrement = isMySQLUsed ? "AUTO_INCREMENT" : "AUTOINCREMENT";
        final String defaultLanguage = plugin.getPluginConfig().getString("settings.default-language", "en");
        try {
            //todo: update language name if it was changed
            getConnection().createStatement()
                    .executeUpdate("CREATE TABLE IF NOT EXISTS " + prefix + "language (id INTEGER PRIMARY KEY "
                            + autoIncrement + ", playerID VARCHAR(256) NOT NULL, language VARCHAR(25) "
                            + "NOT NULL DEFAULT " + defaultLanguage + ");");
        } catch (final SQLException e) {
            LOG.error("There was an exception with SQL", e);
        }
    }

    public String getPrefix() {return prefix;}
}
