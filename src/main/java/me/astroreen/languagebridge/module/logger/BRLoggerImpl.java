package me.astroreen.languagebridge.module.logger;

import org.fusesource.jansi.Ansi;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the implementation of the interface {@link BRLogger}.
 */
public class BRLoggerImpl implements BRLogger {

    /**
     * The {@link Plugin} this logger belongs to.
     */
    private final Plugin plugin;

    /**
     * The original logger.
     */
    private final Logger logger;

    /**
     * Creates a decorator for the {@link TopicLogger}.
     *
     * @param plugin       The {@link Plugin} this logger belongs to.
     * @param parentLogger The parent logger for this logger.
     * @param clazz        The calling class.
     * @param topic        The topic of the logger.
     */
    public BRLoggerImpl(@NotNull final Plugin plugin, final Logger parentLogger, final Class<?> clazz, final String topic) {
        this.plugin = plugin;
        this.logger = new TopicLogger(parentLogger, clazz, topic);
    }

    @Override
    public void info(final String msg) {
        final BRLogRecord record = new BRLogRecord(Level.INFO, msg, plugin.getName());
        logger.log(record);
    }

    @Override
    public void warn(final String msg) {
        warn(msg, null);
    }

    @Override
    public void warn(final String msg, final Throwable thrown) {
        final BRLogRecord record = new BRLogRecord(Level.WARNING, msg, plugin.getName());
        record.setThrown(thrown);
        logger.log(record);
    }

    @Override
    public void error(final String msg) {
        error(msg, null);
    }

    @Override
    public void error(final String msg, final Throwable thrown) {
        final BRLogRecord record = new BRLogRecord(Level.SEVERE, msg, plugin.getName());
        record.setThrown(thrown);
        logger.log(record);
    }

    @Override
    public void debug(final String msg) {
        debug(msg, null);
    }

    @Override
    public void debug(final String msg, final Throwable thrown) {
        if (!DebugHandlerConfig.isDebugging()) return;
        final String DEBUG_ID = Ansi.ansi().fg(Ansi.Color.CYAN) + "[DEBUG] ";
        final BRLogRecord record = new BRLogRecord(Level.INFO, DEBUG_ID + msg, plugin.getName());
        record.setThrown(thrown);
        logger.log(record);
    }

    @Override
    public void reportException(Throwable thrown) {
        final String msg = "This is an exception that should never occur.";
        final BRLogRecord record = new BRLogRecord(Level.SEVERE, msg, plugin.getName());
        record.setThrown(thrown);
        logger.log(record);
    }
}
