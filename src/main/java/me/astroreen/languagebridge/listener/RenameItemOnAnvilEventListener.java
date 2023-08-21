package me.astroreen.languagebridge.listener;

import com.destroystokyo.paper.event.block.AnvilDamagedEvent;
import me.astroreen.astrolibs.listener.EventListener;
import me.astroreen.languagebridge.LanguageBridge;
import me.astroreen.languagebridge.module.placeholder.PlaceholderManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.inventory.ItemStack;

//todo: rework
public class RenameItemOnAnvilEventListener extends EventListener {

    private final LanguageBridge plugin;

    public RenameItemOnAnvilEventListener() throws NoSuchMethodException {
        super(
                AnvilDamagedEvent.class,
                LanguageBridge.getEventPriority(),
                RenameItemOnAnvilEventListener.class.getMethod("onAnvilDamagedEvent", AnvilDamagedEvent.class),
                LanguageBridge.getInstance()
        );
        this.plugin = LanguageBridge.getInstance();
    }


    public void onAnvilDamagedEvent(final AnvilDamagedEvent event) {
        final ItemStack itemStack = event.getInventory().getResult();
        if(itemStack == null) return;

        final Component item = itemStack.displayName();
        final TextColor itemNameColor = item.color();
        if(PlaceholderManager.hasPlaceholder(item.examinableName())){
        }
    }

    @Override
    public String getName() {
        return "RenameItemOnAnvilEventListener";
    }
}
