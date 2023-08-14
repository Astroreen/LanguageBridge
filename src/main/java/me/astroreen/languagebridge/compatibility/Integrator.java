package me.astroreen.languagebridge.compatibility;

import me.astroreen.languagebridge.exception.HookException;

/**
 * Integrator object performs integration with other plugins.
 */
public interface Integrator {

    /**
     * Integrate with another plugin.
     *
     * @throws HookException Is thrown, if the hooking was not successful
     */
    void hook() throws HookException;

    /**
     * Reload the plugin integration.
     */
    void reload();

    /**
     * Clean up everything.
     */
    void close();
}
