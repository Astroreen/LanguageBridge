package me.astroreen.languagebridge.listener;

import me.astroreen.astrolibs.api.listener.EventListener;
import me.astroreen.astrolibs.utils.PlayerConverter;
import me.astroreen.languagebridge.LanguageBridge;
import me.astroreen.languagebridge.PlaceholderManager;
import me.astroreen.languagebridge.config.Config;
import me.astroreen.languagebridge.permissions.Permission;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class onPrepareAnvilEventListener extends EventListener {

    private final LanguageBridge plugin;

    public onPrepareAnvilEventListener(final @NotNull LanguageBridge plugin) throws NoSuchMethodException {
        super(
                plugin,
                PrepareAnvilEvent.class,
                onPrepareAnvilEventListener.class.getMethod("onPrepareAnvilEvent", PrepareAnvilEvent.class),
                LanguageBridge.getEventPriority()
        );
        this.plugin = plugin;
    }


    public void onPrepareAnvilEvent(final @NotNull PrepareAnvilEvent event) {
        final ItemStack item = event.getResult();
        if (item == null) return;
        //getting player
        final HumanEntity human = event.getView().getPlayer();
        final Player player = PlayerConverter.getPlayer(human.getUniqueId());
        if (player == null) return;

        //checking permission
        if(Config.noPermission(player, Permission.PLACEHOLDER_ANVIL)) return;

        //creating new name
        final ItemMeta meta = item.getItemMeta();
        if(!meta.hasDisplayName()) return;

        final Component itemDisplayName = meta.displayName();
        if(itemDisplayName == null) return;

        final PlaceholderManager manager = plugin.getPlaceholderManager();
        final Component name = manager.translate(player, itemDisplayName);

        //creating result
        final ItemStack result = new ItemStack(item);
        //adding new name
        final ItemMeta resultMeta = result.getItemMeta();
        resultMeta.displayName(name);
        result.setItemMeta(resultMeta);
        //set new result
        event.setResult(result);
    }

    @Override
    public String getName() {
        return "prepare-anvil";
    }
}
