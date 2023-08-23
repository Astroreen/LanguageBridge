package me.astroreen.languagebridge;

import lombok.CustomLog;
import lombok.Getter;
import me.astroreen.astrolibs.api.config.ConfigAccessor;
import me.astroreen.astrolibs.api.config.ConfigurationFile;
import me.astroreen.languagebridge.config.Config;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//todo: on reload check languages in config and in database, if language don't exist, replace it with default value from config
@CustomLog
public class PlaceholderManager {
    private static final String SYMBOL = "%";
    /**
     * Gets prefix that is used in placeholder pattern.
     *
     * @return prefix
     */
    @Getter
    private static final String PREFIX = "lngbr_";
    /**
     * Gets placeholder pattern.
     *
     * @return placeholder
     */
    @Getter
    private static final Pattern PATTERN = Pattern.compile(SYMBOL + PREFIX + ".*?" + SYMBOL, Pattern.CASE_INSENSITIVE);
    private static final Pattern EMPTY_PATTERN = Pattern.compile(SYMBOL + PREFIX + SYMBOL, Pattern.CASE_INSENSITIVE);
    private static final String FILE_SUFFIX = "-messages.yml";
    private final LanguageBridge plugin;
    //key, language, value
    private final Map<String, Map<String, String>> placeholders = new TreeMap<>();
    private boolean storePlaceholders = false;
    public PlaceholderManager(final @NotNull LanguageBridge plugin) {
        this.plugin = plugin;
        reload(); // as setup
    }

    /**
     * Reloads {@link PlaceholderManager}
     */
    public void reload() {
        placeholders.clear();

        final ConfigurationFile config = plugin.getPluginConfig();
        this.storePlaceholders = config.getBoolean("settings.store-keys-in-memory", false);

        if (storePlaceholders) uploadPlaceholdersToMemory();
    }

