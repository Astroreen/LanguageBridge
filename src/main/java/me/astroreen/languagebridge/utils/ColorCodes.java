package me.astroreen.languagebridge.utils;

import me.astroreen.languagebridge.module.config.ConfigurationFile;
import lombok.CustomLog;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@CustomLog
public class ColorCodes {
    static public final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";

    private ColorCodes() {
    }

    public static boolean isHexValid(String color) {
        if (color == null) return false;
        return color.matches("^#([A-Fa-f\\d]{6})$");
    }

    @NotNull
    public static String generateColoredMessage(final @NotNull ConfigurationFile config, @NotNull final String head, @NotNull final String tail, @NotNull final String message) {
        if (message.isEmpty()) return message;
        final int[] hex = resolveDefinedHexInts(head, tail);

        final double perChar = 100.0 / message.length();
        final StringBuilder builder = new StringBuilder();

        double current = 0;

        if(head.equals(tail)) {
            final int[] values = generatePercentageRGB(hex, current / 100.0);
            final String delimiter = config.getString("delimiter", "§");
            builder.append(delimiter).append("x").append(delimiter)
                    .append(String.join(delimiter, String.format("%02X%02X%02X", values[0], values[1], values[2]).split("")))
                    .append(message);
            return builder.toString();
        }

        for (final char c : message.toCharArray()) {
            final int[] values = generatePercentageRGB(hex, current / 100.0);

            final String delimiter = config.getString("delimiter", "§");
            builder.append(delimiter).append("x").append(delimiter)
                    .append(String.join(delimiter, String.format("%02X%02X%02X", values[0], values[1], values[2]).split("")))
                    .append(c);

            current = Math.min(100.0, current + perChar);
        }

        return builder.toString();
    }


    /**
     * Create an array of ints representing the rgb values of the provided strings
     *
     * @param head The head color of the gradient
     * @param tail The tail color of the gradient
     * @return An array of 6 ints between [0..255]
     * @apiNote Array values are in order [hexOneR, hexOneG, hexOneB, hexTwoR, hexTwoG, hexTwoB]
     */
    private static int @NotNull [] resolveDefinedHexInts(@NotNull final String head, @NotNull final String tail) {
        final int hexOneR = Integer.parseInt(head.substring(1, 3), 16);
        final int hexOneG = Integer.parseInt(head.substring(3, 5), 16);
        final int hexOneB = Integer.parseInt(head.substring(5, 7), 16);

        final int hexTwoR = Integer.parseInt(tail.substring(1, 3), 16);
        final int hexTwoG = Integer.parseInt(tail.substring(3, 5), 16);
        final int hexTwoB = Integer.parseInt(tail.substring(5, 7), 16);

        return new int[]{hexOneR, hexOneG, hexOneB, hexTwoR, hexTwoG, hexTwoB};
    }

    /**
     * Create an array of ints representing the rgb value of the color at the <code>percentage</code> of the  gradient defined by <code>hex</code>
     *
     * @param hex        The gradient's hex values
     * @param percentage The percentage along the gradient to target
     * @return An array of 3 ints between [0..255]
     * @apiNote Array values are in order [valueR, valueG, valueB]
     */
    @Contract(value = "_, _ -> new", pure = true)
    private static int @NotNull [] generatePercentageRGB(final int @NotNull [] hex, final double percentage) {
        final int valueR = (int) (hex[0] + ((hex[3] - hex[0]) * percentage));
        final int valueG = (int) (hex[1] + ((hex[4] - hex[1]) * percentage));
        final int valueB = (int) (hex[2] + ((hex[5] - hex[2]) * percentage));

        return new int[]{valueR, valueG, valueB};
    }

    /**
     * Translate all colors and return as Kyori Adventure {@link TextComponent}
     *
     * @param text original text
     * @return {@link TextComponent} if all color codes are translatable
     * @throws IllegalStateException if in color code aren't recognizable
     */
    public static @NotNull TextComponent translateToTextComponent(@NotNull String text)
            throws IllegalStateException {

        String[] texts = text.split(String.format(WITH_DELIMITER, "&"));

        TextComponent.Builder builder = Component.text();
        for (int i = 0; i < texts.length; i++) {
            if (texts[i].equalsIgnoreCase("&")) {
                TextComponent component = Component.empty();
                //get the next string
                i++;
                if (texts[i].charAt(0) == '#') {
                    component = component.content(texts[i].substring(7))
                            .color(TextColor.fromHexString(texts[i].substring(0, 7)));
                    builder.append(component);
                } else {
                    if (texts[i].length() > 1) {
                        component = component.content(texts[i].substring(1));
                    } else {
                        component = component.content(" ");
                    }

                    Style style = switch (texts[i].charAt(0)) {
                        case '0' -> Style.style(TextColor.fromHexString("#000000"));
                        case '1' -> Style.style(TextColor.fromHexString("#0000AA"));
                        case '2' -> Style.style(TextColor.fromHexString("#00AA00"));
                        case '3' -> Style.style(TextColor.fromHexString("#00AAAA"));
                        case '4' -> Style.style(TextColor.fromHexString("#AA0000"));
                        case '5' -> Style.style(TextColor.fromHexString("#AA00AA"));
                        case '6' -> Style.style(TextColor.fromHexString("#FFAA00"));
                        case '7' -> Style.style(TextColor.fromHexString("#AAAAAA"));
                        case '8' -> Style.style(TextColor.fromHexString("#555555"));
                        case '9' -> Style.style(TextColor.fromHexString("#5555FF"));
                        case 'a' -> Style.style(TextColor.fromHexString("#55FF55"));
                        case 'b' -> Style.style(TextColor.fromHexString("#55FFFF"));
                        case 'c' -> Style.style(TextColor.fromHexString("#FF5555"));
                        case 'd' -> Style.style(TextColor.fromHexString("#FF55FF"));
                        case 'e' -> Style.style(TextColor.fromHexString("#FFFF55"));
                        case 'f' -> Style.style(TextColor.fromHexString("#FFFFFF"));
                        case 'k' -> Style.style(TextDecoration.OBFUSCATED);
                        case 'l' -> Style.style(TextDecoration.BOLD);
                        case 'm' -> Style.style(TextDecoration.STRIKETHROUGH);
                        case 'n' -> Style.style(TextDecoration.UNDERLINED);
                        case 'o' -> Style.style(TextDecoration.ITALIC);
                        case 'r' -> Style.style(TextColor.fromHexString("#FFFFFF"));
                        default -> throw new IllegalStateException("Unexpected value: " + texts[i].charAt(0));
                    };
                    component = component.style(style);
                    builder.append(component);
                }
            } else builder.append(Component.text(texts[i]));
        }
        return builder.build();
    }

    /**
     * @param text The string of text to apply color/effects to
     * @return Returns a string of text with color/effects applied
     */
    @Deprecated
    public static @NotNull String translate(@NotNull String text) {

        String[] texts = text.split(String.format(WITH_DELIMITER, "&"));

        StringBuilder finalText = new StringBuilder();

        for (int i = 0; i < texts.length; i++) {
            if (texts[i].equalsIgnoreCase("&")) {
                //get the next string
                i++;
                if (texts[i].charAt(0) == '#') {
                    finalText.append(texts[i].substring(7));
                } else {
                    finalText.append(ChatColor.translateAlternateColorCodes('&', "&" + texts[i]));
                }
            } else {
                finalText.append(texts[i]);
            }
        }

        return finalText.toString();
    }
}
