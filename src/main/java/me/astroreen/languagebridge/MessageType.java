package me.astroreen.languagebridge;

/**
 * Type of messages
 */
public enum MessageType {
    PREFIX("prefix"),
    ;

    public final String path;

    MessageType(String path) {
        this.path = path;
    }
}
