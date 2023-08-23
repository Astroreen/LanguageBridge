package me.astroreen.languagebridge.commands;

import lombok.CustomLog;
import me.astroreen.astrolibs.api.bukkit.command.SimpleTabCompleter;
import me.astroreen.astrolibs.api.config.ConfigurationFile;
import me.astroreen.astrolibs.module.logger.DebugHandlerConfig;
import me.astroreen.languagebridge.LanguageBridge;
import me.astroreen.languagebridge.config.MessageType;
import me.astroreen.languagebridge.config.Config;
import me.astroreen.languagebridge.permissions.Permission;
import me.astroreen.languagebridge.permissions.PermissionManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

@CustomLog
public class LanguageBridgeCommand implements CommandExecutor, SimpleTabCompleter {

    //todo: translate commands and their outputs
    //todo: make help command and hints for usage

    private static final LanguageBridge instance = LanguageBridge.getInstance();
    private static final PermissionManager permManager = LanguageBridge.getInstance().getPermissionManager();

    public LanguageBridgeCommand() {
        final PluginCommand command = instance.getCommand("languagebridge");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command cmd, final @NotNull String label, final @NotNull String[] args) {
        if ("languagebridge".equalsIgnoreCase(cmd.getName())) {
            LOG.debug("Executing /" + cmd.getName() + " command for user " + sender.getName()
                    + " with arguments: " + Arrays.toString(args));
            // if the command is empty, display help message
            if (args.length == 0) return true;

            switch (args[0].toLowerCase()) {
                case "default-language","language", "lang", "l" -> handleLanguage(sender, args);
                case "version", "ver", "v" ->
                        Config.sendMessage(sender, MessageType.PLUGIN_VERSION, instance.getPluginMeta().getVersion());
                case "debug", "d" -> handleDebug(sender, args);
                case "reload", "rl", "r" -> {
                    if (Config.noPermission(sender, Permission.RELOAD)) return false;
                    //just reloading
                    instance.reload();
                    Config.sendMessage(sender, MessageType.RELOAD);
                }
                default -> Config.sendMessage(sender, MessageType.UNKNOWN_ARGUMENTS, args[0]);
            }
            LOG.debug("Command executing done");
            return true;
        }
        return false;
    }

    @Override
    public Optional<List<String>> simpleTabComplete(final @NotNull CommandSender sender, final @NotNull Command command,
                                                    final @NotNull String alias, final String @NotNull ... args) {
        if (args.length == 1) {
            return Optional.of(Arrays.asList("default-language", "version", "reload", "debug"));
        }
        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "default-language" -> completeLanguage(args);
            case "debug" -> completeDebug(args);
            default -> Optional.empty();
        };
    }

    private void handleLanguage(final CommandSender sender, final String @NotNull ... args) {
        if (Config.noPermission(sender, Permission.CHANGE_DEFAULT_LANGUAGE)) return;
        if (args.length == 1) {
            Config.sendMessage(sender, MessageType.CURRENT_LANGUAGE, Config.getDefaultLanguage());
            return;
        }


        if(!Config.getLanguages().contains(args[1])) {
            Config.sendMessage(sender, MessageType.NO_SUCH_LANGUAGE, args[1]);
            return;
        }

        final String language = Config.getDefaultLanguage();
        if (args.length == 2) {
            if (language.equalsIgnoreCase(args[1])) {
                Config.sendMessage(sender, MessageType.LANGUAGE_ALREADY_SET, language);
                return;
            }

            try {
                Config.setDefaultLanguage(args[1]);
            } catch (final IllegalArgumentException e) {
                Config.sendMessage(sender, MessageType.NO_SUCH_LANGUAGE);
                return;
            }
            final ConfigurationFile config = instance.getPluginConfig();
            config.set("settings.default-language", args[1]);
            try {
                config.save();
            } catch (IOException e) {
                LOG.warn("Failed to save new default language option to config file!", e);
            }
            Config.sendMessage(sender, MessageType.LANGUAGE_SET, args[1]);
            return;
        }
        Config.sendMessage(sender, MessageType.UNKNOWN_ARGUMENTS, args);
    }

    private @NotNull Optional<List<String>> completeLanguage(final String @NotNull ... args) {
        if (args.length == 2) {
            return Optional.of(Config.getLanguages().stream().toList());
        }
        return Optional.of(Collections.emptyList());
    }

    private void handleDebug(final CommandSender sender, final String @NotNull ... args) {
        if (Config.noPermission(sender, Permission.DEBUG)) return;
        if (args.length == 1) {
            Config.sendMessage(sender, MessageType.CURRENT_DEBUG_STATE,
                    DebugHandlerConfig.isDebugging() ?
                            Config.getMessage(MessageType.ENABLED) :
                            Config.getMessage(MessageType.DISABLED));
            return;
        }

        final Boolean input = args[1].equalsIgnoreCase("enable") ? Boolean.TRUE
                : args[1].equalsIgnoreCase("disable") ? Boolean.FALSE : null;
        if (input != null && args.length == 2) {

            if (DebugHandlerConfig.isDebugging() && input || !DebugHandlerConfig.isDebugging() && !input) {
                Config.sendMessage(sender, MessageType.DEBUG_ALREADY_SET,
                        DebugHandlerConfig.isDebugging() ?
                                Config.getMessage(MessageType.ENABLED) :
                                Config.getMessage(MessageType.DISABLED));
                return;
            }

            try {
                DebugHandlerConfig.setDebugging(input);
            } catch (final IOException e) {
                Config.sendMessage(sender, MessageType.DEBUG_ERROR);
                LOG.warn("Could not save new debugging state to configuration file! " + e.getMessage(), e);
            }
            Config.sendMessage(sender, MessageType.DEBUG_SET,
                    DebugHandlerConfig.isDebugging() ?
                            Config.getMessage(MessageType.ENABLED) :
                            Config.getMessage(MessageType.DISABLED));
            return;
        }
        Config.sendMessage(sender, MessageType.UNKNOWN_ARGUMENTS, args);
    }

    private @NotNull Optional<List<String>> completeDebug(final String @NotNull ... args) {
        if (args.length == 2) {
            return Optional.of(Arrays.asList("enable", "disable"));
        }
        return Optional.of(new ArrayList<>());
    }
}
