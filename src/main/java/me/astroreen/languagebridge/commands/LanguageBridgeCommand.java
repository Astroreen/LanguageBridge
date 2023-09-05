package me.astroreen.languagebridge.commands;

import lombok.CustomLog;
import me.astroreen.astrolibs.api.bukkit.command.CommandArgumentNode;
import me.astroreen.astrolibs.api.bukkit.command.CommandArguments;
import me.astroreen.astrolibs.api.bukkit.command.SimpleCommand;
import me.astroreen.astrolibs.api.config.ConfigurationFile;
import me.astroreen.astrolibs.module.logger.DebugHandlerConfig;
import me.astroreen.languagebridge.LanguageBridge;
import me.astroreen.languagebridge.config.Config;
import me.astroreen.languagebridge.config.MessageType;
import me.astroreen.languagebridge.permissions.Permission;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@CustomLog
public class LanguageBridgeCommand extends SimpleCommand {

    //todo: make help command and hints for usage

    private static final LanguageBridge plugin = LanguageBridge.getInstance();

    public LanguageBridgeCommand() {
        super(LanguageBridge.getInstance(), LOG, "languagebridge", Permission.MAIN_COMMAND.getName());
    }

    @Override
    public CommandArguments setupArguments(@NotNull CommandArguments commandArguments) {
        final CommandArgumentNode root = commandArguments.getRoot();

        final CommandArgumentNode language = root.addArgument("default-language");
        Config.getStoredLanguages().forEach(language::addArgument);

        final CommandArgumentNode debug = root.addArgument("debug");
        debug.addArgument("enable");
        debug.addArgument("disable");

        root.addArgument("version");
        root.addArgument("reload");

        return commandArguments;
    }

    @Override
    public void execute() {
        onArgumentSequence("default-language", (sender, args) -> {

            Config.sendMessage(sender, MessageType.CURRENT_LANGUAGE, Config.getDefaultLanguage());
            return true;
        });

        for (final String lang : Config.getStoredLanguages()) {
            onArgumentSequence("default-language " + lang, (sender, args) -> {

                if (Config.noPermission(sender, Permission.CHANGE_DEFAULT_LANGUAGE)) return true;
                final String language = Config.getDefaultLanguage();

                if (language.equalsIgnoreCase(args[1])) {
                    Config.sendMessage(sender, MessageType.LANGUAGE_ALREADY_SET, language);
                    return true;
                }

                try {
                    Config.setDefaultLanguage(args[1]);
                } catch (final IllegalArgumentException e) {
                    Config.sendMessage(sender, MessageType.NO_SUCH_LANGUAGE);
                    return true;
                }
                final ConfigurationFile config = plugin.getPluginConfig();
                config.set("settings.default-language", args[1]);
                try {
                    config.save();
                } catch (IOException e) {
                    LOG.warn("Failed to save new default language option to config file!", e);
                }
                Config.sendMessage(sender, MessageType.LANGUAGE_SET, args[1]);
                return true;

            });
        }

        onArgumentSequence("version", (sender, args) -> {
            Config.sendMessage(sender, MessageType.PLUGIN_VERSION, plugin.getPluginMeta().getVersion());
            return true;
        });

        onArgumentSequence("debug", (sender, args) -> {
            Config.sendMessage(sender, MessageType.CURRENT_DEBUG_STATE,
                    DebugHandlerConfig.isDebugging() ?
                            Config.getMessage(MessageType.ENABLED) :
                            Config.getMessage(MessageType.DISABLED));
            return true;
        });

        onArgumentSequence("debug enable", (sender, args) -> {
            if (Config.noPermission(sender, Permission.DEBUG)) return true;
            if (DebugHandlerConfig.isDebugging()) {
                Config.sendMessage(sender, MessageType.DEBUG_ALREADY_SET, Config.getMessage(MessageType.ENABLED));
                return true;
            }

            try {
                DebugHandlerConfig.setDebugging(true);
            } catch (final IOException e) {
                Config.sendMessage(sender, MessageType.DEBUG_ERROR);
                LOG.warn("Could not save new debugging state to configuration file! " + e.getMessage(), e);
            }
            Config.sendMessage(sender, MessageType.DEBUG_SET, Config.getMessage(MessageType.ENABLED));
            return true;
        });

        onArgumentSequence("debug disable", (sender, args) -> {
            if (Config.noPermission(sender, Permission.DEBUG)) return true;
            if (!DebugHandlerConfig.isDebugging()) {
                Config.sendMessage(sender, MessageType.DEBUG_ALREADY_SET, Config.getMessage(MessageType.DISABLED));
                return true;
            }

            try {
                DebugHandlerConfig.setDebugging(false);
            } catch (final IOException e) {
                Config.sendMessage(sender, MessageType.DEBUG_ERROR);
                LOG.warn("Could not save new debugging state to configuration file! " + e.getMessage(), e);
            }
            Config.sendMessage(sender, MessageType.DEBUG_SET, Config.getMessage(MessageType.DISABLED));
            return true;
        });

        onArgumentSequence("reload", (sender, args) -> {
            if (Config.noPermission(sender, Permission.RELOAD)) return true;
            //just reloading
            plugin.reload();
            Config.sendMessage(sender, MessageType.RELOAD);
            return true;
        });
    }

    @Override
    public void messageNoPermission(final @NotNull CommandSender commandSender) {
        Config.sendMessage(commandSender, MessageType.NO_PERMISSION);
    }
}
