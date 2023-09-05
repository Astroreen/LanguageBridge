package me.astroreen.languagebridge.commands;

import lombok.CustomLog;
import me.astroreen.astrolibs.api.bukkit.command.CommandArgumentNode;
import me.astroreen.astrolibs.api.bukkit.command.CommandArguments;
import me.astroreen.astrolibs.api.bukkit.command.CommandFlag;
import me.astroreen.astrolibs.api.bukkit.command.SimpleCommand;
import me.astroreen.languagebridge.LanguageBridge;
import me.astroreen.languagebridge.config.Config;
import me.astroreen.languagebridge.config.MessageType;
import me.astroreen.languagebridge.permissions.Permission;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CustomLog
public class LanguageBridgeTestCommand extends SimpleCommand {

    public LanguageBridgeTestCommand() {
        super(LanguageBridge.getInstance(), LOG, "languagebridgetest", Permission.MAIN_COMMAND.getName());
    }

    @Override
    public CommandArguments setupArguments(final @NotNull CommandArguments arguments) {
        final CommandArgumentNode root = arguments.getRoot();

        final CommandArgumentNode name = root.addArgument("display-name", CommandFlag.ONLY_PLAYER);
        name.addArgument("item");
        name.addArgument("entity");

        return arguments;
    }

    @Override
    public void execute() {
        onArgumentSequence("display-name item", (sender, args) -> {

            final Component name = ((Player) sender).getInventory().getItemInMainHand().displayName();
            sender.sendMessage(name);
            return true;
        });

        onArgumentSequence("display-name entity", (sender, args) -> {

            final Entity entity = ((Player) sender).getTargetEntity(24);
            if (entity == null) return true;

            Component name = entity.customName();
            if (name == null) name = entity.name();

            sender.sendMessage(name);
            return true;
        });
    }

    @Override
    public void messageNoPermission(final @NotNull CommandSender commandSender) {
        Config.sendMessage(commandSender, MessageType.NO_PERMISSION);
    }
}
