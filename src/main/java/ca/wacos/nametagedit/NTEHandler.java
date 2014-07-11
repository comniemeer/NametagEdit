package ca.wacos.nametagedit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * This class loads all group/player data, and applies the tags during
 * reloads/individually
 * 
 * @author sgtcaze
 */
public class NTEHandler {

	private NametagEdit plugin;

	public NTEHandler(NametagEdit plugin) {
		this.plugin = plugin;
	}

	private HashMap<String, List<String>> groupData = new HashMap<>();
	private HashMap<String, List<String>> playerData = new HashMap<>();
	private HashMap<String, String> permissions = new HashMap<>();

	public void reload() {
		plugin.reloadConfig();
		plugin.getFileUtils().loadYamls();
		loadGroups();
		loadPlayers();
		applyTags();
	}

	public void loadGroups() {
		groupData.clear();

		for (String s : plugin.groups.getConfigurationSection("Groups")
				.getKeys(false)) {
			List<String> tempData = new ArrayList<>();
			String prefix = plugin.groups.getString("Groups." + s + ".Prefix");
			String suffix = plugin.groups.getString("Groups." + s + ".Suffix");
			String permission = plugin.groups.getString("Groups." + s
					+ ".Permission");

			tempData.add(format(prefix));
			tempData.add(format(suffix));
			tempData.add(permission);

			groupData.put(s, tempData);
			permissions.put(permission, s);
		}
	}

	public void loadPlayers() {
		playerData.clear();

		for (String s : plugin.players.getConfigurationSection("Players")
				.getKeys(false)) {
			List<String> tempData = new ArrayList<>();
			String name = plugin.players.getString("Players." + s + ".Name");
			String prefix = plugin.players
					.getString("Players." + s + ".Prefix");
			String suffix = plugin.players
					.getString("Players." + s + ".Suffix");

			tempData.add(name);
			tempData.add(format(prefix));
			tempData.add(format(suffix));

			playerData.put(s, tempData);
		}
	}

	public void applyTags() {
		for (Player p : Bukkit.getOnlinePlayers()) {

			String uuid = p.getUniqueId().toString();

			if (playerData.containsKey(uuid)) {
				List<String> temp = playerData.get(uuid);
				NametagManager.overlap(p.getName(), temp.get(1), temp.get(2));
			} else {
				String permission = "";

				for (String s : groupData.keySet()) {
					List<String> temp = groupData.get(s);

					if (p.hasPermission(temp.get(2))) {
						permission = temp.get(2);
						break;
					}
				}

				String group = permissions.get(permission);
				List<String> temp = groupData.get(group);

				if (temp != null) {
					NametagCommand.setNametagSoft(p.getName(), temp.get(0),
							temp.get(1),
							NametagChangeEvent.NametagChangeReason.GROUP_NODE);
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

	public void applyTagToPlayer(Player p) {
		String uuid = p.getUniqueId().toString();

		if (playerData.containsKey(uuid)) {
			List<String> temp = playerData.get(uuid);
			NametagManager.overlap(p.getName(), temp.get(1), temp.get(2));
		} else {
			String permission = "";

			for (String s : groupData.keySet()) {
				List<String> temp = groupData.get(s);

				if (p.hasPermission(temp.get(2))) {
					permission = temp.get(2);
				}
			}

			String group = permissions.get(permission);
						
			List<String> temp = groupData.get(group);

			if (temp != null) {
				NametagCommand.setNametagSoft(p.getName(), temp.get(0),
						temp.get(1),
						NametagChangeEvent.NametagChangeReason.GROUP_NODE);
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

	private String format(String input) {
		return trim(input.replaceAll("&", "§"));
	}

	private String trim(String input) {
		if (input.length() > 16) {
			String temp = input;
			input = "";
			for (int t = 0; t < 16; t++) {
				input += temp.charAt(t);
			}
		}
		return input;
	}
}