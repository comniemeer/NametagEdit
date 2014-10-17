package ca.wacos.nametagedit.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import ca.wacos.nametagedit.NametagAPI;
import ca.wacos.nametagedit.NametagChangeEvent.NametagChangeReason;
import ca.wacos.nametagedit.NametagCommand;
import ca.wacos.nametagedit.NametagEdit;
import ca.wacos.nametagedit.SQLData;

/**
 * This class loads all group/player data, and applies the tags during
 * reloads/individually
 * 
 * @author sgtcaze
 */
public class NametagHandler {

    private NametagEdit plugin = NametagEdit.getInstance();

    // Stores all group names in order
    public List<String> allGroups = new ArrayList<>();

    // Corresponds permission to group name
    public HashMap<String, String> permissions = new HashMap<>();

    // Stores all group names to permissions/prefix/suffix
    public HashMap<String, List<String>> groupData = new HashMap<>();

    // Stores all player names to prefix/suffix
    public HashMap<String, List<String>> playerData = new HashMap<>();

    // Reloads files, and reapplies tags
    public void reload(CommandSender sender, boolean fromFile) {
        if (plugin.databaseEnabled) {
            new SQLData().runTaskAsynchronously(plugin);
        } else {
            if (fromFile) {
                plugin.reloadConfig();
                plugin.getFileUtils().loadFiles();
            } else {
                plugin.saveConfig();
                saveFileData(plugin.getFileUtils().getPlayersFile(), plugin
                        .getFileUtils().getGroupsFile());
            }

            loadFromFile(plugin.getFileUtils().getPlayersFile(), plugin
                    .getFileUtils().getGroupsFile());

            applyTags();
        }

        sender.sendMessage("§3NametagEdit §4» §fSuccessfully reloaded files.");
    }

    // Workaround for the deprecated getOnlinePlayers()
    public List<Player> getOnline() {
        List<Player> list = new ArrayList<>();

        for (World world : Bukkit.getWorlds()) {
            list.addAll(world.getPlayers());
        }
        return Collections.unmodifiableList(list);
    }

    // Format input string
    private String format(String input) {
        return NametagAPI.trim(ChatColor.translateAlternateColorCodes('&',
                input));
    }

    // Saves all player and group data
    public void saveFileData(YamlConfiguration playersFile,
            YamlConfiguration groupsFile) {
        groupsFile.set("Order", allGroups);

        for (String s : playerData.keySet()) {
            List<String> temp = playerData.get(s);
            playersFile.set("Players." + s + ".Name",
                    temp.get(0).replace("§", "&"));
            playersFile.set("Players." + s + ".Prefix",
                    temp.get(1).replace("§", "&"));
            playersFile.set("Players." + s + ".Suffix",
                    temp.get(2).replace("§", "&"));
        }

        for (String s : groupData.keySet()) {
            List<String> temp = groupData.get(s);
            groupsFile.set("Groups." + s + ".Permission",
                    temp.get(0).replace("§", "&"));
            groupsFile.set("Groups." + s + ".Prefix",
                    temp.get(1).replace("§", "&"));
            groupsFile.set("Groups." + s + ".Suffix",
                    temp.get(2).replace("§", "&"));
        }

        plugin.getFileUtils().saveAllFiles();
    }

    // Loads all player and group data (file)
    public void loadFromFile(YamlConfiguration playersFile,
            YamlConfiguration groupsFile) {
        groupData.clear();
        playerData.clear();

        allGroups.clear();

        for (String s : groupsFile.getStringList("Order")) {
            allGroups.add(s);
        }

        for (String s : allGroups) {
            List<String> tempData = new ArrayList<>();
            String perm = groupsFile.getString("Groups." + s + ".Permission");
            String prefix = groupsFile.getString("Groups." + s + ".Prefix");
            String suffix = groupsFile.getString("Groups." + s + ".Suffix");

            tempData.add(perm);
            tempData.add(format(prefix));
            tempData.add(format(suffix));

            groupData.put(s, tempData);
            permissions.put(perm, s);
        }

        for (String s : playersFile.getConfigurationSection("Players").getKeys(
                false)) {
            List<String> tempData = new ArrayList<>();
            String name = playersFile.getString("Players." + s + ".Name");
            String prefix = playersFile.getString("Players." + s + ".Prefix");
            String suffix = playersFile.getString("Players." + s + ".Suffix");

            tempData.add(name);
            tempData.add(format(prefix));
            tempData.add(format(suffix));

            playerData.put(s, tempData);
        }
    }

    // Applies tags to online players (for /reload, and /ne reload)
    public void applyTags() {
        for (Player p : getOnline()) {
            if (p != null) {
                NametagManager.clear(p.getName());

                String uuid = p.getUniqueId().toString();

                if (playerData.containsKey(uuid)) {
                    List<String> temp = playerData.get(uuid);
                    NametagManager.overlap(p.getName(), temp.get(1),
                            temp.get(2));
                } else {
                    String permission = "";

                    Permission perm = null;

                    for (String s : allGroups) {
                        List<String> temp = groupData.get(s);
                        perm = new Permission(temp.get(0),
                                PermissionDefault.FALSE);
                        if (p.hasPermission(perm)) {
                            permission = temp.get(0);
                        }
                    }

                    String group = permissions.get(permission);
                    List<String> temp = groupData.get(group);

                    if (temp != null) {
                        NametagCommand.setNametagSoft(p.getName(), temp.get(1),
                                temp.get(2), NametagChangeReason.GROUP_NODE);
                    }

                    if (plugin.tabListDisabled) {
                        String str = "§f" + p.getName();
                        String tab = "";
                        for (int t = 0; t < str.length() && t < 16; t++) {
                            tab += str.charAt(t);
                        }
                        p.setPlayerListName(tab);
                    }
                }
            }
        }
    }

    // Applies tags to a specific player
    public void applyTagToPlayer(Player p) {
        String uuid = p.getUniqueId().toString();

        NametagManager.clear(p.getName());

        if (playerData.containsKey(uuid)) {
            List<String> temp = playerData.get(uuid);
            NametagManager.overlap(p.getName(), temp.get(1), temp.get(2));
        } else {
            String permission = "";

            Permission perm = null;

            for (String s : allGroups) {
                List<String> temp = groupData.get(s);
                perm = new Permission(temp.get(0), PermissionDefault.FALSE);
                if (p.hasPermission(perm)) {
                    permission = temp.get(0);
                }
            }

            String group = permissions.get(permission);

            List<String> temp = groupData.get(group);

            if (temp != null) {
                NametagCommand.setNametagSoft(p.getName(), temp.get(1),
                        temp.get(2), NametagChangeReason.GROUP_NODE);
            }
        }

        if (plugin.tabListDisabled) {
            String str = "§f" + p.getName();
            String tab = "";

            for (int t = 0; t < str.length() && t < 16; t++) {
                tab += str.charAt(t);
            }

            p.setPlayerListName(tab);
        }
    }
}