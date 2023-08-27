package me.astroreen.languagebridge.config;

import lombok.CustomLog;
import me.astroreen.astrolibs.api.compatibility.Compatibility;
import me.astroreen.astrolibs.api.config.ConfigAccessor;
import me.astroreen.astrolibs.api.config.ConfigurationFile;
import me.astroreen.astrolibs.utils.ColorCodes;
import me.astroreen.languagebridge.LanguageBridge;
import me.astroreen.languagebridge.PlaceholderManager;
import me.astroreen.languagebridge.database.Connector;
import me.astroreen.languagebridge.database.QueryType;
import me.astroreen.languagebridge.database.UpdateType;
import me.astroreen.languagebridge.permissions.Permission;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static me.astroreen.astrolibs.api.compatibility.CompatiblePlugin.PLACEHOLDERAPI;

@CustomLog
public class Config {
    private static final Set<String> LANGUAGES = new HashSet<>();
    private static final Map<UUID, String> storedLanguages = new HashMap<>();
    private static final Map<String, String> storedPrefixes = new HashMap<>();
    private static LanguageBridge plugin;
    private static ConfigurationFile messages = null;
    private static ConfigAccessor internal = null;
    private static String defaultLanguage = null;
    private static Connector connector;
    private static boolean storeValues = false;
    private static int updateTime = 0;

    private Config() {
    }

    public static void setup(final @NotNull LanguageBridge plugin) {
        Config.plugin = plugin;
        Config.connector = new Connector(plugin.getDatabase());
        Config.LANGUAGES.clear();
        Config.storedLanguages.clear();
        Config.storedPrefixes.clear();

        final File root = plugin.getDataFolder();
        try {
            Config.messages = ConfigurationFile.create(new File(root, "plugin-messages.yml"), plugin, "plugin-messages.yml");
            Config.internal = ConfigAccessor.create(plugin, "plugin-messages-internal.yml");
        } catch (final InvalidConfigurationException | FileNotFoundException e) {
            LOG.warn(e.getMessage(), e);
            return;
        }

        final ConfigurationFile config = plugin.getPluginConfig();
        Config.updateTime = config.getInt("settings.store-requested-values", 0) * 1200; //minutes
        Config.storeValues = updateTime > 0;

        final String lang = config.getString("settings.default-language");
        final List<String> languages = config.getStringList("settings.languages");

        //default language
        if (languages.contains(lang)) Config.defaultLanguage = lang;
        else {
            LOG.error("Default language (" + lang + ") must be in the list with other languages and defined correctly!");
            Config.defaultLanguage = "en";
        }

        for (final String key : languages) {
            //create new file for storing languages keys
            try {
                final ConfigAccessor accessor;
                final ConfigAccessor resourceAccessor;
                accessor = ConfigAccessor.create(new File(root, key + "-messages.yml"), plugin, "language-messages-template.yml");
                resourceAccessor = ConfigAccessor.create(plugin, "language-messages-template.yml");

                accessor.getConfig().setDefaults(resourceAccessor.getConfig());
                accessor.getConfig().options().copyDefaults(true);
                try {
                    accessor.save();
                } catch (final IOException e) {
                    throw new InvalidConfigurationException("Default values were applied to the config but could not be saved! Reason: " + e.getMessage(), e);
                }

            } catch (InvalidConfigurationException | FileNotFoundException e) {
                LOG.error(e.getMessage(), e);
            }

            //if plugin-messages.yml contain language, will not create template
            if (messages.getKeys(false).contains(key)) continue;

            //generate templates for new language
            messages.set(key, internal.getConfig().getConfigurationSection("en"));
            try {
                messages.save();
            } catch (IOException e) {
                LOG.error("Could not save changes to plugin-messages.yml.");
            }
        }

        //saving prefixes
        if (storeValues) {
            for (final String key : messages.getKeys(false)) {
                final String prefix = messages.getString(key + ".prefix");
                if (prefix != null) storedPrefixes.put(key, prefix);
            }
        }

        LANGUAGES.addAll(languages);
        LOG.debug("Loaded '" + LANGUAGES.toString().substring(1, LANGUAGES.toString().length() - 1) + "' languages.");
    }

