package me.astroreen.languagebridge.database;

import java.util.function.Function;

/**
 * Type of the update
 */
public enum UpdateType {

    /**
     * UUID, language.
     */
    ADD_PLAYER_LANGUAGE(prefix -> "INSERT INTO " + prefix + "language (playerID, language) VALUES (?, ?)"),
    /**
     * Language, UUID.
     */
    UPDATE_PLAYER_LANGUAGE(prefix -> "UPDATE " + prefix + "language SET language = ? WHERE playerID = ?"),
    LOAD_ALL_PLAYERS_UUID(prefix -> "SELECT playerID FROM " + prefix + "language"), //todo: on start get all uuids and on player join check if we have player's uuid and only then add to database
    ;


    /**
     * Function to create the SQL code from a prefix.
     */
    private final Function<String, String> statementCreator;

    UpdateType(final Function<String, String> sqlTemplate) {
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
