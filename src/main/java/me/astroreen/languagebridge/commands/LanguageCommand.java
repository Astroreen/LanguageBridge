package me.astroreen.languagebridge.commands;

import com.comphenix.packetwrapper.WrapperPlayServerEntityMetadata;
import com.comphenix.packetwrapper.WrapperPlayServerWindowItems;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import lombok.CustomLog;
import me.astroreen.astrolibs.api.bukkit.command.CommandTreeNode;
import me.astroreen.astrolibs.api.bukkit.command.SimpleCommand;
import me.astroreen.languagebridge.LanguageBridge;
import me.astroreen.languagebridge.config.Config;
import me.astroreen.languagebridge.config.MessageType;
import me.astroreen.languagebridge.permissions.Permission;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@CustomLog
public class LanguageCommand extends SimpleCommand {

    private final static ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();


    public LanguageCommand() {
        super(LanguageBridge.getInstance(), LOG, "language", Permission.LANGUAGE_COMMAND.getName());
    }

    @Override
    public void prepare() {
        final CommandTreeNode root = getRoot();
        root.addArguments(Config.getStoredLanguages());


        onArgumentSequence((sender, args) -> {
            Config.getPlayerLanguage(((Player) sender).getUniqueId()).ifPresent(language ->
                    Config.sendMessage(sender, MessageType.CURRENT_LANGUAGE, language));
            return true;
        });


        onArgumentSequence(Config.getStoredLanguages().toString(), (sender, args) -> {
            final String language = args[0];
            final Player player = (Player) sender;

            if (!Config.getStoredLanguages().contains(language)) {
                Config.sendMessage(sender, MessageType.UNKNOWN_ARGUMENTS, language);
                return true;
            }

            //setting new language
            Config.setPlayerLanguage(player.getUniqueId(), language);
            Config.sendMessage(sender, MessageType.LANGUAGE_SET, language);

            //update inventory placeholders
            updateInventoryItems(player);

            //update placeholders in entity names
            updateEntityNames(player);
            return true;
        });
    }

    private void updateEntityNames(final @NotNull Player player) {
        final PacketContainer container = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        final WrapperPlayServerEntityMetadata wrapper = new WrapperPlayServerEntityMetadata(container);

        for(final LivingEntity entity : player.getWorld().getLivingEntities()) {
            if(entity == null || entity instanceof Player) continue;
            final Component name = entity.customName();
            if(name == null) continue;

            final Component component = LanguageBridge.getInstance().getPlaceholderManager().translateToComponent(player, name);
            entity.setCustomNameVisible(true);
            entity.customName(component);
            //todo: send packet, not replace custom name
        }

        //protocolManager.sendServerPacket(player, container);
    }

    private void updateInventoryItems(final @NotNull Player player) {
        final PacketContainer container = protocolManager.createPacket(PacketType.Play.Server.WINDOW_ITEMS);
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

            final Component name = LanguageBridge.getInstance().getPlaceholderManager().translateToComponent(player, displayName);
            meta.displayName(name);
            final ItemStack result = new ItemStack(item);
            result.setItemMeta(meta);
            items.add(result);
        }
        wrapper.setSlotData(items);

        protocolManager.sendServerPacket(player, container);
    }

    @Override
    public void messageNoPermission(final @NotNull CommandSender commandSender) {
        Config.sendMessage(commandSender, MessageType.NO_PERMISSION);
    }
}
