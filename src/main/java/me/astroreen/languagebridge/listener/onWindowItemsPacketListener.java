package me.astroreen.languagebridge.listener;

import com.comphenix.packetwrapper.WrapperPlayServerWindowItems;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.astroreen.astrolibs.api.listener.PacketEventListener;
import me.astroreen.languagebridge.LanguageBridge;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class onWindowItemsPacketListener extends PacketEventListener {

    private final LanguageBridge plugin;
    private final ProtocolManager manager = ProtocolLibrary.getProtocolManager();

    public onWindowItemsPacketListener(final @NotNull LanguageBridge plugin) {
        super(plugin, PacketType.Play.Server.WINDOW_ITEMS);
        this.plugin = plugin;
    }

    @Override
    public void onPacketSending(final @NotNull PacketEvent event) {
        if (!event.getPacketType().equals(PacketType.Play.Server.WINDOW_ITEMS)) return;

        final Player player = event.getPlayer();
        final PacketContainer container = event.getPacket();
        final WrapperPlayServerWindowItems wrapper = new WrapperPlayServerWindowItems(container);

        final List<ItemStack> items = new ArrayList<>();
        for(final ItemStack item : wrapper.getSlotData()) {
            final ItemMeta meta = item.getItemMeta();
            if(meta == null) {
                items.add(item);
                continue;
            }
            final Component displayName = meta.displayName();
            if(displayName == null) {
                items.add(item);
                continue;
            }

            final Component name = plugin.getPlaceholderManager().translateToComponent(player, displayName);
            meta.displayName(name);
            final ItemStack result = new ItemStack(item);
            result.setItemMeta(meta);
            items.add(result);
        }

        wrapper.setSlotData(items);
    }

    @Override
    public String getName() {
        return "window-items";
    }
}