    /**
     * Translates all placeholders and colors in text.
     *
     * @param player the player for whom translating
     * @param text   the text to translate
     * @return {@link TextComponent}
     */
    public TextComponent translate(final Player player, final String text) {
        //no placeholder
        if (!hasPlaceholder(text)) return Config.parseText(player, text);

        //todo: create patterns for others placeholders values and add this as a method variable to dynamically get answer
        final Matcher matcher = PATTERN.matcher(text);
        final StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            final String matchedText = matcher.group();
            final Optional<String> language = Config.getPlayerLanguage(player.getUniqueId());

            final Optional<String> value = getValueFromKey(
                    language.orElseGet(() -> //get default value
                            plugin.getPluginConfig().getString("settings.default-language", "en")),
                    getKeyFromPlaceholder(matchedText).orElse(matchedText));

            value.ifPresent(s -> matcher.appendReplacement(builder, s));
            matcher.appendTail(builder);
        }
        return Config.parseText(builder.toString());
    }

    /**
     * Get placeholder value.
     *
     * @param uuid the uuid of a player to get language from
     * @param key  key in placeholder
     * @return value or empty optional
     */
    public @NotNull Optional<String> getValueFromKey(final UUID uuid, final String key) {
        return getValueFromKey(Config.getPlayerLanguage(uuid).orElse(Config.getDefaultLanguage()), key);
    }

    /**
     * Get key value.
     *
     * @param language language
     * @param key      key in key
     * @return value or empty optional
     */
    public @NotNull Optional<String> getValueFromKey(final String language, final String key) {
        //check if language is real
        if (!Config.getLanguages().contains(language)) return Optional.empty();
        //check key
        if (key == null || key.isBlank()) return Optional.empty();
        if (!hasPlaceholder(SYMBOL + PREFIX + key + SYMBOL)) return Optional.empty();

        if (storePlaceholders) {
            final Map<String, String> options = placeholders.get(key);
            if (options != null) return Optional.ofNullable(options.get(language));
        }

        try {
            final ConfigAccessor accessor = ConfigAccessor.create(new File(plugin.getDataFolder(), language + FILE_SUFFIX));
            return Optional.ofNullable(accessor.getConfig().getString(key));
        } catch (InvalidConfigurationException | FileNotFoundException e) {
            LOG.warn("Could not open '" + language + FILE_SUFFIX + "' file. Reason: " + e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Copy all placeholders and their values to HashMap.
     */
    public void uploadPlaceholdersToMemory() {
        final Set<String> keys = new HashSet<>();

        for (final String language : Config.getLanguages()) {
            if (!isLanguageFileExist(language)) {
                LOG.warn("Language file '" + language + FILE_SUFFIX + "' was not found!"
                        + " Keys from this file will not be uploaded to the memory.");
                continue;
            }

            final File root = plugin.getDataFolder();
            final ConfigAccessor accessor;
            try {
                accessor = ConfigAccessor.create(new File(root, language + FILE_SUFFIX));
            } catch (InvalidConfigurationException | FileNotFoundException e) {
                LOG.error("Could not create ConfigAccessor for '" + language
                        + FILE_SUFFIX + "'. Reason: " + e.getMessage(), e);
                continue;
            }


            keys.addAll(accessor.getConfig().getKeys(true));
            createPlaceholders(accessor, keys);
        }
    }

    private void createPlaceholders(final ConfigAccessor accessor, final Set<String> keys) {
        if (keys == null || keys.isEmpty() || accessor == null) return;

        final String fileName = accessor.getConfigurationFile().getName();
        final String language = fileName.substring(0, fileName.length() - FILE_SUFFIX.length());

        for (final String key : keys) {
            final String value = accessor.getConfig().getString(key);
            if (value == null) {
                LOG.warn("Key '" + key + "' was not found in '" + fileName + "' file!");
                continue;
            }

            Map<String, String> options = placeholders.get(key);
            if (options == null) {
                options = new HashMap<>();
            }

            options.put(language, value);
            placeholders.put(key, options);
        }
    }

    /**
     * Checks if key exist.
     *
     * @param key      key to check
     * @return true if exists, otherwise false
     */
    public boolean isKeyExist(final String key){
        return isKeyExist(Config.getDefaultLanguage(), key);
    }

    /**
     * Checks if key exist.
     *
     * @param language from which language file take key from
     * @param key      key to check
     * @return true if exists, otherwise false
     */
    public boolean isKeyExist(final String language, final String key) {
        if (key == null || key.isBlank() || language == null || language.isBlank()) return false;
        if (storePlaceholders) return placeholders.containsKey(key);
        try {
            final ConfigAccessor accessor = ConfigAccessor.create(new File(plugin.getDataFolder(), language + FILE_SUFFIX));
            return accessor.getConfig().isSet(key);
        } catch (InvalidConfigurationException | FileNotFoundException e) {
            LOG.warn("Could not open '" + language + FILE_SUFFIX + "' file. Reason: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Checks if language file exist in plugin folder.
     *
     * @param language language
     * @return true if it does, otherwise false
     */
    public boolean isLanguageFileExist(final String language) {
        if (language == null) return false;
        return new File(plugin.getDataFolder(), language + FILE_SUFFIX).exists();
    }

    //////////////////
    ///   Static   ///
    //////////////////

    /**
     * Gets first placeholder from text if one exist.
     *
     * @param text text
     * @return placeholder or empty optional
     */
    public static Optional<String> getPlaceholderFromText(final String text) {
        if (text == null || text.isBlank()) return Optional.empty();
        final Matcher matcher = PATTERN.matcher(text);

        if (matcher.find()) return Optional.ofNullable(matcher.group());

        return Optional.empty();
    }

    /**
     * Gets key from placeholder.
     *
     * @param placeholder placeholder
     * @return key or empty optional
     */
    public static Optional<String> getKeyFromPlaceholder(final String placeholder) {
        if (!hasPlaceholder(placeholder)) return Optional.empty();

        final String result = placeholder.substring(
                SYMBOL.length() + PREFIX.length(),
                placeholder.length() - SYMBOL.length());

        if (result.isBlank()) return Optional.empty();

        return Optional.of(result);
    }

    /**
     * Gets key from placeholder in text if one exist.
     *
     * @param text text
     * @return key or empty optional
     */
    public static Optional<String> getKeyFromText(final String text) {
        return getKeyFromPlaceholder(getPlaceholderFromText(text).orElse(null));
    }

    /**
     * Checks text for placeholder.
     *
     * @param text text
     * @return true if exists, else false
     */
    public static boolean hasPlaceholder(final String text) {
        if (text == null || text.isBlank() || EMPTY_PATTERN.matcher(text).find()) return false;
        return PATTERN.matcher(text).find();
    }
}
