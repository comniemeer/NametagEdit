package ca.wacos.nametagedit;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import ca.wacos.nametagedit.NametagChangeEvent.NametagChangeReason;
import ca.wacos.nametagedit.utils.UUIDFetcher;

/**
 * This class is responsible for handling the /ne command.
 * 
 * @author Levi Webb Heavily edited by @sgtcaze
 * 
 */
public class NametagCommand implements CommandExecutor {

	private NametagEdit plugin;

	public NametagCommand(NametagEdit plugin) {
		this.plugin = plugin;
	}

	String prefix = "§4[§3NametagEdit§4] ";

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String label,
			final String[] args) {

		if (sender != null) {
			if (!sender.hasPermission("nametagedit.use")) {
				sender.sendMessage(prefix
						+ "§fYou don not have §cpermission §fto use this command.");
				return true;
			}
		}

		if (args.length < 1) {
			sender.sendMessage(prefix + "§fCommand usage:");
			sender.sendMessage(prefix
					+ "§c/ne prefix [name] [text] §fsets a player's prefix");
			sender.sendMessage(prefix
					+ "§c/ne suffix [name] [text] §fsets a player's suffix");
			sender.sendMessage(prefix
					+ "§c/ne clear [name] §fclears a player's tags");
			sender.sendMessage(prefix
					+ "§c/ne reload §freloads the players/groups files");
		} else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {

			if (!sender.hasPermission("nametagedit.reload")) {
				sender.sendMessage(prefix
						+ "§cYou don't have permission to reload this plugin.");
				return true;
			}

			plugin.getNTEHandler().reload();

			sender.sendMessage(prefix
					+ "§fSuccessfully §areloaded §fthe files.");
		} else if (args.length == 2 && args[0].equalsIgnoreCase("clear")) {

			if (!sender.hasPermission("nametagedit.clear")) {
				sender.sendMessage("§cYou can only edit your own nametag.");
				return true;
			}

			Player target = Bukkit.getPlayer(args[1]);

			if (target != null) {
				String uuid = target.getUniqueId().toString();

				NametagManager.clear(target.getName());
				if (plugin.players.contains("Players." + uuid)) {
					plugin.players.set("Players." + uuid + ".Prefix", "");
					plugin.players.set("Players." + uuid + ".Suffix", "");
					plugin.getFileUtils().saveYamls();
				}
			}
		} else if (args.length == 3) {

			if (!sender.hasPermission("nametagedit.edittags")) {
				sender.sendMessage("§cYou can only edit your own nametag.");
				return true;
			}

			final String targetName = args[1];

			if (args[0].equalsIgnoreCase("prefix")) {
				setType(targetName, "Prefix", args[2]);
			} else if (args[0].equalsIgnoreCase("suffix")) {
				setType(targetName, "Suffix", args[2]);
			}
		}
		return true;
	}

	/**
	 * Updates the players.yml file with the tags, and the reason Calls are made
	 * async if the player is offline (to get their UUID)
	 */
	@SuppressWarnings("deprecation")
	public void setType(final String targetName, final String type,
			final String args) {

		NametagChangeReason reason = null;

		switch (type) {
		case "Prefix":
			reason = NametagChangeReason.SET_PREFIX;
			break;
		case "Suffix":
			reason = NametagChangeReason.SET_SUFFIX;
			break;
		}

		Player target = Bukkit.getPlayer(targetName);

		if (target != null) {

			String uuid = target.getUniqueId().toString();

			if (!plugin.players.contains("Players." + uuid)) {
				plugin.players.set("Players." + uuid + ".Name", targetName);
				plugin.players.set("Players." + uuid + ".Prefix", "");
				plugin.players.set("Players." + uuid + ".Suffix", "");
			}

			plugin.players.set("Players." + uuid + "." + type, args);

			plugin.getFileUtils().saveYamls();

			plugin.getNTEHandler().reload();

			setNametagSoft(target.getName(), args.replaceAll("&", "§"), "",
					reason);
		} else {
			new BukkitRunnable() {
				@Override
				public void run() {
					String temp = "";

					try {
						temp = UUIDFetcher.getUUIDOf(targetName).toString();
					} catch (Exception e) {
						e.printStackTrace();
					}

					final String uuid = temp;

					new BukkitRunnable() {
						@Override
						public void run() {
							if (!plugin.players.contains("Players." + uuid)) {
								plugin.players.set("Players." + uuid + ".Name",
										targetName);
								plugin.players.set("Players." + uuid
										+ ".Prefix", "");
								plugin.players.set("Players." + uuid
										+ ".Suffix", "");
							}

							plugin.players.set("Players." + uuid + "." + type,
									args);

							plugin.getFileUtils().saveYamls();

							plugin.getNTEHandler().reload();
						}
					}.runTask(plugin);
				}
			}.runTaskAsynchronously(plugin);
		}
	}

	/**
	 * Sets a player's nametag with the given information and additional reason.
	 * 
	 * @param player
	 *            the player whose nametag to set
	 * @param prefix
	 *            the prefix to set
	 * @param suffix
	 *            the suffix to set
	 * @param reason
	 *            the reason for setting the nametag
	 */
	void setNametagHard(String player, String prefix, String suffix,
			NametagChangeEvent.NametagChangeReason reason) {
		NametagChangeEvent e = new NametagChangeEvent(player,
				NametagAPI.getPrefix(player), NametagAPI.getSuffix(player),
				prefix, suffix, NametagChangeEvent.NametagChangeType.HARD,
				reason);
		Bukkit.getServer().getPluginManager().callEvent(e);
		if (!e.isCancelled()) {
			NametagManager.overlap(player, prefix, suffix);
		}
	}

	/**
	 * Sets a player's nametag with the given information and additional reason.
	 * 
	 * @param player
	 *            the player whose nametag to set
	 * @param prefix
	 *            the prefix to set
	 * @param suffix
	 *            the suffix to set
	 * @param reason
	 *            the reason for setting the nametag
	 */
	public static void setNametagSoft(String player, String prefix,
			String suffix, NametagChangeEvent.NametagChangeReason reason) {
		NametagChangeEvent e = new NametagChangeEvent(player,
				NametagAPI.getPrefix(player), NametagAPI.getSuffix(player),
				prefix, suffix, NametagChangeEvent.NametagChangeType.SOFT,
				reason);
		Bukkit.getServer().getPluginManager().callEvent(e);
		if (!e.isCancelled()) {
			NametagManager.update(player, prefix, suffix);
		}
	}
}