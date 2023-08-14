package me.astroreen.languagebridge.module.config.transformers;

import me.astroreen.languagebridge.exception.PatchException;
import me.astroreen.languagebridge.module.config.patcher.PatchTransformer;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;

/**
 * Renames a key while preserving the value.
 */
public class KeyRenameTransformer implements PatchTransformer {

    /**
     * Default constructor
     */
    public KeyRenameTransformer() {
    }

    @Override
    public void transform(final Map<String, String> options, final ConfigurationSection config) throws PatchException {
        final String oldKey = options.get("oldKey");
        final String newKey = options.get("newKey");

        final Object value = config.get(oldKey);
        if (value == null) {
            throw new PatchException("Key '" + oldKey + "' was not set, skipping transformation to '" + newKey + "'.");
        }
        config.set(oldKey, null);
        config.set(newKey, value);
    }
}

