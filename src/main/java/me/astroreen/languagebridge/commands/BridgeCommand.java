package me.astroreen.languagebridge.commands;

import lombok.CustomLog;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

@CustomLog
public class BridgeCommand implements CommandExecutor, SimpleTabCompleter {
    @Override
    public Optional<List<String>> simpleTabComplete(CommandSender sender, Command command, String alias, String... args) {
        return Optional.empty();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return false;
    }

    /*private final LanguageBridge instance = LanguageBridge.getInstance();
    private final DefaultPermissionManager permManager;

    public BridgeCommand() {
        final PluginCommand command = instance.getCommand("bridge");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
        permManager = instance.getPermManager();
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, final @NotNull Command cmd, final @NotNull String alias, final @NotNull String[] args) {
        if ("bridge".equalsIgnoreCase(cmd.getName())) {
            LOG.debug("Executing /bridge command for user " + sender.getName()
                    + " with arguments: " + Arrays.toString(args));
            // if the command is empty, display help message
            if (args.length == 0) return true;

            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "language", "lang" -> handleLanguage(sender, args);
                case "version", "ver", "v" ->
                        sendMessage(sender, MessageType.VERSION, instance.getDescription().getVersion());
                case "debug" -> handleDebug(sender, args);
                case "reload", "rl" -> {
                    if (noPermission(sender, Permission.COMMAND_RELOAD)) return true;
                    //just reloading
                    instance.reload();
                    sendMessage(sender, MessageType.RELOADED);
                }
                case "nickname", "nick" -> {
                    final IModule module = ModuleManager.NICKNAME.getModule();
                    if (!Compatibility.getHooked().contains(CompatiblePlugin.TAB))
                        sendMessage(sender, MessageType.PLUGIN_DISABLED, module.getName());

                    if (!ModuleManager.NICKNAME.isActive()) {
                        sendMessage(
                                sender,
                                MessageType.MODULE_STATE,
                                (module.getName() == null ? "color-nickname" : module.getName()),
                                Config.getMessage(MessageType.DISABLED)
                        );
                        return true;
                    }
                    handleNickName(sender, args);
                }
                case "ffa" -> {
                    if (noPermission(sender, Permission.COMMAND_FFA)) return true;
                    if (!ModuleManager.FFA.isActive()) {
                        sendMessage(sender, MessageType.MODULE_STATE, ModuleManager.FFA.getModule().getName(), Config.getMessage(MessageType.DISABLED));
                        return true;
                    }
                    handleFFA(sender, args);
                }
                default -> sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args[0]);
            }
            LOG.debug("Command executing done");
            return true;
        }
        return false;
    }

    @Override
    public Optional<List<String>> simpleTabComplete(final @NotNull CommandSender sender, final @NotNull Command command,
                                                    final @NotNull String alias, final String @NotNull ... args) {
        if (args.length == 1) {
            return Optional.of(Arrays.asList("language", "version", "reload", "debug", "ffa", "nickname"));
        }
        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "language" -> completeLanguage(args);
            case "debug" -> completeDebug(args);
            case "nickname" -> completeNickname(args);
            case "ffa" -> completeFFA(args);
            default -> Optional.empty();
        };
    }

    private void handleFFA(final @NotNull CommandSender sender, final String @NotNull ... args) {
        switch (args[1]) {
            // bridge ffa arena
            case "arena": {
                if (args[2].equalsIgnoreCase("load")) {
                    // bridge ffa arena load <name>
                    if (noPermission(sender, Permission.FFA_ARENA_LOAD)) return;
                    if (!FFAArenaManager.getExistingFFAWorlds().contains(args[3])) {
                        sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args[3]);
                        return;
                    }
                    final String arena = args[3];
                    if (!WorldUtils.isWorldFolderExist(arena))
                        sendMessage(sender, MessageType.ARENA_LOADED_ERROR, arena);
                    if (WorldUtils.loadWorld(arena) == null) sendMessage(sender, MessageType.ARENA_LOADED_ERROR, arena);
                    sendMessage(sender, MessageType.ARENA_LOADED_SUCCESSFULLY, arena);

                    //reloading list of enabled worlds
                    FFAArenaManager.getActiveFFAWorlds(true);
                    // done
                    return;
                } else if (args[2].equalsIgnoreCase("unload")) {
                    // bridge ffa arena unload <name>
                    if (noPermission(sender, Permission.FFA_ARENA_UNLOAD)) return;
                    final World world = WorldUtils.getWorld(args[3]);
                    //check if world exists and it is FFAModule's world
                    if (world == null || !FFAArenaManager.getActiveFFAWorlds(false).contains(world)) {
                        sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args[3]);
                        return;
                    }
                    if (WorldUtils.unloadWorld(world, !FFAArenaManager.haveSchematic(world.getName())))
                        sendMessage(sender, MessageType.ARENA_UNLOADED_SUCCESSFULLY, world.getName());
                    else sendMessage(sender, MessageType.ARENA_UNLOADED_ERROR, world.getName());

                    //reloading list of enabled worlds
                    FFAArenaManager.getActiveFFAWorlds(true);
                    //done
                    return;
                } else if (args[2].equalsIgnoreCase("create")) {
                    // bridge ffa arena create <name>
                    final String name = args[3];
                    if (name == null) {
                        sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args);
                        return;
                    }
                    //creating empty world that player should add to config
                    if (WorldUtils.createEmptyWorld(name) == null)
                        sendMessage(sender, MessageType.ARENA_CREATED_ERROR, name);
                    else sendMessage(sender, MessageType.ARENA_CREATED_SUCCESSFULLY, name);
                    //done
                    return;
                }
            }

            // bridge ffa kit
            case "kit": {
                if (args[2].equalsIgnoreCase("give")) {
                    // bridge ffa kit give <kit> (player)
                    final String name = args[3];
                    if (!FFAKitManager.isKitCreated(name)) {
                        sendMessage(sender, MessageType.KIT_NOT_CREATED, name);
                        return;
                    }
                    // initialize player
                    Player player = null;
                    if (args.length == 4 && sender instanceof Player p) player = p;
                    else if (args.length == 5 && PlayerConverter.getPlayer(args[4]) != null)
                        player = PlayerConverter.getPlayer(args[4]);
                    if (player == null) {
                        sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args);
                        return;
                    }
                    // check perm
                    if (!permManager.hasPermission(player, "bridge.ffa.kit." + name)) {
                        sendMessage(sender, MessageType.NO_PERMISSION);
                        return;
                    }
                    final HashMap<Integer, FFAKitItem> kit = FFAKitManager.getKit(name);
                    if (kit.isEmpty()) { // if kit empty, it means nothing there or an error occurred
                        sendMessage(sender, MessageType.KIT_NOT_CREATED, name);
                        return;
                    }
                    FFAKitManager.applyKit(player, name);
                    //done
                    return;

                } else if (args[2].equalsIgnoreCase("create")) {
                    // bridge ffa kit create <name>
                    if (!(sender instanceof final Player player)) {
                        sendMessage(sender, MessageType.NEED_TO_BE_PLAYER);
                        return;
                    }
                    if (noPermission(player, Permission.FFA_KIT_CREATE)) return;

                    final String name = args[3];
                    final PlayerInventory inv = player.getInventory();
                    //adding all items to the list
                    final List<FFAKitItem> items = new ArrayList<>();
                    for (final Material item : Material.values()) {
                        if(item == null) continue;
                        final HashMap<Integer, ? extends ItemStack> map = inv.all(item);
                        if (map.isEmpty()) continue;
                        for (final int slot : map.keySet()) {
                            if (IAManager.isActive()) {
                                final CustomStack customItem = CustomStack.byItemStack(map.get(slot));
                                if (customItem != null) items.add(FFAKitManager.createKitItem(name, slot, customItem));
                                else items.add(FFAKitManager.createKitItem(name, slot, map.get(slot)));
                            } else items.add(FFAKitManager.createKitItem(name, slot, map.get(slot)));
                        }
                    }
                    //get and set armor settings
                    final ItemStack helmet = inv.getHelmet();
                    final ItemStack chestplate = inv.getChestplate();
                    final ItemStack leggings = inv.getLeggings();
                    final ItemStack boots = inv.getBoots();
                    if (helmet != null) FFAKitManager.setHelmet(name, helmet);
                    if (chestplate != null) FFAKitManager.setChestplate(name, chestplate);
                    if (leggings != null) FFAKitManager.setLeggings(name, leggings);
                    if (boots != null) FFAKitManager.setBoots(name, boots);
                    //save everything to config
                    try {
                        FFAKitManager.saveAll(items);
                        sendMessage(player, MessageType.FFA_KIT_CREATED_SUCCESSFULLY, name);
                    } catch (IOException e) {
                        sendMessage(player, MessageType.FFA_KIT_CREATED_ERROR, name);
                        LOG.error("An error occurred while tried to save FFA's kit item in config!", e);
                    }
                    //done
                    return;
                }
            }

            // bridge ffa teleport
            case "teleport": {
                // bridge ffa teleport <arena> random/default/<pos> (player)
                if (args.length < 4 || args.length > 5) {
                    sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args);
                    return;
                }
                //check perm
                if (noPermission(sender, Permission.FFA_ARENA_TELEPORT)) return;
                //initialize arena
                final String arena = args[2];
                if (!FFAArenaManager.getExistingFFAWorlds().contains(arena)) {
                    sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args[2]);
                    return;
                }
                //loading world
                final World world = WorldUtils.getWorld(arena);
                if (world == null) WorldUtils.loadWorld(arena);

                //reloading list of enabled worlds
                FFAArenaManager.getActiveFFAWorlds(true);

                //initialize location
                final Location choice;
                if (args[3].equals("default"))
                    choice = FFAArenaManager.getDefaultTeleportPoint(arena);
                else if (args[3].equals("random"))
                    choice = FFAArenaManager.randomTeleportLocation(arena);
                else
                    choice = FFAArenaManager.getTeleportPoint(arena, args[3]);
                if (choice == null) {
                    sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args[3]);
                    return;
                }
                //initialize player
                Player player = null;
                if (args.length == 4 && sender instanceof Player p) player = p;
                else if (args.length == 5 && PlayerConverter.getPlayer(args[4]) != null)
                    player = PlayerConverter.getPlayer(args[4]);
                if (player == null) {
                    sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args);
                    return;
                }
                player.teleport(choice);
                //done
                return;
            }

            // bridge ffa kills
            case "kills": {
                // bridge ffa kills set/add #Amount (player)
                if (args.length < 4 || args.length > 5) {
                    sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args);
                    return;
                }
                if(noPermission(sender, Permission.FFA_MANAGE_INFO)) return;

                //initialize player
                Player player = null;
                if (args.length == 4 && sender instanceof Player p) player = p;
                else if (args.length == 5 && PlayerConverter.getPlayer(args[4]) != null)
                    player = PlayerConverter.getPlayer(args[4]);
                if (player == null) {
                    sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args);
                    return;
                }

                final Connector con = new Connector(Bridge.getInstance().getDB());
                final int amount = Integer.parseInt(args[3]);
                final MessageType type;
                if(args[3].equalsIgnoreCase("set")) {
                    con.updateSQL(UpdateType.SET_FFA_KILLS, String.valueOf(amount), player.getUniqueId().toString());
                    type = MessageType.SET;
                }
                else if (args[3].equalsIgnoreCase("add")) {
                    con.updateSQL(UpdateType.ADD_FFA_KILLS, String.valueOf(amount), player.getUniqueId().toString());
                    type = MessageType.ADD;
                }
                else {
                    sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args);
                    return;
                }
                //send success message
                if (args.length == 5) sendMessage(sender, MessageType.FFA_OTHER_INFO_CHANGED,
                        Config.getMessage(type), player.getName(), String.valueOf(amount),
                        Config.getMessage(MessageType.KILLS));
                sendMessage(player, MessageType.FFA_YOUR_INFO_CHANGED,
                        Config.getMessage(type), String.valueOf(amount),
                        Config.getMessage(MessageType.KILLS));
                return;
            }

            // bridge ffa deaths
            case "deaths": {
                // bridge ffa deaths set/add #Amount (player)
                if (args.length < 4 || args.length > 5) {
                    sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args);
                    return;
                }
                if(noPermission(sender, Permission.FFA_MANAGE_INFO)) return;

                //initialize player
                Player player = null;
                if (args.length == 4 && sender instanceof Player p) player = p;
                else if (args.length == 5 && PlayerConverter.getPlayer(args[4]) != null)
                    player = PlayerConverter.getPlayer(args[4]);
                if (player == null) {
                    sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args);
                    return;
                }

                final Connector con = new Connector(Bridge.getInstance().getDB());
                final int amount = Integer.parseInt(args[3]);
                final MessageType type;
                if(args[3].equalsIgnoreCase("set")) {
                    con.updateSQL(UpdateType.SET_FFA_DEATHS, String.valueOf(amount), player.getUniqueId().toString());
                    type = MessageType.SET;
                }
                else if (args[3].equalsIgnoreCase("add")) {
                    con.updateSQL(UpdateType.ADD_FFA_DEATHS, String.valueOf(amount), player.getUniqueId().toString());
                    type = MessageType.ADD;
                }
                else {
                    sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args);
                    return;
                }
                //send success message
                if (args.length == 5) sendMessage(sender, MessageType.FFA_OTHER_INFO_CHANGED,
                        Config.getMessage(type), player.getName(), String.valueOf(amount),
                        Config.getMessage(MessageType.DEATHS));
                sendMessage(player, MessageType.FFA_YOUR_INFO_CHANGED,
                        Config.getMessage(type), String.valueOf(amount),
                        Config.getMessage(MessageType.DEATHS));
                return;
            }
        }
        sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args);
    }

    private @NotNull Optional<List<String>> completeFFA(final String @NotNull ... args) {
        // bridge ffa arena/kit/teleport
        if (args.length == 2) return Optional.of(List.of("arena", "kit", "teleport", "deaths", "kills"));
        if (args.length == 3) {
            switch (args[1]) {
                case "arena" -> {
                    return Optional.of(List.of("load", "unload", "create"));
                }
                case "kit" -> {
                    return Optional.of(List.of("create", "give"));
                }
                case "teleport" -> {
                    return Optional.of(FFAArenaManager.getExistingFFAWorlds());
                }
                case "deaths", "kills" -> {
                    return Optional.of(List.of("set", "add"));
                }
            }
        } else if (args.length == 4) {
            if (args[1].equalsIgnoreCase("arena"))
                // bridge ffa arena load/unload/create <#NAME>
                switch (args[2]) {
                    case "load" -> {
                        List<String> names = new ArrayList<>(FFAArenaManager.getExistingFFAWorlds());
                        FFAArenaManager.getActiveFFAWorlds(false).forEach(world -> names.remove(world.getName()));
                        return Optional.of(names);
                    }
                    case "unload" -> {
                        List<String> names = new ArrayList<>();
                        FFAArenaManager.getActiveFFAWorlds(false).forEach(world -> names.add(world.getName()));
                        return Optional.of(names);
                    }
                    case "create" -> {
                        return Optional.of(List.of("#NAME"));
                    }
                }
            else if (args[1].equalsIgnoreCase("kit"))
                // bridge ffa kit create/apply
                switch (args[2]) {
                    case "create" -> {
                        return Optional.of(List.of("#NAME"));
                    }
                    case "give" -> {
                        return Optional.of(FFAKitManager.getKits());
                    }
                }
            else if (args[1].equalsIgnoreCase("teleport")) {
                // bridge ffa teleport <arena> random/default/<pos>
                final List<String> names = new ArrayList<>(FFAArenaManager.getTeleportPointsName(args[2]));
                names.add("random");
                return Optional.of(names);
            }
            else if (args[1].equalsIgnoreCase("deaths") || args[1].equalsIgnoreCase("kills"))
                return Optional.of(List.of("#AMOUNT"));
        } else if (args.length == 5) {
            if (args[1].equalsIgnoreCase("teleport")) {
                // bridge ffa teleport <arena> random/default/<pos> (player)
                List<String> names = new ArrayList<>();
                Bukkit.getOnlinePlayers().forEach(p -> names.add(p.getName()));
                return Optional.of(names);
            }
            else if (args[1].equalsIgnoreCase("deaths") || args[1].equalsIgnoreCase("kills")) {
                // bridge ffa deaths/kills set/add #Amount (player)
                List<String> names = new ArrayList<>();
                Bukkit.getOnlinePlayers().forEach(p -> names.add(p.getName()));
                return Optional.of(names);
            }
        }
        return Optional.empty();
    }

    private void handleNickName(final CommandSender sender, final String @NotNull ... args) {
        if (noPermission(sender, Permission.COMMAND_NICKNAME)) return;
        NicknameManager manager = TABManager.getManager();
        if (manager == null) return;
        if (args.length < 2) {
            sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args);
            return;
        }
        if (args[1].equalsIgnoreCase("clear")) {
            if (noPermission(sender, Permission.NICKNAME_COLOR_REPLACE)) return;
            Bridge.getInstance().getSaver().add(new Saver.Record(UpdateType.DELETE_NICKNAME_DUPLICATES));
        } else if (args[1].equalsIgnoreCase("color")) {
            //bridge nickname color (cost/have)/set <color>
            if (args.length == 3) {
                if (args[2].equalsIgnoreCase("cost")) {
                    if (noPermission(sender, Permission.NICKNAME_COLOR_COST_OWN)) return;
                    if (sender instanceof Player player) {
                        NicknameManager.PlayerColor info = manager.getPlayerInfo(player.getUniqueId());
                        if (info == null) return;
                        if (!manager.getAllColorsName().contains(info.name())) {
                            sendMessage(player, MessageType.YOUR_NICKNAME_IS_UNIQUE);
                            return;
                        }
                        sendMessage(
                                player,
                                MessageType.YOUR_NICKNAME_COLOR_COST,
                                String.valueOf(manager.getColorCost(info.name()))
                        );
                    } else sendMessage(sender, MessageType.NEED_TO_BE_PLAYER);
                } else if (args[2].equalsIgnoreCase("have")) {
                    if (noPermission(sender, Permission.NICKNAME_COLOR_HAVE_OWN)) return;
                    if (sender instanceof Player player) {
                        NicknameManager.PlayerColor info = manager.getPlayerInfo(player.getUniqueId());
                        if (info == null) {
                            sendMessage(player, MessageType.YOUR_NICKNAME_IS_UNIQUE);
                            return;
                        }
                        if (info.name() == null) {
                            if (permManager.hasPermission(player, Permission.NICKNAME_COLOR_SET_HEX)) {
                                sendMessage(sender, MessageType.YOUR_NICKNAME_COLOR,
                                        info.gradient());
                                return;
                            }
                            sendMessage(player, MessageType.YOUR_NICKNAME_IS_UNIQUE);
                            return;
                        }
                        sendMessage(
                                player,
                                MessageType.YOUR_NICKNAME_COLOR,
                                info.name());
                    } else sendMessage(sender, MessageType.NEED_TO_BE_PLAYER);
                }
            }
            //bridge nickname color set/(cost/have) <COLOR>/<PLAYERS>
            else if (args.length == 4) {
                if (args[2].equalsIgnoreCase("set")) {
                    if (!(sender instanceof Player player)) {
                        sendMessage(sender, MessageType.NEED_TO_BE_PLAYER);
                        return;
                    }
                    if (noPermission(player, Permission.NICKNAME_COLOR_SET_OWN)) return;

                    final String gradient;
                    List<String> colorList = manager.getAllColorsName();

                    if (ColorCodes.isHexValid(args[3])) {
                        if (noPermission(sender, Permission.NICKNAME_COLOR_SET_HEX)) return;
                        gradient = args[3] + ">" + args[3];
                    } else if (manager.isGradient(args[3])) gradient = args[3];
                    else if (colorList.contains(args[3])) gradient = manager.getGradient(args[3]);
                    else {
                        sendMessage(player, MessageType.UNKNOWN_ARGUMENT, args[3]);
                        return;
                    }
                    Currency currency = TABManager.getStars();
                    if (currency != null) {
                        Integer cost = manager.getColorCost(args[3]);
                        if (cost == null) {
                            sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args[3]);
                            return;
                        }
                        int have = currency.getCurrencyAmount(player.getUniqueId());
                        if (have < cost) {
                            sendMessage(
                                    player,
                                    MessageType.NOT_ENOUGH_STARS,
                                    String.valueOf(cost - have));
                            return;
                        }
                    }
                    manager.applyColor(player, gradient, true);
                    sendMessage(player, MessageType.YOUR_NICKNAME_COLOR_CHANGED, args[3]);
                    //done!
                } else if (args[2].equalsIgnoreCase("cost")) {
                    if (noPermission(sender, Permission.NICKNAME_COLOR_COST_OTHER)) return;
                    Player player = PlayerConverter.getPlayer(args[3]);
                    if (player == null) {
                        sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args[3]);
                        return;
                    }

                    NicknameManager.PlayerColor info = manager.getPlayerInfo(player.getUniqueId());
                    if (info == null) return;
                    if (!manager.getAllColorsName().contains(info.name())) {
                        sendMessage(sender, MessageType.OTHER_PLAYER_NICKNAME_IS_UNIQUE, player.getName());
                        return;
                    }

                    sendMessage(
                            sender,
                            MessageType.OTHER_PLAYER_NICKNAME_COLOR_COST,
                            player.getName(),
                            String.valueOf(info.cost()));
                    //done!
                } else if (args[2].equalsIgnoreCase("have")) {
                    if (noPermission(sender, Permission.NICKNAME_COLOR_HAVE_OTHER)) return;
                    Player player = PlayerConverter.getPlayer(args[3]);
                    if (player == null) sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args[3]);
                    else {
                        NicknameManager.PlayerColor info = manager.getPlayerInfo(player.getUniqueId());
                        if (info == null) {
                            sendMessage(player, MessageType.YOUR_NICKNAME_IS_UNIQUE);
                            return;
                        }
                        if (info.name() == null) {
                            if (permManager.hasPermission(player, Permission.NICKNAME_COLOR_SET_HEX)) {
                                sendMessage(sender, MessageType.OTHER_PLAYER_NICKNAME_COLOR,
                                        player.getName(), info.gradient());
                                return;
                            }
                            sendMessage(sender, MessageType.OTHER_PLAYER_NICKNAME_IS_UNIQUE);
                            return;
                        }
                        sendMessage(
                                sender,
                                MessageType.OTHER_PLAYER_NICKNAME_COLOR,
                                player.getName(),
                                info.name());
                    }
                }
            }
            //bridge nickname color set/replace #color/<fromCOLOR> <PLAYERS>/<toCOLOR>
            else if (args.length == 5) {
                if (args[2].equalsIgnoreCase("replace")) {
                    if (noPermission(sender, Permission.NICKNAME_COLOR_REPLACE)) return;
                    final String gradient1;
                    if (ColorCodes.isHexValid(args[3])) {
                        if (noPermission(sender, Permission.NICKNAME_COLOR_SET_HEX)) return;
                        gradient1 = args[3] + ">" + args[3];
                    } else if (manager.isGradient(args[3])) gradient1 = args[3];
                    else if (manager.getAllColorsName().contains(args[3])) {
                        gradient1 = manager.getGradient(args[3]);
                    } else {
                        sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args[3]);
                        return;
                    }
                    final String gradient2;
                    if (ColorCodes.isHexValid(args[4])) {
                        if (noPermission(sender, Permission.NICKNAME_COLOR_SET_HEX)) return;
                        gradient2 = args[4] + ">" + args[4];
                    } else if (manager.isGradient(args[4])) gradient2 = args[4];
                    else if (manager.getAllColorsName().contains(args[4])) {
                        gradient2 = manager.getGradient(args[4]);
                    } else {
                        sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args[4]);
                        return;
                    }
                    if (manager.globallyReplaceColors(gradient1, gradient2))
                        sendMessage(sender, MessageType.REPLACE_COLORS_SUCCESSFULLY, gradient1, gradient2);
                    else sendMessage(sender, MessageType.REPLACE_COLORS_ERROR, gradient1, gradient2);
                } else if (args[2].equalsIgnoreCase("set")) {
                    if (noPermission(sender, Permission.NICKNAME_COLOR_SET_OTHER)) return;
                    final String gradient;
                    String ColorName = null;
                    if (ColorCodes.isHexValid(args[3])) {
                        if (noPermission(sender, Permission.NICKNAME_COLOR_SET_HEX)) return;
                        gradient = args[3] + ">" + args[3];
                    } else if (manager.isGradient(args[3])) gradient = args[3];
                    else if (manager.getAllColorsName().contains(args[3])) {
                        ColorName = args[3];
                        gradient = manager.getGradient(args[3]);
                    } else {
                        sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args[3]);
                        return;
                    }

                    Player p = PlayerConverter.getPlayer(args[4]);
                    if (p == null) {
                        sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args[4]);
                        return;
                    }
                    manager.applyColor(p, gradient, true);
                    final String result = ColorName == null ? gradient : ColorName;
                    sendMessage(p, MessageType.YOUR_NICKNAME_COLOR_CHANGED, result);
                    sendMessage(sender, MessageType.OTHER_PLAYER_NICKNAME_COLOR_CHANGED, p.getName(), result);
                }
            }
        } else if (args[1].equalsIgnoreCase("stars")) {

            //bridge nickname stars set <amount> <PLAYERS>
            if (args[2].equalsIgnoreCase("set")) {
                UUID uuid = null;
                if (args.length == 4) {
                    if (noPermission(sender, Permission.NICKNAME_STARS_SET_OWN)) return;
                    if (sender instanceof Player player) uuid = player.getUniqueId();
                    else {
                        sendMessage(sender, MessageType.NEED_TO_BE_PLAYER);
                        return;
                    }
                } else if (args.length == 5) {
                    if (noPermission(sender, Permission.NICKNAME_STARS_SET_OTHER)) return;
                    Player player = PlayerConverter.getPlayer(args[4]);
                    if (player == null) {
                        sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args[4]);
                        return;
                    }
                    uuid = player.getUniqueId();
                }
                if (uuid != null) {
                    Currency currency = TABManager.getStars();
                    if (currency == null) {
                        sendMessage(sender, MessageType.MODULE_STATE, ModuleManager.NICKNAME.getModule().getName(), Config.getMessage(MessageType.DISABLED));
                        return;
                    }
                    currency.setCurrency(uuid, Integer.parseInt(args[3]));
                    Player player = PlayerConverter.getPlayer(uuid);
                    if (player == null) {
                        sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args[4]);
                        return;
                    }
                    sendMessage(sender, MessageType.OTHER_PLAYER_NICKNAME_STARS_CHANGED,
                            args[3],
                            args.length == 4 ? Config.getMessage(MessageType.SELF)
                                    : player.getName());

                } else sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args);
                //done!
            }

            //bridge nickname stars add <amount> <PLAYERS>
            else if (args[2].equalsIgnoreCase("add")) {
                Currency currency = TABManager.getStars();
                if (currency == null) {
                    sendMessage(
                            sender,
                            MessageType.MODULE_STATE,
                            "UseStars",
                            Config.getMessage(MessageType.DISABLED));
                    return;
                }
                if (args.length == 4) {
                    if (noPermission(sender, Permission.NICKNAME_STARS_ADD_OWN)) return;
                    if (sender instanceof Player player) {
                        int toAdd = Integer.parseInt(args[3], 10);
                        int have = currency.getCurrencyAmount(player.getUniqueId());
                        currency.setCurrency(player.getUniqueId(), have + toAdd);
                        sendMessage(player, MessageType.YOUR_NICKNAME_STARS_CHANGED, String.valueOf(have + toAdd));
                    } else sendMessage(sender, MessageType.NEED_TO_BE_PLAYER);
                } else if (args.length == 5) {
                    if (noPermission(sender, Permission.NICKNAME_STARS_ADD_OTHER)) return;
                    Player p = PlayerConverter.getPlayer(args[4]);
                    if (p == null) sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args[4]);
                    else {
                        int toAdd = Integer.parseInt(args[3], 10);
                        int have = currency.getCurrencyAmount(p.getUniqueId());
                        currency.setCurrency(p.getUniqueId(), have + toAdd);
                        sendMessage(p, MessageType.YOUR_NICKNAME_STARS_CHANGED, String.valueOf(have + toAdd));
                        sendMessage(sender, MessageType.OTHER_PLAYER_NICKNAME_STARS_CHANGED, String.valueOf(have + toAdd), p.getName());
                    }
                }
                //done!
            }

            //bridge nickname stars have <PLAYERS>
            else if (args[2].equalsIgnoreCase("have")) {
                Currency currency = TABManager.getStars();
                if (currency == null) {
                    sendMessage(
                            sender,
                            MessageType.MODULE_STATE,
                            "UseStars",
                            Config.getMessage(MessageType.DISABLED));
                    return;
                }
                if (args.length == 3) {
                    if (noPermission(sender, Permission.NICKNAME_STARS_HAVE_OWN)) return;
                    if (sender instanceof Player player) {
                        sendMessage(
                                player,
                                MessageType.YOUR_NICKNAME_STARS,
                                String.valueOf(currency.getCurrencyAmount(player.getUniqueId()))
                        );
                    } else sendMessage(sender, MessageType.NEED_TO_BE_PLAYER);
                } else if (args.length == 4) {
                    if (noPermission(sender, Permission.NICKNAME_STARS_HAVE_OTHER)) return;
                    Player p = PlayerConverter.getPlayer(args[3]);
                    if (p == null) sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args[3]);
                    else sendMessage(
                            sender,
                            MessageType.OTHER_PLAYER_NICKNAME_STARS,
                            p.getName(),
                            String.valueOf(currency.getCurrencyAmount(p.getUniqueId()))
                    );
                }
                //done!
            }
        } else sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args);
    }

    private @NotNull Optional<List<String>> completeNickname(final String @NotNull ... args) {
        // bridge nickname color/stars
        if (args.length == 2) return Optional.of(Arrays.asList("color", "stars"));
        // bridge nickname color (have/cost)/set/replace <PLAYERS>/<COLORS>/fromColor <PLAYERS>/toColor
        if (args[1].equalsIgnoreCase("color")) {
            if (args.length == 3) return Optional.of(Arrays.asList("have", "cost", "set", "replace"));
            else if (args.length == 4) {
                switch (args[2]) {
                    case "have", "cost" -> {
                        List<String> names = new ArrayList<>();
                        Bukkit.getOnlinePlayers().forEach((p) -> names.add(p.getName()));
                        return Optional.of(names);
                    }
                    case "set" -> {
                        NicknameManager manager = TABManager.getManager();
                        if (manager == null) return Optional.empty();
                        return Optional.of(manager.getAllColorsName());
                    }
                    case "replace" -> {
                        return Optional.of(List.of("#HEX"));
                    }
                }
            } else if (args.length == 5) {
                if (args[2].equalsIgnoreCase("set")) {
                    List<String> names = new ArrayList<>();
                    Bukkit.getOnlinePlayers().forEach((p) -> names.add(p.getName()));
                    return Optional.of(names);
                } else if (args[2].equalsIgnoreCase("replace")) return Optional.of(List.of("#HEX"));
            }
        }
        // bridge nickname stars (set/add)/have <AMOUNT>/<PLAYERS
        else if (args[1].equalsIgnoreCase("stars")) {
            if (args.length == 3) return Optional.of(Arrays.asList("set", "add", "have"));
            else if (args.length == 4 && args[2].equalsIgnoreCase("have")) {
                List<String> names = new ArrayList<>();
                Bukkit.getOnlinePlayers().forEach((p) -> names.add(p.getName()));
                return Optional.of(names);
            }
        }
        return Optional.empty();
    }

    private void handleLanguage(final CommandSender sender, final String @NotNull ... args) {
        if (noPermission(sender, Permission.COMMAND_LANGUAGE)) return;
        if (args.length == 1) {
            sendMessage(sender, MessageType.CURRENT_LANGUAGE, Config.getLanguage());
            return;
        }

        final String language = Config.getLanguage();

        if (Config.getLanguages().contains(args[1]) && args.length == 2) {
            if (language.equalsIgnoreCase(args[1])) {
                sendMessage(sender, MessageType.ALREADY_LANGUAGE, language);
                return;
            }

            try {
                Config.setLanguage(language);
            } catch (final IllegalArgumentException e) {
                sendMessage(sender, MessageType.NO_SUCH_LANGUAGE);
                return;
            }
            instance.getPluginConfig().set("settings.language", language);
            sendMessage(sender, MessageType.SET_LANGUAGE_SUCCESSFULLY, language);
            return;
        }
        sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args);
    }

    private @NotNull Optional<List<String>> completeLanguage(final String @NotNull ... args) {
        if (args.length == 2) {
            return Optional.of(Config.getLanguages());
        }
        return Optional.of(new ArrayList<>());
    }

    private void handleDebug(final CommandSender sender, final String @NotNull ... args) {
        if (noPermission(sender, Permission.COMMAND_DEBUG)) return;
        if (args.length == 1) {
            sendMessage(sender, MessageType.DEBUGGING,
                    DebugHandlerConfig.isDebugging() ?
                            Config.getMessage(MessageType.ENABLED) :
                            Config.getMessage(MessageType.DISABLED));
            return;
        }

        final Boolean input = "true".equalsIgnoreCase(args[1]) ? Boolean.TRUE
                : "false".equalsIgnoreCase(args[1]) ? Boolean.FALSE : null;
        if (input != null && args.length == 2) {

            if (DebugHandlerConfig.isDebugging() && input || !DebugHandlerConfig.isDebugging() && !input) {
                sendMessage(sender, MessageType.ALREADY_DEBUGGING,
                        DebugHandlerConfig.isDebugging() ?
                                Config.getMessage(MessageType.ENABLED) :
                                Config.getMessage(MessageType.DISABLED));
                return;
            }

            try {
                DebugHandlerConfig.setDebugging(input);
            } catch (final IOException e) {
                sendMessage(sender, MessageType.SET_DEBUG_ERROR);
                LOG.warn("Could not save new debugging state to configuration file! " + e.getMessage(), e);
            }
            sendMessage(sender, MessageType.SET_DEBUG_SUCCESSFULLY,
                    DebugHandlerConfig.isDebugging() ?
                            Config.getMessage(MessageType.ENABLED) :
                            Config.getMessage(MessageType.DISABLED));
            return;
        }
        sendMessage(sender, MessageType.UNKNOWN_ARGUMENT, args);
    }

    private @NotNull Optional<List<String>> completeDebug(final String @NotNull ... args) {
        if (args.length == 2) {
            return Optional.of(Arrays.asList("true", "false"));
        }
        return Optional.of(new ArrayList<>());
    }

    private void sendMessage(final CommandSender sender, final MessageType msg) {
        sendMessage(sender, msg, (String[]) null);
    }

    private void sendMessage(final CommandSender sender, final MessageType msg, final String... variables) {
        if (sender instanceof Player player) {
            Config.sendMessage(player, msg, variables);
        } else {
            sender.sendMessage(Config.parseMessage(Config.getMessage(msg, variables)));
        }
    }

    private boolean noPermission(@NotNull CommandSender sender, @NotNull String perm) {
        if (sender instanceof Player player) {
            if (!permManager.hasPermission(player, perm)) {
                sendMessage(sender, MessageType.NO_PERMISSION);
                return true;
            }
        } else if (!sender.hasPermission(perm)) {
            sendMessage(sender, MessageType.NO_PERMISSION);
            return true;
        }
        return false;
    }*/
}
