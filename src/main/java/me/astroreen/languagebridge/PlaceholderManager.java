package me.astroreen.languagebridge;

import lombok.CustomLog;
import lombok.Getter;
import me.astroreen.astrolibs.api.config.ConfigAccessor;
import me.astroreen.astrolibs.api.config.ConfigurationFile;
import me.astroreen.languagebridge.config.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CustomLog
public class PlaceholderManager {
    private static final String SYMBOL = "%";
    /**
     * Gets prefix that is used in placeholder pattern.
     */
    @Getter
    private static final String PREFIX = "lngbr_";
    /**
     * Gets placeholder pattern.
     */
    @Getter
    private static final Pattern PATTERN = Pattern.compile(SYMBOL + PREFIX + ".*?" + SYMBOL, Pattern.CASE_INSENSITIVE);
    private static final Pattern EMPTY_PATTERN = Pattern.compile(SYMBOL + PREFIX + SYMBOL, Pattern.CASE_INSENSITIVE);
    private static final String FILE_SUFFIX = "-messages.yml";
    private final LanguageBridge plugin;
    //key, language, value
    private final Map<String, Map<String, String>> placeholders = new TreeMap<>();
    private final Map<UUID, String> languages = new HashMap<>();
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
     * @param player    the player for whom translating
     * @param component the text component to translate
     * @return {@link TextComponent}
     */
    public @NotNull Component translateToComponent(final Player player, Component component) {
        if(player == null || component == null) return Component.empty();

        final PlaceholderManager manager = plugin.getPlaceholderManager();
        String text = PlainTextComponentSerializer.plainText().serialize(component);
        boolean hasPlaceholder = PlaceholderManager.hasPlaceholder(text);

        while(hasPlaceholder){
            final Optional<String> placeholder = PlaceholderManager.getPlaceholderFromText(text);

            //there's must be a placeholder, because we check it in while
            if (placeholder.isEmpty()) {
                continue;
            }

            final Optional<String> key = PlaceholderManager.getKeyFromPlaceholder(placeholder.get());
            //check key
            if(key.isEmpty() || !manager.isKeyExist(key.get())) return component;

            //getting value of the placeholder
            final TextComponent value = Config.parseTextToComponent(player, manager
                    .getValueFromKey(player.getUniqueId(), key.get())
                    .orElse(placeholder.get()));

            //getting name with replacements
            component = component.replaceText(TextReplacementConfig.builder()
                    .match(placeholder.get())
                    .replacement(value)
                    .build());

            text = PlainTextComponentSerializer.plainText().serialize(component);
            hasPlaceholder = PlaceholderManager.hasPlaceholder(text);
        }

        return component;
    }

    /**
     * Translates all placeholders and colors in text.
     *
     * @param player the player for whom translating
     * @param text   the text to translate
     * @return {@link TextComponent}
     */
    public TextComponent translateToComponent(final Player player, final String text) {
        //no placeholder
        if (!hasPlaceholder(text)) return Config.parseTextToComponent(player, text);

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
        return Config.parseTextToComponent(builder.toString());
    }

    /**
     * Translates all placeholders and colors in text.
     *
     * @param player the player for whom translating
     * @param text   the text to translate
     * @return translated text
     */
    public String translate(final Player player, final String text) {
        //no placeholder
        if (!hasPlaceholder(text)) return Config.parseText(player, text);

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
        if (!Config.getStoredLanguages().contains(language)) return Optional.empty();
        //check key
        if (key == null || key.isBlank()) return Optional.empty();
        if (!hasPlaceholder(SYMBOL + PREFIX + key + SYMBOL)) return Optional.empty();

        if (storePlaceholders && placeholders.containsKey(key)) {
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

        for (final String language : Config.getStoredLanguages()) {
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
     * @param key key to check
     * @return true if exists, otherwise false
     */
    public boolean isKeyExist(final String key) {
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
