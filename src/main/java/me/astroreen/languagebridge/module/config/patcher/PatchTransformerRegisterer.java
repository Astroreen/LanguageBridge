package me.astroreen.languagebridge.module.config.patcher;

import me.astroreen.languagebridge.module.config.Patcher;
import me.astroreen.languagebridge.module.config.transformers.*;

/**
 * Functional interface for registering all transformers of a {@link Patcher}.
 */
public interface PatchTransformerRegisterer {

    /**
     * Functional interface for registering all transformers of a {@link Patcher}.
     *
     * @param patcher the {@link Patcher} to register the transformers for
     */
    default void registerTransformers(final Patcher patcher) {
        patcher.registerTransformer("SET", new SetTransformer());
        patcher.registerTransformer("REMOVE", new RemoveTransformer());
        patcher.registerTransformer("KEY_RENAME", new KeyRenameTransformer());
        patcher.registerTransformer("VALUE_RENAME", new ValueRenameTransformer());
        patcher.registerTransformer("LIST_ENTRY_ADD", new ListEntryAddTransformer());
        patcher.registerTransformer("LIST_ENTRY_REMOVE", new ListEntryRemoveTransformer());
        patcher.registerTransformer("LIST_ENTRY_RENAME", new ListEntryRenameTransformer());
        patcher.registerTransformer("TYPE_TRANSFORM", new TypeTransformer());
    }
}
