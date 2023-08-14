package me.astroreen.languagebridge.utils;

import me.astroreen.languagebridge.LanguageBridge;
import lombok.CustomLog;
import me.astroreen.languagebridge.version.VersionChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
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
                .append(Component.text("  dl;,:cc;,,,;cc:,,;::::;,,:cc;,,,;cc:,;ld").color(TextColor.fromHexString("#6ed1fa")))  .append(n)
                .append(Component.text("  dl::colc;;;cloc;;:cccc:;;colc;;;cloc::ld").color(TextColor.fromHexString("#66c2ee")))  .append(n)
                .append(Component.text("  ;:cloooollloooollloooollloooollloooolc:;  ").color(TextColor.fromHexString("#5fb3e1")) .append(Component.text("__________        .__    .___              ").color(TextColor.fromHexString("#CFCFCF")))).append(n)
                .append(Component.text("  ;coxxxxxkkkkkkkkkkkkkkkkkkkkkkkkxxxxxoc;  ").color(TextColor.fromHexString("#59a5d4")) .append(Component.text("\\______   \\_______|__| __| _/ ____   ____").color(TextColor.fromHexString("#CFCFCF")))).append(n)
                .append(Component.text("  loxOkxk0KXXXXXXXXXXXXXXXXXXXXXXK0kxkOxol  ").color(TextColor.fromHexString("#5397c7")) .append(Component.text(" |    |  _/\\_  __ \\  |/ __ | / ___\\_/ __ \\").color(TextColor.fromHexString("#CFCFCF")))).append(n)
                .append(Component.text("  oxO0OxxOKNWWNXKK00000000KKXNWWNKOxxO0Oxo  ").color(TextColor.fromHexString("#4e89b9")) .append(Component.text(" |    |   \\ |  | \\/  / /_/ |/ /_/  >  ___/").color(TextColor.fromHexString("#CFCFCF")))).append(n)
                .append(Component.text("  oxKXXKOkO0XX0kxddxxxxxxddxk0XX0OkOKXXKxo  ").color(TextColor.fromHexString("#487bac")) .append(Component.text(" |______  / |__|  |__\\____ |\\___  / \\___  >").color(TextColor.fromHexString("#CFCFCF")))).append(n)
                .append(Component.text("  oxKNWWWNX0kxkO0X        X0Okxk0XNWWWNKxo  ").color(TextColor.fromHexString("#436d9e")) .append(Component.text("        \\/                \\/_____/      \\/ ").color(TextColor.fromHexString("#CFCFCF")))).append(n)
                .append(Component.text("  oxKWMMWNKkxk0N            N0kxkKNWMMWKxo   ").color(TextColor.fromHexString("#3f6090")).append(Component.text("Made by " + authors + ".").color(TextColor.fromHexString("#b6fc03")))).append(n)
                .append(Component.text("  oxKWMWXOkk0X                X0kkOXWMWKxo   ").color(TextColor.fromHexString("#3a5382")).append(Component.text("Current version: " + version).color(TextColor.fromHexString("#b6fc03")))).append(n)
                .append(Component.text("  odk0K0kdoON                  NOodk0K0kdo   ").color(TextColor.fromHexString("#354674")).append(Component.text("Server version: " + VersionChecker.getRawVersion()).color(TextColor.fromHexString("#42484D")))).append(n)
                .append(Component.text("  kxxddddox0                    0xoddddxxk").color(TextColor.fromHexString("#2f3a66")))  .append(n)
                .append(Component.text("  NNNXXXXXN                      NXXXXXNNN").color(TextColor.fromHexString("#2a2e58")))  .append(n)
        );
    }

    private void log(final @NotNull Component msg) {
        console.sendMessage(msg);
    }
}
