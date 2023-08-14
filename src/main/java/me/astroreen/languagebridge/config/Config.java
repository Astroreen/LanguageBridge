package me.astroreen.languagebridge.config;

import me.astroreen.languagebridge.LanguageBridge;
import me.astroreen.languagebridge.MessageType;
import me.astroreen.languagebridge.module.config.ConfigAccessor;
import me.astroreen.languagebridge.module.config.ConfigurationFile;
import me.astroreen.languagebridge.utils.ColorCodes;
import lombok.CustomLog;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

@CustomLog
public class Config {
    private static final List<String> LANGUAGES = new ArrayList<>();
    private static LanguageBridge plugin;
    private static ConfigurationFile messages;
    private static ConfigAccessor internal;
    private static String lang;
    private static String prefix;

    private Config() {
    }

    public static void setup(final @NotNull LanguageBridge plugin) {
        Config.plugin = plugin;
        Config.LANGUAGES.clear();

        final File root = plugin.getDataFolder();
        try {
            Config.messages = ConfigurationFile.create(new File(root, "messages.yml"), plugin, "messages.yml");
            Config.internal = ConfigAccessor.create(plugin, "messages-internal.yml");
        } catch (final InvalidConfigurationException | FileNotFoundException e) {
            LOG.warn(e.getMessage(), e);
            return;
        }

        Config.lang = plugin.getPluginConfig().getString("settings.language");
        for (final String key : messages.getKeys(false)) {
            if (!"global".equals(key)) {
                LOG.debug("Loaded " + key + " language");
                LANGUAGES.add(key);
            }
        }
        Config.prefix = getMessage(lang, MessageType.PREFIX);
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
        if (parts.length < 2) {
            return null;
        }
        final String main = parts[0];
        if ("config".equals(main)) {
            return plugin.getPluginConfig().getString(address.substring(7));
        } else if ("messages".equals(main)) {
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
     * @param lang    language in which the message should be retrieved
     * @return message in that language, or message in English, or null if it
     * does not exist
     */
    public static @NotNull String getMessage(final @NotNull String lang, final @NotNull MessageType message) {
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
        if (lang == null) lang = getLanguage();

        String result = messages.getString(lang + "." + message.path);
        if (result == null) {
            result = messages.getString("en." + message.path);
        }
        if (result == null) {
            result = internal.getConfig().getString(lang + "." + message.path);
        }
        if (result == null) {
            result = internal.getConfig().getString("en." + message.path);
        }
        if (result != null) {
            if (variables != null) {
                for (int i = 0; i < variables.length; i++) {
                    result = result.replace("{" + i + "}", variables[i]);
                }
            }
            return result;
        }
        LOG.warn("Message was not found");
        return "";
    }

    /**
     * Sends a message to player in his chosen language or default or English
     * (if previous not found). It will replace all {x} sequences with the
     * variables.
     *
     * @param player player
     * @param msg    ID of the message
     */
    public static void sendMessage(final Player player, final @NotNull MessageType msg) {
        sendMessage(player, msg, (String[]) null);
    }

    /**
     * Sends a message to player in his chosen language or default or English
     * (if previous not found). It will replace all {x} sequences with the
     * variables and play the sound.
     *
     * @param player    player
     * @param msg       ID of the message
     * @param variables array of variables which will be inserted into the string
     */
    public static void sendMessage(final Player player, final @NotNull MessageType msg, final String... variables) {
        sendMessage(player, msg, null, variables);
    }

    /**
     * Sends a message to player in his chosen language or default or English
     * (if previous not found). It will replace all {x} sequences with the
     * variables and play the sound. It will also add a prefix to the message.
     *
     * @param player    player
     * @param msg       ID of the message
     * @param variables array of variables which will be inserted into the message
     * @param sound     color of the sound to play to the player
     */
    public static void sendMessage(final Player player, final @NotNull MessageType msg, final ConfigSound sound, final String... variables) {
        if (player == null) return;

        player.sendMessage(parseMessage(player, getMessage(msg, variables)));
        if (sound != null) playSound(player, sound);
    }

    /**
     * Retrieve's a message, replacing variables.
     * Placeholders will not be replaced, because there's no player data.
     *
     * @return The parsed message as Kyori {@link TextComponent}
     */
    public static @NotNull TextComponent parseMessage(final @NotNull String msg) {
        return ColorCodes.translateToTextComponent(prefix + msg);
    }

    /**
     * Retrieve's a message, replacing variables
     *
     * @param player an {@link Player}
     * @return The parsed message as Kyori {@link TextComponent}
     */
    public static @NotNull TextComponent parseMessage(final @NotNull Player player, final @NotNull String msg) {
        return ColorCodes.translateToTextComponent(PlaceholderAPI.setPlaceholders(player, prefix + msg));
    }

    /**
     * Retrieve's a message, replacing variables
     *
     * @param player an {@link OfflinePlayer}
     * @return The parsed message as Kyori {@link TextComponent}
     */
    public static @NotNull TextComponent parseMessage(final @NotNull OfflinePlayer player, final @NotNull String msg) {
        return ColorCodes.translateToTextComponent(PlaceholderAPI.setPlaceholders(player, prefix + msg));
    }

    /**
     * Plays a sound specified in the plugin's config to the player
     *
     * @param player    the uuid of the player
     * @param soundType the color of the sound to play to the player
     * @throws IllegalArgumentException if sound color was not found
     */
    public static void playSound(final Player player, final @NotNull ConfigSound soundType)
            throws IllegalArgumentException {
        if (player == null) {
            return;
        }
        final String rawSound = plugin.getPluginConfig().getString(soundType.path);
        if (rawSound != null) {
            final String[] sound = rawSound.split(" ", 3);
            if (sound.length != 3) {
                LOG.error("Sound in the config used wrong: " + rawSound);
                return;
            }
            if (!"false".equalsIgnoreCase(sound[0])) {
                try {
                    player.playSound(player.getLocation(), Sound.valueOf(sound[0]), Float.parseFloat(sound[1]), Float.parseFloat(sound[2]));
                } catch (final IllegalArgumentException e) {
                    LOG.warn("Unknown sound type: " + rawSound, e);
                }
            }
        }
    }

    /**
     * @return messages configuration
     */
    public static ConfigurationFile getMessagesFile() {
        return messages;
    }

    public static void setLanguage (String lang) throws IllegalArgumentException{
        if(LANGUAGES.contains(lang)){
            Config.lang = lang;
        } else throw new IllegalArgumentException();
    }

    /**
     * @return the default language
     */
    public static String getLanguage() {
        return lang;
    }

    /**
     * @return the languages defined in messages.yml
     */
    public static List<String> getLanguages() {
        return LANGUAGES;
    }

    public enum ConfigSound {
        PING("sounds.ping");

        private final String path;

        ConfigSound(String path) {
            this.path = path;
        }
    }
}
