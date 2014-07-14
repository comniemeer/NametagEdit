package ca.wacos.nametagedit;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import ca.wacos.nametagedit.NametagChangeEvent.NametagChangeReason;
import ca.wacos.nametagedit.utils.UUIDFetcher;

import com.google.common.base.Joiner;

/**
 * This class is responsible for handling the /ne command.
 * 
 * @author Levi Webb Heavily edited by @sgtcaze
 * 
 */
@SuppressWarnings("deprecation")
public class NametagCommand implements CommandExecutor {

	private NametagEdit plugin;

	public NametagCommand(NametagEdit plugin) {
		this.plugin = plugin;
	}

	String prefix = "§4[§3NametagEdit§4] ";

	public boolean onCommand(CommandSender sender, Command cmd, String label,
			final String[] args) {

		if (sender != null) {
			if (!sender.hasPermission("nametagedit.use")) {
				sender.sendMessage(prefix
						+ "§fYou do not have §cpermission §fto use this command.");
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
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("clear")) {
				if (!sender.hasPermission("nametagedit.clear")) {
					sender.sendMessage(prefix
							+ "§cYou can only edit your own nametag.");
					return true;
				}

				Player target = Bukkit.getPlayer(args[1]);

				if (target != null) {
					String uuid = target.getUniqueId().toString();

					NametagManager.clear(target.getName());

					if (plugin.getNTEHandler().playerData.containsKey(uuid)) {
						plugin.getNTEHandler().playerData.remove(uuid);
					}
				}
			} else if (args[0].equalsIgnoreCase("reload")) {
				if (!sender.hasPermission("nametagedit.reload")) {
					sender.sendMessage(prefix
							+ "§cYou don't have permission to reload this plugin.");
					return true;
				}

				if (args[1].equalsIgnoreCase("hard")) {
					plugin.getNTEHandler().hardReload();
					sender.sendMessage(prefix
							+ "§fSuccessfully §areloaded §fthe files.");
				} else if (args[1].equalsIgnoreCase("soft")) {
					plugin.getNTEHandler().softReload();
					sender.sendMessage(prefix
							+ "§fSuccessfully §areloaded §fthe files.");
				}
			}
		} else if (args.length > 2) {

			if (!sender.hasPermission("nametagedit.edittags")) {
				sender.sendMessage(prefix
						+ "§cYou can only edit your own nametag.");
				return true;
			}

			final String targetName = args[1];

			String oper = Joiner.on(" ")
					.join(Arrays.copyOfRange(args, 2, args.length))
					.replaceAll("'", "").replaceAll("&", "§");

			if (args[0].equalsIgnoreCase("prefix")) {
				setType(sender, targetName, "Prefix", NametagAPI.trim(oper));
			} else if (args[0].equalsIgnoreCase("suffix")) {
				setType(sender, targetName, "Suffix", NametagAPI.trim(oper));
			}
		}
		return true;
	}

	/**
	 * Updates the playerData hashmap and reloads the content async if the
	 * player is offline (to get their UUID)
	 */
	public void setType(final CommandSender sender, final String targetName,
			final String type, final String args) {

		NametagChangeReason reason = null;

		int tempId = 0;

		switch (type) {
		case "Prefix":
			reason = NametagChangeReason.SET_PREFIX;
			tempId = 1;
			break;
		case "Suffix":
			reason = NametagChangeReason.SET_SUFFIX;
			tempId = 2;
			break;
		}

		final int finId = tempId;

		Player target = Bukkit.getPlayer(targetName);

		if (target != null) {

			String uuid = target.getUniqueId().toString();

			if (!plugin.getNTEHandler().playerData.containsKey(uuid)) {
				plugin.getNTEHandler().playerData.put(uuid,
						Arrays.asList(targetName, "", ""));
			}

			plugin.getNTEHandler().playerData.get(uuid).set(finId, args);

			if (reason == NametagChangeReason.SET_PREFIX) {
				setNametagSoft(target.getName(), args, "", reason);
			} else if (reason == NametagChangeReason.SET_SUFFIX) {
				setNametagSoft(target.getName(), "", args, reason);
			}
		} else {
			new BukkitRunnable() {
				@Override
				public void run() {
					String temp = "";

					try {
						temp = UUIDFetcher.getUUIDOf(targetName).toString();
					} catch (Exception e) {
						sender.sendMessage(prefix
								+ "§cCould not retrieve the UUID for: §f"
								+ targetName);
						return;
					}

					final String uuid = temp;

					new BukkitRunnable() {
						@Override
						public void run() {
							if (!plugin.getNTEHandler().playerData
									.containsKey(uuid)) {
								plugin.getNTEHandler().playerData.put(uuid,
										Arrays.asList(targetName, "", ""));
							}

							plugin.getNTEHandler().playerData.get(uuid).set(
									finId, args);
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