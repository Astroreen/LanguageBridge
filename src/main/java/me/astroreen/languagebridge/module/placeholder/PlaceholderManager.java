package me.astroreen.languagebridge.module.placeholder;

import lombok.CustomLog;
import me.astroreen.languagebridge.LanguageBridge;
import me.astroreen.languagebridge.config.Config;
import me.astroreen.languagebridge.database.Connector;
import me.astroreen.languagebridge.database.QueryType;
import me.astroreen.languagebridge.module.config.ConfigAccessor;
import me.astroreen.languagebridge.module.config.ConfigurationFile;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//todo: on reload check languages in config and in database, if language don't exist, replace it with default value from config
@CustomLog
public class PlaceholderManager {
    private static final String FILE_SUFFIX = "-messages.yml";
    private static final String PREFIX = "lngbr_";
    private static final Pattern PATTERN = Pattern.compile("%" + PREFIX + ".*?%", Pattern.CASE_INSENSITIVE);
    private final LanguageBridge plugin;
    private final Connector connector;
    private boolean storePlaceholders = false;
    //key, language, value
    private final Map<String, Map<String, String>> placeholders = new TreeMap<>();

    public PlaceholderManager(final @NotNull LanguageBridge plugin) {
        this.plugin = plugin;
        this.connector = new Connector(plugin.getDatabase());
        reload(); // as setup
    }

    public void reload(){
        placeholders.clear();

        final ConfigurationFile config = plugin.getPluginConfig();
        this.storePlaceholders = config.getBoolean("settings.store-keys-in-memory", false);

        if(storePlaceholders) uploadPlaceholdersToMemory();
    }

    public TextComponent translate(final Player player, final String text) {
        //no placeholder
        if(!hasPlaceholder(text)) return Config.parseText(text);

        //todo: create patterns for others placeholders values and add this as a method variable to dynamically get answer
        final Matcher matcher = PATTERN.matcher(text);
        final StringBuilder builder = new StringBuilder();
        while(matcher.find()){
            final String matchedText = matcher.group(1);
            final Optional<String> language = getPlayerLanguage(player.getUniqueId());

            final Optional<String> value = getPlaceholderValue(
                    language.orElseGet(() -> //get default value
                            plugin.getPluginConfig().getString("settings.default-language", "en")),
                    matchedText);

            value.ifPresent(s -> matcher.appendReplacement(builder, s));
            matcher.appendTail(builder);
        }
        return Config.parseText(builder.toString());
    }

    public @NotNull Optional<String> getPlayerLanguage(final UUID uuid){
        try {
            final ResultSet rs = connector.querySQL(QueryType.SELECT_PLAYER_LANGUAGE, String.valueOf(uuid));
            rs.next();
            return Optional.ofNullable(rs.getString("language"));
        } catch (SQLException e) {
            LOG.error("There was an exception with SQL", e);
            return Optional.empty(); //default
        }
    }

    public @NotNull Optional<String> getPlaceholderValue(final String language, final String key){
        //check if language is real
        if(!Config.getLanguages().contains(language)) return Optional.empty();
        //check if key is really a placeholder
        if(!hasPlaceholder(key)) return Optional.empty();

        final String value;
        if(storePlaceholders) {
            final Map<String, String> options = placeholders.get(key);
            if (options == null) return Optional.empty();

            value = options.get(language);
        } else {
            try {
                final ConfigAccessor accessor = ConfigAccessor.create(new File(plugin.getDataFolder(), language + FILE_SUFFIX));
                value = accessor.getConfig().getString(key);
            } catch (InvalidConfigurationException | FileNotFoundException e) {
                LOG.warn("Could not open '" + language + FILE_SUFFIX + "' file. Reason: " + e.getMessage(), e);
                return Optional.empty();
            }
        }

        return value == null ? Optional.empty() : Optional.of(value);
    }

    public void uploadPlaceholdersToMemory() {
        final Set<String> keys = new HashSet<>();

        for(final String language: Config.getLanguages()) {
            if(!isLanguageFileExist(language)) {
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
        if(keys == null || keys.isEmpty() || accessor == null) return;

        final String fileName = accessor.getConfigurationFile().getName();
        final String language = fileName.substring(0, fileName.length() - FILE_SUFFIX.length());

        for(final String key : keys){
            if(!placeholders.containsKey(key)) {
                LOG.warn("Key '" + key + "' was not found in '" + fileName + "' file!");
                continue;
            }
            final String value = accessor.getConfig().getString(key);

            Map<String, String> options = placeholders.get(key);
            if(options == null) {
                options = new HashMap<>();
            }

            options.put(language, value);
            placeholders.put(key, options);
        }
    }

    public boolean hasPlaceholder(final String text){
        if(text == null) return false;
        return PATTERN.matcher(text).find();
    }

    public boolean isLanguageFileExist(final String language) {
        if(language == null) return false;
        return new File(plugin.getDataFolder(), language + FILE_SUFFIX).exists();
    }
}
