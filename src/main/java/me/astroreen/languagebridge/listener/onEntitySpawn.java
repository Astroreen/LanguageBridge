package me.astroreen.languagebridge.listener;

import com.comphenix.packetwrapper.WrapperPlayServerSpawnEntity;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import me.astroreen.astrolibs.api.listener.PacketEventListener;
import me.astroreen.languagebridge.LanguageBridge;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class onEntitySpawn extends PacketEventListener {

    final LanguageBridge plugin;
    public onEntitySpawn(final @NotNull LanguageBridge plugin) {
        super(plugin, PacketType.Play.Server.SPAWN_ENTITY);
        this.plugin = plugin;
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        final PacketContainer container = event.getPacket();
        final WrapperPlayServerSpawnEntity wrapper = new WrapperPlayServerSpawnEntity(container);

        final Entity entity = wrapper.getEntity(event);
        if(entity == null || entity instanceof Player) return;

        Component name = entity.customName();
        if(name == null) return;

        final Component component = plugin.getPlaceholderManager().translateToComponent(event.getPlayer(), name);
        entity.setCustomNameVisible(true);
        entity.customName(component);
    }

    @Override
    public String getName() {
        return "entity-spawn";
    }
}
