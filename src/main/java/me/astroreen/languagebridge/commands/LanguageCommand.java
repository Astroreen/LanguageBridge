package me.astroreen.languagebridge.commands;

import lombok.CustomLog;
import me.astroreen.astrolibs.api.bukkit.command.CommandArgumentNode;
import me.astroreen.astrolibs.api.bukkit.command.CommandArguments;
import me.astroreen.astrolibs.api.bukkit.command.SimpleCommand;
import me.astroreen.languagebridge.LanguageBridge;
import me.astroreen.languagebridge.config.Config;
import me.astroreen.languagebridge.config.MessageType;
import me.astroreen.languagebridge.permissions.Permission;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

@CustomLog
public class LanguageCommand extends SimpleCommand {


    public LanguageCommand() {
        super(LanguageBridge.getInstance(), LOG, "language", Permission.LANGUAGE_COMMAND.getName());
    }

    @Override
    public CommandArguments setupArguments(@NotNull CommandArguments commandArguments) {
        final CommandArgumentNode root = commandArguments.getRoot();
        Config.getStoredLanguages().forEach(root::addArgument);

        return commandArguments;
    }

    @Override
    public void execute() {
        onArgumentSequence("", (sender, args) -> {
            Config.getPlayerLanguage(((Player) sender).getUniqueId()).ifPresent(language ->
                    Config.sendMessage(sender, MessageType.CURRENT_LANGUAGE, language));
            return true;
        });

        for (final String lang : Config.getStoredLanguages())
            onArgumentSequence(lang, (sender, args) -> {
                final String language = args[0];

                if (!Config.getStoredLanguages().contains(language)) {
                    Config.sendMessage(sender, MessageType.UNKNOWN_ARGUMENTS, language);
                    return true;
                }

                //setting new language
                Config.setPlayerLanguage(((Player) sender).getUniqueId(), language);
                Config.sendMessage(sender, MessageType.LANGUAGE_SET, language);
                return true;
            });
    }

    @Override
    public void messageNoPermission(final @NotNull CommandSender commandSender) {
        Config.sendMessage(commandSender, MessageType.NO_PERMISSION);
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


        return true;
    }
}
