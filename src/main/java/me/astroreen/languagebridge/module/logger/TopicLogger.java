package me.astroreen.languagebridge.module.logger;

import org.fusesource.jansi.Ansi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class TopicLogger extends Logger {
    /**
     * The topic of this logger.
     */
    private final String topic;

    /**
     * Creates a new {@link TopicLogger} that adds an optional topic.
     *
     * @param parentLogger A reference to the parent {@link Logger} which is used as parent for this logger.
     * @param clazz        The calling class.
     * @param topic        The topic to add or null.
     */
    public TopicLogger(@NotNull final Logger parentLogger, @NotNull final Class<?> clazz, @Nullable final String topic) {
        super(clazz.getCanonicalName(), null);
        setParent(parentLogger);
        setLevel(Level.ALL);
        //color of ID
        //final String ID = Ansi.ansi().fg(Ansi.Color.CYAN) + "[BR] " +  Ansi.ansi().fg(Ansi.Color.DEFAULT);
        this.topic = topic == null ? "" : Ansi.ansi().fg(Ansi.Color.GREEN) + "(" + topic + ") " +  Ansi.ansi().fg(Ansi.Color.DEFAULT);
    }

    /**
     * Logs a LogRecord to the log with the topic.
     *
     * @param logRecord The record to log.
     */
    @Override
    public void log(@NotNull final LogRecord logRecord) {
        logRecord.setMessage(topic + logRecord.getMessage());
        logRecord.setLoggerName(getParent().getName());
        super.log(logRecord);
    }
}
