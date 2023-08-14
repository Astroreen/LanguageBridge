package me.astroreen.languagebridge.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Interface which handles tab complete for commands.
 */
public interface SimpleTabCompleter extends TabCompleter {
    default List<String> onTabComplete(final @NotNull CommandSender sender, final @NotNull Command command, final @NotNull String alias, final String[] args) {
        final Optional<List<String>> completions = this.simpleTabComplete(sender, command, alias, args);
        if (completions.isEmpty()) {
            return null;
        }
        final List<String> out = new ArrayList<>();
        final String lastArg = args[args.length - 1];
        for (final String completion : completions.get()) {
            if (lastArg == null || lastArg.matches(" *") || completion.toLowerCase(Locale.ROOT).startsWith(lastArg.toLowerCase(Locale.ROOT))) {
                out.add(completion);
            }
        }
        return out;
    }

    Optional<List<String>> simpleTabComplete(CommandSender sender, Command command, String alias, String... args);
}
