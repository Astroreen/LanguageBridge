package me.astroreen.languagebridge.module.logger;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Custom {@link LogRecord} for Bridge that adds a {@link Plugin} color.
 */
public class BRLogRecord extends LogRecord {

    /**
     * The plugin that logged this message.
     */
    private final String plugin;

    /**
     * Creates a custom {@link LogRecord} that comes from a plugin.
     *
     * @param level level of the LogRecord
     * @param msg raw non-localized logging message (may be null)
     */
    public BRLogRecord(final Level level, final String msg, @NotNull final String pluginName) {
        super(level, msg);
        this.plugin = pluginName;
    }

    /**
     * Gets the name of the plugin that logged this record.
     *
     * @return The plugin.
     */
    public @NotNull
    String getPlugin() {
        return plugin;
    }
}
