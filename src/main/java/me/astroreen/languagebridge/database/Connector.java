package me.astroreen.languagebridge.database;

import lombok.CustomLog;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Connects to the database and queries it
 */
@CustomLog
public class Connector {

    private final String prefix;
    private final Database database;
    private Connection connection;

    /**
     * Opens a new connection to the database
     */
    public Connector(final @NotNull Database database) {
        this.database = database;
        this.prefix = database.getPrefix();
        this.connection = database.getConnection();
        refresh();
    }

    /**
     * This method should be used before any other database operations.
     */
    public final void refresh() {
        try {
            connection.prepareStatement("SELECT 1").executeQuery().close();
        } catch (final SQLException e) {
            LOG.error("Could not refresh database connection. Reconnecting to the database...", e);
            database.closeConnection();
            connection = database.getConnection();
        }
    }

    /**
     * Queries the database with the given type and arguments
     *
     * @param type type of the query
     * @param args arguments
     * @return ResultSet with the requested data
     */
    public ResultSet querySQL(final @NotNull QueryType type, final String @NotNull ... args) {
        final String sql = type.createSql(prefix);
        try {
            final PreparedStatement statement = connection.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                statement.setString(i + 1, args[i]);
            }
            return statement.executeQuery();
        } catch (final SQLException e) {
            LOG.warn("There was a exception with SQL", e);
            return null;
        }
    }

    /**
     * Updates the database with the given type and arguments
     *
     * @param type type of the update
     * @param args arguments
     */
    public void updateSQL(final @NotNull UpdateType type, final String @NotNull ... args) {
        final String sql = type.createSql(prefix);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                statement.setString(i + 1, args[i]);
            }
            statement.executeUpdate();
        } catch (final SQLException e) {
            LOG.error("There was an exception with SQL", e);
        }
    }
}
