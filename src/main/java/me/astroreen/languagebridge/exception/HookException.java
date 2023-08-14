package me.astroreen.languagebridge.exception;

/**
 * Thrown if Bridge tries to hook a plugin
 */
public class HookException extends Exception {

    /**
     * Constructs a new exception related to a plugin
     * {@link Exception#Exception(String)}
     *
     * @param message The Message.
     */
    public HookException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception related to a plugin
     * {@link Exception#Exception(String, Throwable)}
     *
     * @param message The message
     * @param cause   The Throwable
     */
    public HookException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception related to a plugin
     * {@link Exception#Exception(Throwable)}
     *
     * @param cause  the exception cause.
     */
    public HookException(final Throwable cause) {
        super(cause);
    }
}
