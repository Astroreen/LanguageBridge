package me.astroreen.languagebridge.module.logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

/**
 * This facade uses the {@link PluginLogger} from the {@link Plugin#getLogger()} method.
 * It registers a new child logger for each class it's used in.
 */
public interface BRLogger {

    /**
     * Creates a logger.
     * <p>
     * Use this method to create a logger without a topic.
     * <p>
     * This method is also used by Lombok.
     *
     * @param clazz The class to create a logger for.
     * @return A {@link BRLogger} implementation.
     * @throws IllegalStateException Thrown if this is called from a class, that extends {@link Plugin}
     */
    @Contract("_ -> new")
    static @NotNull BRLogger create(@NotNull final Class<?> clazz) {
        return create(clazz, null);
    }


    /**
     * Creates a logger.
     * <p>
     * Use this method to create a logger with a topic.
     * <p>
     * This method is used by Lombok.
     *
     * @param clazz The class to create a logger for.
     * @return A {@link BRLogger} implementation.
     * @throws IllegalStateException Thrown if this is called from a class, that extends {@link Plugin}
     */
    @Contract("_, _ -> new")
    static @NotNull BRLogger create(@NotNull final Class<?> clazz, @Nullable final String topic) throws IllegalStateException {
            if (Plugin.class.isAssignableFrom(clazz) ) {
                throw new IllegalStateException("It is not allowed to use the '@CustomLog' annotation from the class '"
                        + clazz.getName() + "' which directly or indirectly extends 'org.bukkit.plugin.Plugin'!");
            }
            for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                if (plugin.getClass().getClassLoader().equals(clazz.getClassLoader())) {
                    return new BRLoggerImpl(plugin, plugin.getLogger(), clazz, topic);
                }
            }
            throw new IllegalStateException("The class '" + clazz.getName()
                    + "' has not been loaded by a 'org.bukkit.plugin.Plugin'. "
                    + "Therefore, it was not possible to create a logger for this class!");
    }

    /**
     * Creates a logger.
     * <p>
     * Use this method to create a logger for the {@link Plugin} class without a topic.
     * For other classes use the {@link BRLogger#create(Class)}
     * or {@link BRLogger#create(Class, String)} method.
     *
     * @param plugin The plugin which is used for logging.
     * @return A {@link BRLogger} implementation.
     */
    @Contract("_ -> new")
    static @NotNull BRLogger create(@NotNull final Plugin plugin) {
        return create(plugin, null);
    }

    /**
     * Creates a logger.
     * <p>
     * Use this method to create a logger for the {@link Plugin} class without a topic.
     * For other classes use the {@link BRLogger#create(Class)}
     * or {@link BRLogger#create(Class, String)} method.
     *
     * @param plugin The plugin which is used for logging.
     * @param topic  The optional topic of the logger.
     * @return A {@link BRLogger} implementation.
     */
    @Contract("_, _ -> new")
    static @NotNull BRLogger create(@NotNull final Plugin plugin, @Nullable final String topic) {
        return new BRLoggerImpl(plugin, plugin.getLogger(), plugin.getClass(), topic);
    }

    /**
     * Logs a normal message with the {@link Level#INFO} level to log.
     * <p>
     * Use this for normal log information.
     *
     * @param msg The message to log.
     */
    void info(String msg);

    /**
     * Logs a warning message with the {@link Level#WARNING} level to log.
     * <p>
     * Use this if you can provide useful information how to fix the underlying problem.
     *
     * @param msg The message to log.
     */
    void warn(String msg);

    /**
     * Logs a warning message with the {@link Level#WARNING} level to the log.
     * The {@link Throwable} is logged with the {@link Level#FINE} level to the log.
     * <p>
     * Use this if you can provide useful information how to fix the underlying problem.
     *
     * @param msg    The message to log.
     * @param thrown The throwable to log.
     */
    void warn(String msg, Throwable thrown);

    /**
     * Logs an error message with the {@link Level#SEVERE} level to the log.
     * <p>
     * Use this if the underlying problem affects the servers security or functionality.
     * Usage is also allowed if you don't know how the user can fix the underlying issue.
     *
     * @param msg The message to log.
     */
    void error(String msg);

    /**
     * Logs an error message with the {@link Level#SEVERE} level to the log.
     * The {@link Throwable} is logged together with the message.
     * <p>
     * Use this if the underlying problem affects the servers security or functionality.
     * Usage is also allowed if you don't know how the user can fix the underlying issue.
     *
     * @param msg    The message to log.
     * @param thrown The throwable to log.
     */
    void error(String msg, Throwable thrown);

    /**
     * Logs a debug message with the {@link Level#FINE} level to the log.
     * <p>
     * Use this for additional debug log information.
     *
     * @param msg The message to log.
     */
    void debug(String msg);

    /**
     * Logs a debug message with the {@link Level#FINE} level to the log.
     * The {@link Throwable} is logged together with the message.
     * <p>
     * Use this for additional debug log information.
     *
     * @param msg    The message to log.
     * @param thrown The throwable to log.
     */
    void debug(String msg, Throwable thrown);

    /**
     * Logs a {@link Throwable} with the {@link Level#SEVERE} level to the log.
     * The Throwable is logged together with a message that informs the user that the error
     * needs to be reported to the issue tracker.
     * <p>
     * Only use this in cases that should never occur and indicate an error that must be reported.
     *
     * @param thrown The throwable to log.
     */
    void reportException(Throwable thrown);

}
