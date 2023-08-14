package me.astroreen.languagebridge.compatibility.luckperms;

import me.astroreen.languagebridge.compatibility.Integrator;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class BRLuckPermsIntegrator implements Integrator {
    @Override
    public void hook() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) new LPPermissionManager(provider.getProvider());
    }

    @Override
    public void reload() {
        //empty
    }

    @Override
    public void close() {
        //empty
    }
}
