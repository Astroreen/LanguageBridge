package me.astroreen.languagebridge.compatibility.placeholderapi;

import me.astroreen.languagebridge.LanguageBridge;
import me.astroreen.languagebridge.PlaceholderManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class BRPlaceholder extends PlaceholderExpansion {

    private final LanguageBridge plugin = LanguageBridge.getInstance();

    /**
     * Persist through reloads
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist() {
        return true;
    }

    /**
     * We can always register
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister() {
        return true;
    }

    /**
     * The identifier for PlaceHolderAPI to link to this expansion
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public @NotNull String getIdentifier() {
        return PlaceholderManager.getPREFIX();
    }

    /**
     * Name of person who created the expansion
     *
     * @return The color of the author as a String.
     */
    @Override
    public @NotNull String getAuthor() {
        return plugin.getPluginMeta().getAuthors().toString();
    }

    /**
     * Version of the expansion
     *
     * @return The version as a String.
     */
    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    /**
     * A placeholder request has occurred and needs a value
     *
     * @param player A {@link OfflinePlayer OfflinePlayer}.
     * @param params A Placeholder.
     * @return possibly-null String of the requested params.
     */
    //todo: parse placeholders using PlaceholderManager
    @Override
    public String onRequest(final OfflinePlayer player, final @NotNull String params) {
        if(player == null) return "";
        return "";
    }
}
