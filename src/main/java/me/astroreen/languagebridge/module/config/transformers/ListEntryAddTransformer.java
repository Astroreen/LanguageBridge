package me.astroreen.languagebridge.module.config.transformers;

import me.astroreen.languagebridge.exception.PatchException;
import me.astroreen.languagebridge.module.config.patcher.PatchTransformer;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adds an entry to the given list at the given position.
 */

public class ListEntryAddTransformer implements PatchTransformer {

    /**
     * Default constructor
     */
    public ListEntryAddTransformer() {
    }

    @Override
    public void transform(final Map<String, String> options, final ConfigurationSection config) throws PatchException {
        final String key = options.get("key");
        final String entry = options.get("entry");
        final String position = options.getOrDefault("position", "LAST");

        final List<String> list = config.getStringList(key);
        final boolean listExists = config.isList(key);

        final int index;
        switch (position.toUpperCase(Locale.ROOT)) {
            case "FIRST" -> index = 0;
            case "LAST" -> index = list.size();
            default -> index = list.size();
        }

        list.add(index, entry);
        config.set(key, list);

        if (!listExists) {
            throw new PatchException("List '" + key + "' did not exist, so it was created.");
        }
    }
}

