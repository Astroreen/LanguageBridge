package me.astroreen.languagebridge.commands;

import lombok.CustomLog;
import me.astroreen.astrolibs.api.bukkit.command.SimpleTabCompleter;
import me.astroreen.languagebridge.LanguageBridge;
import me.astroreen.languagebridge.config.Config;
import me.astroreen.languagebridge.config.MessageType;
import me.astroreen.languagebridge.permissions.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@CustomLog
public class LanguageCommand implements CommandExecutor, SimpleTabCompleter {

    private static final LanguageBridge plugin = LanguageBridge.getInstance();

    public LanguageCommand() {
        final PluginCommand command = plugin.getCommand("language");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command cmd, final @NotNull String label, final @NotNull String[] args) {
        if (!cmd.getName().equalsIgnoreCase("language")) {
            return false;
        }

        LOG.debug("Executing /" + cmd.getName() + " command for user " + sender.getName()
                + " with arguments: " + Arrays.toString(args));

        if (!(sender instanceof Player player)) {
            LOG.error("This command can only be executed by the player.");
            return true;
        }

        if (Config.noPermission(sender, Permission.LANGUAGE_COMMAND)) return true;

        if (args.length == 0) {
            Config.getPlayerLanguage(player.getUniqueId()).ifPresent(language ->
                    Config.sendMessage(player, MessageType.CURRENT_LANGUAGE, language));
            return true;
        } else if (args.length > 1) {
            Config.sendMessage(sender, MessageType.UNKNOWN_ARGUMENTS, args);
            return true; //false for help command
        }

        final String language = args[0];

        if (!Config.getStoredLanguages().contains(language)) {
            Config.sendMessage(sender, MessageType.UNKNOWN_ARGUMENTS, language);
            return true;
        }

        //setting new language
        Config.setPlayerLanguage(player.getUniqueId(), language);
        Config.sendMessage(player, MessageType.LANGUAGE_SET, language);

        LOG.debug("Command executing done");
        return true;
    }

    @Override
    public Optional<List<String>> simpleTabComplete(final @NotNull CommandSender sender, final @NotNull Command command,
                                                    final @NotNull String alias, final String @NotNull ... args) {

        if (args.length == 1)
            return Optional.of(Config.getStoredLanguages().stream().toList());

        return Optional.empty();
    }
}
