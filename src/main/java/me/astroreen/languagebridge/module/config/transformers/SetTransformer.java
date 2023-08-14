package me.astroreen.languagebridge.module.config.transformers;

import me.astroreen.languagebridge.exception.PatchException;
import me.astroreen.languagebridge.module.config.patcher.PatchTransformer;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

/**
 * Sets the given value on the given key.
 */
public class SetTransformer implements PatchTransformer {

    /**
     * Default constructor
     */
    public SetTransformer() {
    }

    @Override
    public void transform(final Map<String, String> options, final ConfigurationSection config) throws PatchException {
        final String key = options.get("key");
        final String value = options.get("value");

        config.set(key, value);
    }
}
