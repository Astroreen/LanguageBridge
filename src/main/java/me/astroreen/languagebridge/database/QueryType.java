package me.astroreen.languagebridge.database;

import java.util.function.Function;

/**
 * Type of the query.
 */
public enum QueryType {
    /**
     * UUID.
     */
    SELECT_PLAYER_LANGUAGE(prefix -> "SELECT language FROM " + prefix + "language WHERE playerID = ?;"),
    ;

    /**
     * Function to create the SQL code from a prefix.
     */
    private final Function<String, String> statementCreator;

    QueryType(final Function<String, String> sqlTemplate) {
        this.statementCreator = sqlTemplate;
    }

    /**
     * Create the SQL code for the given table prefix.
     *
     * @param tablePrefix table prefix to use
     * @return SQL-code for the update
     */
    public String createSql(final String tablePrefix) {
        return statementCreator.apply(tablePrefix);
    }
}
