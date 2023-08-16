package me.astroreen.languagebridge.module.logger;

import me.astroreen.languagebridge.module.config.ConfigurationFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Handler;

public class DebugHandlerConfig {

    /**
     * The full path to the config setting, that saved if debugging is enabled.
     */
    private static final String CONFIG_ENABLED_PATH = "debug";

    private static boolean DEBUGGING = false;

    /**
     * The {@link ConfigurationFile} where to configure debugging.
     */
    private static ConfigurationFile config;

    /**
     * Wrap the given {@link ConfigurationFile} to easily access the relevant options for the debug {@link Handler}.
     *
     * @param config the related {@link ConfigurationFile}
     */
    public static void setup(final @NotNull ConfigurationFile config) {
        DebugHandlerConfig.config = config;
        DEBUGGING = config.getBoolean(CONFIG_ENABLED_PATH, false);
    }

    /**
     * Get logging state.
     *
     * @return true if debugging is enabled in the config; false otherwise
     */
    public static boolean isDebugging() {
        return DEBUGGING;
    }

    /**
     * Set logging state.
     *
     * @param debug enabled state to set
     * @throws IOException when persisting the changed state fails
     */
    public static void setDebugging(final boolean debug) throws IOException {
        if (config.getBoolean(CONFIG_ENABLED_PATH) == debug) return;

        DEBUGGING = debug;
        config.set(CONFIG_ENABLED_PATH, debug);
        config.save();
    }
}
