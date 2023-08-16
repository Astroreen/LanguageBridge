package me.astroreen.languagebridge.module.placeholder;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.regex.Pattern;

public class PlaceholderManager {
    private static final String PREFIX = "lngbridge_";
    private static final Pattern PATTERN = Pattern.compile("%" + PREFIX + ".*?%", Pattern.CASE_INSENSITIVE);

    public @NotNull Optional<String> getPlaceholderValue(final String key){
        if(!hasPlaceholder(key)) return Optional.empty();

        //todo: get placeholder value from database or config

        return Optional.empty();
    }

    public boolean hasPlaceholder(final String text){
        if(text == null) return false;
        return PATTERN.matcher(text).find();
    }
}
