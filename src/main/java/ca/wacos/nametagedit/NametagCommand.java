//package ca.wacos.nametagedit;
//
//import java.util.Arrays;
//
//import org.apache.commons.lang.StringUtils;
//import org.bukkit.Bukkit;
//import org.bukkit.ChatColor;
//import org.bukkit.command.Command;
//import org.bukkit.command.CommandExecutor;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//import org.bukkit.scheduler.BukkitRunnable;
//
//import ca.wacos.nametagedit.NametagChangeEvent.NametagChangeReason;
//import ca.wacos.nametagedit.NametagChangeEvent.NametagChangeType;
//import ca.wacos.nametagedit.core.NametagManager;
//import ca.wacos.nametagedit.utils.UUIDFetcher;
//
///**
// * This class is responsible for handling the /ne command.
// * 
// * @author Levi Webb Heavily edited by @sgtcaze
// * 
// */
//@SuppressWarnings("deprecation")
//public class NametagCommand implements CommandExecutor {
//
//    private NametagEdit plugin = NametagEdit.getInstance();
//
//    private String prefix = "§3NametagEdit §4» ", noPerm = prefix
//            + "§fYou do not have §cpermission §fto use this command.";
//
//    // Converts multiple arguments to one string
//    private String argsToString(String[] text, int to, int from) {
//        return ChatColor.translateAlternateColorCodes('&',
//                StringUtils.join(text, ' ', to, from)).replace("'", "");
//    }
//
//    // NametagEdit's base command
//    public boolean onCommand(final CommandSender sender, Command cmd,
//            String label, final String[] args) {
//        if (!sender.hasPermission("nametagedit.use")) {
//            sender.sendMessage(noPerm);
//            return true;
//        }
//
//        if (args.length < 1) {
//            cmdUsage(sender);
//        } else if (args.length >= 1) {
//            switch (args[0]) {
//            case "clear":
//                cmdClear(sender, args);
//                break;
//            case "reload":
//                cmdReload(sender, args);
//                break;
//            case "prefix":
//                cmdEdit(sender, args);
//                break;
//            case "suffix":
//                cmdEdit(sender, args);
//                break;
//            case "groups":
//                cmdGroups(sender, args);
//                break;
//            default:
//                sender.sendMessage(prefix + "§cUnrecognized sub-command \""
//                        + args[0] + "\"");
//            }
//        }
//
//        return true;
//    }
//
//    // Sends basic command usage
//    private void cmdUsage(CommandSender sender) {
//        sender.sendMessage(prefix + "§fCommand usage:");
//        sender.sendMessage(prefix
//                + "§c/ne reload §f§oreloads the players/groups files");
//        sender.sendMessage(prefix
//                + "§c/ne prefix [player] [text] §f§osets a player's prefix");
//        sender.sendMessage(prefix
//                + "§c/ne suffix [player] [text] §f§osets a player's suffix");
//        sender.sendMessage(prefix
//                + "§c/ne clear [player] §f§oclears a player's tags");
//        sender.sendMessage(prefix
//                + "§c/ne groups §f§oThe base command for the groups");
//    }
//
//    // Clears prefixes and suffixes
//    private void cmdClear(final CommandSender sender, String[] args) {
//        if (!(sender.hasPermission("nametagedit.clear.self") || sender
//                .hasPermission("nametagedit.clear.others"))) {
//            sender.sendMessage(noPerm);
//            return;
//        }
//
//        if (args.length != 2) {
//            sender.sendMessage(prefix + "§fUsage: /ne clear <player>");
//            return;
//        } else if (args.length == 2) {
//            final String targetName = args[1];
//
//            if (!sender.hasPermission("nametagedit.clear.others")
//                    && !targetName.equalsIgnoreCase(sender.getName())) {
//                sender.sendMessage(prefix
//                        + "§cYou can only clear your own tag.");
//                return;
//            }
//
//            Player target = Bukkit.getPlayer(targetName);
//
//            if (target != null) {
//                final String uuid = target.getUniqueId().toString();
//
//                NametagManager.clear(target.getName());
//
//                if (plugin.getNTEHandler().playerData.containsKey(uuid)) {
//                    plugin.getNTEHandler().playerData.remove(uuid);
//                }
//            } else {
//                new BukkitRunnable() {
//                    @Override
//                    public void run() {
//                        String temp = "";
//
//                        try {
//                            temp = UUIDFetcher.getUUIDOf(targetName).toString();
//                        } catch (Exception e) {
//                            sender.sendMessage(prefix
//                                    + "§fCould not retrieve the UUID for: §c"
//                                    + targetName);
//                            return;
//                        }
//
//                        final String uuid = temp;
//
//                        new BukkitRunnable() {
//                            @Override
//                            public void run() {
//                                if (plugin.getNTEHandler().playerData
//                                        .containsKey(uuid)) {
//                                    plugin.getNTEHandler().playerData
//                                            .remove(uuid);
//                                }
//                            }
//                        }.runTask(plugin);
//                    }
//                }.runTaskAsynchronously(plugin);
//            }
//        }
//    }
//
//    // Reloads from file or memory
//    private void cmdReload(CommandSender sender, String[] args) {
//        if (!sender.hasPermission("nametagedit.reload")) {
//            sender.sendMessage(noPerm);
//            return;
//        }
//
//        if (args.length != 2) {
//            sender.sendMessage(prefix + "§cFile§f/§cMemory");
//            sender.sendMessage(prefix
//                    + "§fReloading from file will accept active edits");
//            sender.sendMessage(prefix
//                    + "§fReloading from memory will overwrite file data from memory");
//        } else if (args[1].equalsIgnoreCase("file")) {
//            plugin.getNTEHandler().reload(sender, true);
//        } else if (args[1].equalsIgnoreCase("memory")) {
//            plugin.getNTEHandler().reload(sender, false);
//        }
//    }
//
//    // Sets prefix or suffix
//    private void cmdEdit(CommandSender sender, String[] args) {
//        if (!(sender.hasPermission("nametagedit.edit.self") || sender
//                .hasPermission("nametagedit.edit.others"))) {
//            sender.sendMessage(noPerm);
//            return;
//        }
//
//        if (args.length <= 2) {
//            sender.sendMessage(prefix
//                    + "§fUsage: /ne prefix/suffix <player> text");
//            return;
//        } else if (args.length > 2) {
//            String type = args[0].toLowerCase();
//
//            final String targetName = args[1];
//
//            if (!sender.hasPermission("nametagedit.edit.others")
//                    && !targetName.equalsIgnoreCase(sender.getName())) {
//                sender.sendMessage(prefix + "§cYou can only edit your own tag.");
//                return;
//            }
//
//            Player target = Bukkit.getPlayer(args[1]);
//
//            String oper = argsToString(args, 2, args.length);
//
//            setType(sender, targetName, type, NametagAPI.trim(oper));
//        }
//    }
//
//    // Groups subcommand
//    private void cmdGroups(CommandSender sender, String[] args) {
//        if (!sender.hasPermission("nametagedit.groups")) {
//            sender.sendMessage(noPerm);
//            return;
//        }
//
//        if (args.length >= 2) {
//
//            if (args[1].equalsIgnoreCase("list")) {
//                StringBuilder sb = new StringBuilder();
//
//                for (String s : plugin.getNTEHandler().allGroups) {
//                    sb.append(new StringBuilder().append("§6" + s)
//                            .append("§f, ").toString());
//                }
//
//                if (sb.length() > 0) {
//                    sb.setLength(sb.length() - 2);
//                }
//
//                sender.sendMessage(prefix + "§cLoaded Groups: §f"
//                        + sb.toString());
//            } else if (args[1].equalsIgnoreCase("remove")) {
//                if (args.length == 3) {
//                    String group = args[2];
//
//                    if (plugin.getNTEHandler().allGroups.contains(group)) {
//                        plugin.getNTEHandler().allGroups.remove(group);
//                        plugin.getNTEHandler().groupData.remove(group);
//                    }
//
//                    sender.sendMessage(prefix + "§fSuccessfully removed §c"
//                            + group);
//                }
//            } else if (args[1].equalsIgnoreCase("order")) {
//                if (sender instanceof Player) {
//                    plugin.createOrganizer((Player) sender);
//                }
//            } else if (args[1].equalsIgnoreCase("add")) {
//                if (args.length == 3) {
//                    String group = args[2];
//                    if (!plugin.getNTEHandler().groupData.containsKey(group)) {
//                        plugin.getNTEHandler().groupData.put(group,
//                                Arrays.asList("", "", ""));
//                        sender.sendMessage(prefix + "§fThe group §c" + group
//                                + " §fhas been added!");
//
//                        // MySQL To-Do
//                    } else {
//                        sender.sendMessage(prefix
//                                + "§cThis group already exists.");
//                    }
//                }
//            } else if (args[1].equalsIgnoreCase("set")) {
//                if (args.length >= 5) {
//                    String group = args[3];
//
//                    if (!plugin.getNTEHandler().groupData.containsKey(group)) {
//                        sender.sendMessage(prefix + "§fThe group §c" + group
//                                + " §fdoesn't exist.");
//                        return;
//                    }
//
//                    if (args[2].equalsIgnoreCase("perm")) {
//                        plugin.getNTEHandler().groupData.get(group).set(0,
//                                args[4]);
//                        plugin.getNTEHandler().permissions.remove(plugin
//                                .getNTEHandler().groupData.get(group).get(2));
//                        plugin.getNTEHandler().permissions.put(args[4], group);
//                        sender.sendMessage(prefix + "§fSet §c" + group
//                                + "§f's permission to: §c" + args[4]);
//                    } else if (args[2].equalsIgnoreCase("prefix")) {
//                        String oper = argsToString(args, 4, args.length);
//
//                        plugin.getNTEHandler().groupData.get(group).set(1,
//                                NametagAPI.trim(oper));
//
//                        sender.sendMessage(prefix + "§fSet §c" + args[3]
//                                + "§f's prefix to: §c" + NametagAPI.trim(oper));
//                    } else if (args[2].equalsIgnoreCase("suffix")) {
//                        String oper = argsToString(args, 4, args.length);
//
//                        plugin.getNTEHandler().groupData.get(group).set(2,
//                                NametagAPI.trim(oper));
//
//                        sender.sendMessage(prefix + "§fSet §c" + args[3]
//                                + "§f's suffix to: §c" + NametagAPI.trim(oper));
//                    }
//                } else {
//                    sender.sendMessage(prefix
//                            + "§fUsage §c/ne groups set [option] <group> <value>");
//                }
//            } else {
//                sender.sendMessage(prefix + "§cUnrecognized sub-command \""
//                        + args[1] + "\"");
//            }
//        }
//    }
//
//    /**
//     * Updates the playerData hashmap and reloads the content async if the
//     * player is offline (to get their UUID)
//     */
//    public void setType(final CommandSender sender, final String targetName,
//            final String type, final String args) {
//        NametagChangeReason reason = null;
//
//        int tempId = 0;
//
//        switch (type) {
//        case "prefix":
//            reason = NametagChangeReason.SET_PREFIX;
//            tempId = 1;
//            break;
//        case "suffix":
//            reason = NametagChangeReason.SET_SUFFIX;
//            tempId = 2;
//            break;
//        }
//
//        final int finId = tempId;
//
//        Player target = Bukkit.getPlayer(targetName);
//
//        if (target != null) {
//            String uuid = target.getUniqueId().toString();
//
//            if (!plugin.getNTEHandler().playerData.containsKey(uuid)) {
//                plugin.getNTEHandler().playerData.put(uuid,
//                        Arrays.asList(targetName, "", ""));
//            }
//
//            plugin.getNTEHandler().playerData.get(uuid).set(finId, args);
//
//            if (reason == NametagChangeReason.SET_PREFIX) {
//                setNametagSoft(target.getName(), args, "", reason);
//            } else if (reason == NametagChangeReason.SET_SUFFIX) {
//                setNametagSoft(target.getName(), "", args, reason);
//            }
//        } else {
//            new BukkitRunnable() {
//                @Override
//                public void run() {
//                    String temp = "";
//
//                    try {
//                        temp = UUIDFetcher.getUUIDOf(targetName).toString();
//                    } catch (Exception e) {
//                        sender.sendMessage(prefix
//                                + "§fCould not retrieve the UUID for: §c"
//                                + targetName);
//                        return;
//                    }
//
//                    final String uuid = temp;
//
//                    new BukkitRunnable() {
//                        @Override
//                        public void run() {
//                            if (!plugin.getNTEHandler().playerData
//                                    .containsKey(uuid)) {
//                                plugin.getNTEHandler().playerData.put(uuid,
//                                        Arrays.asList(targetName, "", ""));
//                            }
//
//                            plugin.getNTEHandler().playerData.get(uuid).set(
//                                    finId, args);
//                        }
//                    }.runTask(plugin);
//                }
//            }.runTaskAsynchronously(plugin);
//        }
//    }
//
//    /**
//     * Sets a player's nametag with the given information and additional reason.
//     * 
//     * @param player
//     *            the player whose nametag to set
//     * @param prefix
//     *            the prefix to set
//     * @param suffix
//     *            the suffix to set
//     * @param reason
//     *            the reason for setting the nametag
//     */
//    public static void setNametagSoft(String player, String prefix,
//            String suffix, NametagChangeReason reason) {
//        NametagChangeEvent e = new NametagChangeEvent(player,
//                NametagAPI.getPrefix(player), NametagAPI.getSuffix(player),
//                prefix, suffix, NametagChangeType.SOFT, reason);
//        Bukkit.getServer().getPluginManager().callEvent(e);
//        if (!e.isCancelled()) {
//            NametagManager.update(player, prefix, suffix);
//        }
//    }
//}