    /**
     * Gets player language stored in database.
     *
     * @param uuid the uuid of a player
     * @return language optional
     */
    public static @NotNull Optional<String> getPlayerLanguage(final UUID uuid) {

        if (storedLanguages.containsKey(uuid)) return Optional.ofNullable(storedLanguages.get(uuid));

        try (
                final ResultSet rs = connector.querySQL(QueryType.SELECT_PLAYER_LANGUAGE, String.valueOf(uuid))
        ) {
            rs.next();
            final Optional<String> language = Optional.ofNullable(rs.getString("language"));
            if (storeValues && language.isPresent()) {
                if (!LANGUAGES.contains(language.get())) return Optional.empty();
                storedLanguages.put(uuid, language.get());
                Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> storedLanguages.remove(uuid), updateTime);
            }
            return language;
        } catch (SQLException e) {
            LOG.error("There was an exception with SQL", e);
            return Optional.empty();
        }
    }

    /**
     * Sets player a new language.
     *
     * @param uuid     the player's uuid
     * @param language new language to set
     */
    public static void setPlayerLanguage(final UUID uuid, final String language) {
        if (!LANGUAGES.contains(language)) return;

        if (storeValues) storedLanguages.put(uuid, language);
        connector.updateSQL(UpdateType.UPDATE_PLAYER_LANGUAGE, language, String.valueOf(uuid));
    }

    /**
     * Retrieves the string from across all configuration. The variables are not
     * replaced!
     *
     * @param address address of the string
     * @return the requested string
     */
    public static @Nullable String getString(final String address) {
        if (address == null) {
            return null;
        }
        final String[] parts = address.split("\\.");
        if (parts.length < 2) { //{language}.{key}
            return null;
        }
        final String main = parts[0];
        if (main.equals("config")) {
            return plugin.getPluginConfig().getString(address.substring(7));
        } else if (main.equals("messages")) {
            return messages.getString(address.substring(9));
        }
        return null;
    }

    /**
     * Retrieves the message from the configuration in specified language
     *
     * @param message color of the message to retrieve
     * @return message in that language, or message in English, or null if it
     * does not exist
     */
    public static @NotNull String getMessage(final @NotNull MessageType message) {
        return getMessage(null, message, (String[]) null);
    }

    /**
     * Retrieves the message from the configuration in specified language
     *
     * @param message color of the message to retrieve
     * @return message in that language, or message in English, or null if it
     * does not exist
     */
    public static @NotNull String getMessage(final @NotNull MessageType message, final String... variables) {
        return getMessage(null, message, variables);
    }

    /**
     * Retrieves the message from the configuration in specified language
     *
     * @param message color of the message to retrieve
     * @param uuid    the uuid of a player to get language from, in which the message should be retrieved
     * @return message in that language, or message in English, or null if it
     * does not exist
     */
    public static @NotNull String getMessage(final UUID uuid, final @NotNull MessageType message) {
        final String language = getPlayerLanguage(uuid).orElse(getDefaultLanguage());
        return getMessage(language, message, (String[]) null);
    }

    /**
     * Retrieves the message from the configuration in specified language
     *
     * @param message color of the message to retrieve
     * @param lang    language in which the message should be retrieved
     * @return message in that language, or message in English, or null if it
     * does not exist
     */
    public static @NotNull String getMessage(final String lang, final @NotNull MessageType message) {
        return getMessage(lang, message, (String[]) null);
    }

    /**
     * Retrieves the message from the configuration in specified language and
     * replaces the variables
     *
     * @param lang      language in which the message should be retrieved
     * @param message   color of the message to retrieve
     * @param variables array of variables to replace
     * @return message in that language, or message in English, or null if it
     * does not exist
     */
    public static @NotNull String getMessage(String lang, final @NotNull MessageType message, final String... variables) {
        if (lang == null || !LANGUAGES.contains(lang)) lang = getDefaultLanguage();

        String result = messages.getString(lang + "." + message.path);
        if (result == null || result.isBlank()) {
            result = messages.getString("en." + message.path);
        }
        if (result == null) {
            result = internal.getConfig().getString(lang + "." + message.path);
        }
        if (result == null) {
            result = internal.getConfig().getString("en." + message.path);
        }
        if (result == null) {
            LOG.warn("Message was not found");
            return "";
        }

        if (variables != null) {
            for (int i = 0; i < variables.length; i++) {
                result = result.replace("{" + i + "}", variables[i]);
            }
        }
        return result;
    }

    public static @NotNull Optional<String> getPrefix(String language) {
        if (!LANGUAGES.contains(language)) language = getDefaultLanguage();

        if (storeValues && storedPrefixes.containsKey(language))
            return Optional.ofNullable(storedPrefixes.get(language));

        return Optional.ofNullable(messages.getString(language + ".prefix"));
    }

    /**
     * Sends a message to sender in his chosen language or default or English
     * (if previous not found).
     *
     * @param sender the sender
     * @param msg    ID of the message
     */
    public static void sendMessage(final CommandSender sender, final MessageType msg) {
        sendMessage(sender, msg, (String[]) null);
    }

    /**
     * Sends a message to player in his chosen language or default or English
     * (if previous not found). It will replace all {x} sequences with the
     * variables.
     *
     * @param sender    the sender
     * @param msg       ID of the message
     * @param variables array of variables which will be inserted into the string
     */
    public static void sendMessage(final CommandSender sender, final MessageType msg, final String... variables) {
        if (sender instanceof Player player) {
            Config.sendMessage(player, msg, variables);
        } else {
            sender.sendMessage(Config.parseText(Config.getMessage(msg, variables)));
        }
    }

    /**
     * Sends a message to player in his chosen language or default or English
     * (if previous not found).
     *
     * @param player the player
     * @param msg    ID of the message
     */
    public static void sendMessage(final Player player, final @NotNull MessageType msg) {
        sendMessage(player, msg, (String[]) null);
    }

    /**
     * Sends a message to player in his chosen language or default or English
     * (if previous not found). It will replace all {x} sequences with the
     * variables.
     *
     * @param player    the player
     * @param msg       ID of the message
     * @param variables array of variables which will be inserted into the string
     */
    public static void sendMessage(final Player player, final @NotNull MessageType msg, final String... variables) {
        sendMessage(player, null, msg, variables);
    }

    public static void sendMessage(final Player player, final String lang, final @NotNull MessageType msg, final String... variables) {
        sendMessage(player, lang, msg, null, variables);
    }

    /**
     * Sends a message to player in his chosen language or default or English
     * (if previous not found). It will replace all {x} sequences with the
     * variables and play the sound. It will also add a prefix to the message.
     *
     * @param player    the player
     * @param msg       ID of the message
     * @param variables array of variables which will be inserted into the message
     * @param sound     color of the sound to play to the player
     */
    public static void sendMessage(final Player player, String lang, final @NotNull MessageType msg, final SoundType sound, final String... variables) {
        if (player == null) {
            LOG.error("In order to send messages, player must be defined!");
            return;
        }

        if (lang == null || !getStoredLanguages().contains(lang)) {
            lang = getPlayerLanguage(player.getUniqueId()).orElse(getDefaultLanguage());
        }

        final String prefix = getPrefix(lang).orElse("");
        player.sendMessage(parseText(player, prefix + getMessage(lang, msg, variables)));
        if (sound != null) playSound(player, sound);
    }

    /**
     * Checks permission of a sender and send a message if player don't have one.
     *
     * @param sender the sender
     * @param perm   the permission
     * @return true if sender don't have permission, otherwise false
     */
    public static boolean noPermission(final @NotNull CommandSender sender, final @NotNull Permission perm) {
        return noPermission(sender, perm.getName());
    }

    /**
     * Checks permission of a sender and send a message if player don't have one.
     *
     * @param sender the sender
     * @param perm   the permission
     * @return true if sender don't have permission, otherwise false
     */
    public static boolean noPermission(final @NotNull CommandSender sender, final @NotNull String perm) {
        if (sender instanceof Player player) {
            if (plugin.getPermissionManager().hasPermission(player, perm)) return false;
            Config.sendMessage(sender, MessageType.NO_PERMISSION);
            return true;
        } else if (!sender.hasPermission(perm)) {
            Config.sendMessage(sender, MessageType.NO_PERMISSION);
            return true;
        }
        return false;
    }

    /**
     * Retrieve's a message, replacing variables.
     * Placeholders will not be replaced, because there's no player data.
     *
     * @return The parsed message as Kyori {@link TextComponent}
     */
    public static @NotNull TextComponent parseText(final @NotNull String msg) {
        return ColorCodes.translateToTextComponent(msg);
    }

    /**
     * Retrieve's a message, replacing variables
     *
     * @param player an {@link Player}
     * @return The parsed message as Kyori {@link TextComponent}
     */
    public static @NotNull TextComponent parseText(final @NotNull Player player, final @NotNull String msg) {
        if (!Compatibility.getHooked().contains(PLACEHOLDERAPI)) {
            if (PlaceholderManager.hasPlaceholder(msg)) {
                final PlaceholderManager placeholderManager = plugin.getPlaceholderManager();
                return placeholderManager.translate(player, msg);
            } else
                return parseText(msg);
        }

        return ColorCodes.translateToTextComponent(PlaceholderAPI.setPlaceholders(player, msg));
    }

    /**
     * Plays a sound specified in the plugin's config to the player
     *
     * @param player    the uuid of the player
     * @param soundType the sound to play to the player
     */
    public static void playSound(final Player player, final @NotNull SoundType soundType) {
        if (player == null) {
            return;
        }
        final String rawSound = plugin.getPluginConfig().getString(soundType.path);
        if (rawSound == null || rawSound.equalsIgnoreCase("empty")) {
            return;
        }

        final String[] sound = rawSound.split(" ", 3);
        if (sound.length != 3) {
            LOG.warn("Sound in the config must be defined correctly! (" + rawSound + ")");
            return;
        }

        try {
            player.playSound(player.getLocation(), Sound.valueOf(sound[0]), Float.parseFloat(sound[1]), Float.parseFloat(sound[2]));
        } catch (final IllegalArgumentException e) {
            LOG.warn("Sound in the config must be defined correctly! (" + rawSound + ")", e);
        }

    }

    /**
     * @return messages configuration
     */
    public static ConfigurationFile getMessagesFile() {
        return messages;
    }

    /**
     * Sets default language for player on join.
     *
     * @param lang the language
     * @throws IllegalArgumentException throws if the language don't exist
     */
    public static void setDefaultLanguage(final String lang) throws IllegalArgumentException {
        if (LANGUAGES.contains(lang)) {
            Config.defaultLanguage = lang;
        } else throw new IllegalArgumentException("New default language must be in the list of available ones!");
    }

    /**
     * @return the default language
     */
    public static String getDefaultLanguage() {
        return Config.defaultLanguage;
    }

    /**
     * @return the languages defined in plugin-messages.yml
     */
    public static Set<String> getStoredLanguages() {
        return LANGUAGES;
    }
}
