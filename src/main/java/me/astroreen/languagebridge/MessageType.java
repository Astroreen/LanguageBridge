package me.astroreen.languagebridge;

/**
 * Type of messages
 */
public enum MessageType {
    PREFIX("prefix"),
    /**
     * Require: plugin version argument
     */
    PLUGIN_VERSION("version"),
    RELOAD("reloaded"),
    NO_PERMISSION("no-permission"),
    REQUIRE_PLAYER("need-to-be-player"),
    /**
     * Require: arguments that should not be
     */
    UNKNOWN_ARGUMENTS("unknown-arguments"),
    /**
     * Require: disabled plugin name
     */
    DISABLED_PLUGIN("plugin-is-disabled"),

    // Language
    /**
     * Require: current language name player use
     */
    CURRENT_LANGUAGE("language.current-language"),
    /**
     * Require: current language name
     */
    LANGUAGE_ALREADY_SET("language.already-language"),
    /**
     * Require: unknown language name
     */
    NO_SUCH_LANGUAGE("language.no-such-language"),
    /**
     * Require: language name set to
     */
    LANGUAGE_SET("language.set-language-successfully"),
    LANGUAGE_ERROR("language.set-language-error"),

    // Debug
    /**
     * Require: current debug state
     */
    CURRENT_DEBUG_STATE("debug.current-debug"),
    /**
     * Require: current debug state
     */
    DEBUG_ALREADY_SET("debug.already-debugging"),
    /**
     * Require: debug state set to
     */
    DEBUG_SET("debug.set-debug-successfully"),
    DEBUG_ERROR("debug.set-debug-error"),

    // Variables
    ENABLED("variable.enabled"),
    DISABLED("variable.disabled"),

    ;

    public final String path;

    MessageType(String path) {
        this.path = path;
    }
}
