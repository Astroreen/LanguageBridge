package me.astroreen.languagebridge.utils;

import me.astroreen.languagebridge.LanguageBridge;
import lombok.CustomLog;
import me.astroreen.languagebridge.version.VersionChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

@CustomLog
public class StartScreen {

    private final ConsoleCommandSender console;
    private final String rawAuthors = LanguageBridge.getInstance().getPluginMeta().getAuthors().toString();
    private final String authors = rawAuthors.substring(1, rawAuthors.length() - 1);
    private final String version = LanguageBridge.getInstance().getPluginMeta().getVersion();
    private final Component n = Component.text("\n");

    public StartScreen(final @NotNull ConsoleCommandSender console) {
        this.console = console;
    }

    //https://patorjk.com/software/taag/#p=display&f=Graffiti&t=Type%20Something%20 -> Custom Text
    public void BridgeImage() {
        log(n
                .append(n)
                .append(Component.text("  _                                      ").color(TextColor.fromHexString("#5555FF"))).append(n)
                .append(Component.text(" | |   __ _ _ _  __ _ _  _ __ _ __ _ ___ ").color(TextColor.fromHexString("#5555FF"))).append(n)
                .append(Component.text(" | |__/ _` | ' \\/ _` | || / _` / _` / -_)").color(TextColor.fromHexString("#5555FF"))).append(n)
                .append(Component.text(" |____\\__,_|_||_\\__, |\\_,_\\__,_\\__, \\___|").color(TextColor.fromHexString("#5555FF"))).append(n)
                .append(Component.text(" | _ )_ _(_)__| |____ ___      |___/     ").color(TextColor.fromHexString("#5555FF"))).append(n)
                .append(Component.text(" | _ | '_| / _` / _` / -_)               ").color(TextColor.fromHexString("#5555FF"))).append(n)
                .append(Component.text(" |___|_| |_\\__,_\\__, \\___|               ").color(TextColor.fromHexString("#5555FF"))).append(n)
                .append(Component.text("                |___/                    ").color(TextColor.fromHexString("#5555FF"))).append(n)
                .append(Component.text("Made by " + authors + ".").color(TextColor.fromHexString("#55FF55"))).append(n)
                .append(Component.text("Current version: " + version).color(TextColor.fromHexString("#55FF55"))).append(n)
                .append(Component.text("Server version: " + VersionChecker.getRawVersion()).color(TextColor.fromHexString("#555555"))).append(n)
        );
    }

    private void log(final @NotNull Component msg) {
        console.sendMessage(msg);
    }
}
