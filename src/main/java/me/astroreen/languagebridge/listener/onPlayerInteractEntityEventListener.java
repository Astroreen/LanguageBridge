package me.astroreen.languagebridge.listener;

import me.astroreen.astrolibs.api.bukkit.listener.EventListener;
import me.astroreen.languagebridge.LanguageBridge;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

public class onPlayerInteractEntityEventListener extends EventListener {

    private LanguageBridge plugin;

    public onPlayerInteractEntityEventListener(final @NotNull LanguageBridge plugin) {
        super(plugin, LanguageBridge.getEventPriority());
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteractEntityEvent(final @NotNull PlayerInteractEntityEvent event){
        final Player player = event.getPlayer();
        final PlayerInventory inv = player.getInventory();

        if(!inv.getItemInMainHand().getType().equals(Material.NAME_TAG) ||
            !inv.getItemInOffHand().getType().equals(Material.NAME_TAG)) return;

        event.setCancelled(true);

        final Entity entity = event.getRightClicked();
        //todo: spawn on entity armor stand with custom name
    }

    @Override
    public String getName() {
        return "interact-entity";
    }
}
