package me.astroreen.languagebridge.compatibility.luckperms;

import me.astroreen.astrolibs.utils.PlayerConverter;
import me.astroreen.languagebridge.module.permissions.PermissionManager;
import lombok.CustomLog;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@CustomLog(topic = "LPPermissionManager")
public class LPPermissionManager extends PermissionManager {

    private final LuckPerms LuckPerms;
    public static LPPermissionManager instance;
    public LPPermissionManager(LuckPerms lp) {
        LuckPerms = lp;
        instance = this;
    }

    @Override
    public boolean hasPermission(final @NotNull Player player, final @NotNull String permission) {
        User user = LuckPerms.getUserManager().getUser(player.getUniqueId());
        if(user != null)
            return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
        else return false;
    }

    @Override
    public boolean hasPermission(final @NotNull UUID uuid, final @NotNull String permission) {
        Player player = PlayerConverter.getPlayer(uuid);
        if(player != null) hasPermission(player, permission);
        try {
            User user = LuckPerms.getUserManager().loadUser(uuid).get();
            return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("There was an exception checking player permissions", e);
            return false;
        }
    }

    @Override
    public void addPermission(final @NotNull UUID uuid, final @NotNull String permission, final boolean value) {
        LuckPerms.getUserManager().modifyUser(uuid, user -> user.data().add(convert(permission, value)));
    }

    public static @NotNull Node convert(final @NotNull String perm, final boolean value){
        return Node.builder(perm).value(value).build();
    }

    public static LPPermissionManager getInstance() {return instance;}
}
