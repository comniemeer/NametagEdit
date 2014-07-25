package ca.wacos.nametagedit;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import ca.wacos.nametagedit.NametagChangeEvent.NametagChangeReason;
import ca.wacos.nametagedit.NametagChangeEvent.NametagChangeType;
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

	private String prefix = "§f[§6NametagEdit§f] ";
	private String noPerm = prefix
			+ "§fYou do not have §cpermission §fto use this command.";

	public boolean onCommand(final CommandSender sender, Command cmd,
			String label, final String[] args) {

		if (!sender.hasPermission("nametagedit.use")) {
			sender.sendMessage(noPerm);
			return true;
		}

		if (args.length < 1) {
			sender.sendMessage(prefix + "§fCommand usage:");
			sender.sendMessage(prefix
					+ "§6/ne reload §f§oreloads the players/groups files");
			sender.sendMessage(prefix
					+ "§6/ne prefix [player] [text] §f§osets a player's prefix");
			sender.sendMessage(prefix
					+ "§6/ne suffix [player] [text] §f§osets a player's suffix");
			sender.sendMessage(prefix
					+ "§6/ne clear [player] §f§oclears a player's tags");
			sender.sendMessage(prefix
					+ "§6/ne groups §f§oThe base command for the groups");
		} else if (args[0].equalsIgnoreCase("clear")) {
			if (!(sender.hasPermission("nametagedit.clear.self") || sender
					.hasPermission("nametagedit.clear.others"))) {
				sender.sendMessage(noPerm);
				return true;
			}

			if (args.length != 2) {
				sender.sendMessage(prefix + "Usage: /ne clear <player>");
				return true;
			} else if (args.length == 2) {
				final String targetName = args[1];

				if (!sender.hasPermission("nametagedit.clear.others") && !targetName.equalsIgnoreCase(sender.getName())) {
						sender.sendMessage(prefix + "§cYou can only clear your own tag.");
					return false;
				}

				Player target = Bukkit.getPlayer(targetName);

				if (target != null) {
					final String uuid = target.getUniqueId().toString();

					NametagManager.clear(target.getName());

					if (plugin.getNTEHandler().playerData.containsKey(uuid)) {
						plugin.getNTEHandler().playerData.remove(uuid);
						plugin.getNTEHandler().playerData.get(uuid).set(0, " ");
						plugin.getNTEHandler().playerData.get(uuid).set(1, " ");
						plugin.getNTEHandler().playerData.get(uuid).set(2, " ");
					}

					if (plugin.databaseEnabled) {
						new BukkitRunnable() {
							@Override
							public void run() {
								plugin.getMySQL().purgeType("players", "uuid",
										uuid);
							}
						}.runTaskAsynchronously(plugin);
					}
				} else {
					new BukkitRunnable() {
						@Override
						public void run() {
							String temp = "";

							try {
								temp = UUIDFetcher.getUUIDOf(targetName)
										.toString();
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
									if (plugin.getNTEHandler().playerData
											.containsKey(uuid)) {
										plugin.getNTEHandler().playerData
												.remove(uuid);
									}
								}
							}.runTask(plugin);
						}
					}.runTaskAsynchronously(plugin);
				}
			}
		} else if (args[0].equalsIgnoreCase("reload")) {
			if (!sender.hasPermission("nametagedit.reload")) {
				sender.sendMessage(noPerm);
				return true;
			}

			if (args.length != 2) {
				sender.sendMessage(prefix + "§6File§f/§6Memory");
				sender.sendMessage(prefix
						+ "§fReloading from file will accept active edits");
				sender.sendMessage(prefix
						+ "§fReloading from memory will overwrite file data from memory");
			} else if (args[1].equalsIgnoreCase("file")) {
				plugin.getNTEHandler().reload(sender, true);
			} else if (args[1].equalsIgnoreCase("memory")) {
				plugin.getNTEHandler().reload(sender, false);
			}
		} else if (args[0].equalsIgnoreCase("prefix")
				|| args[0].equalsIgnoreCase("suffix")) {
			if (!(sender.hasPermission("nametagedit.edit.self") || sender
					.hasPermission("nametagedit.edit.others"))) {
				sender.sendMessage(noPerm);
				return true;
			}

			if (args.length <= 2) {
				sender.sendMessage(prefix
						+ "Usage: /ne prefix/suffix <player> text");
				return true;
			} else if (args.length > 2) {
				String type = args[0].toLowerCase();

				final String targetName = args[1];

				if (!sender.hasPermission("nametagedit.edit.others")
						&& !targetName.equalsIgnoreCase(sender.getName())) {
					sender.sendMessage(prefix
							+ "§cYou can only edit your own tag.");
					return false;
				}

				Player target = Bukkit.getPlayer(args[1]);

				String oper = argsToString(args, 2, args.length);

				setType(sender, targetName, type, NametagAPI.trim(oper));

				if (plugin.databaseEnabled) {
					if (target != null) {
						plugin.getMySQL().updatePlayerSQL("players",
								target.getUniqueId().toString(),
								target.getName(), "prefix", oper);
					}
				}
			}
		} else if (args[0].equalsIgnoreCase("groups")) {
			if (!sender.hasPermission("nametagedit.groups")) {
				sender.sendMessage(noPerm);
				return true;
			}

			if (args[1].equalsIgnoreCase("list")) {

				StringBuilder sb = new StringBuilder();

				for (String s : plugin.getNTEHandler().allGroups) {
					sb.append(new StringBuilder().append("§6" + s)
							.append("§f, ").toString());
				}

				if (sb.length() > 0) {
					sb.setLength(sb.length() - 2);
				}

				sender.sendMessage(prefix + "§fLoaded Groups: §f"
						+ sb.toString());
			} else if (args[1].equalsIgnoreCase("remove")) {
				if (args.length == 3) {
					String group = args[2];

					if (plugin.getNTEHandler().allGroups.contains(group)) {
						plugin.getNTEHandler().allGroups.remove(group);
						plugin.getNTEHandler().groupData.remove(group);
					}

					if (plugin.databaseEnabled) {
						plugin.getMySQL().purgeType("groups", "name", group);
					}

					sender.sendMessage(prefix + "§fSuccessfully removed §6"
							+ group);
				}
			} else if (args[1].equalsIgnoreCase("order")) {
				if (sender instanceof Player) {

					if (plugin.databaseEnabled) {
						sender.sendMessage(prefix
								+ "§fYou can only sort flat file groups.");
						return false;
					}

					createOrganizer((Player) sender);
				}
			} else if (args[1].equalsIgnoreCase("add")) {
				if (args.length == 3) {
					String group = args[2];
					if (!plugin.getNTEHandler().groupData.containsKey(group)) {
						plugin.getNTEHandler().groupData.put(group,
								Arrays.asList("", "", ""));
						sender.sendMessage(prefix + "§fThe group §6" + group
								+ " §fhas been added!");

						if (plugin.databaseEnabled) {
							plugin.getMySQL().groupCheck("groups", group);
						}
					} else {
						sender.sendMessage(prefix
								+ "§cThis group already exists.");
					}
				}
			} else if (args[1].equalsIgnoreCase("set")) {
				if (args.length >= 5) {

					String group = args[3];

					if (!plugin.getNTEHandler().groupData.containsKey(group)) {
						sender.sendMessage(prefix + "§cThe group '" + group
								+ "' doesn't exist.");
						return false;
					}

					if (args[1].equalsIgnoreCase("set")) {
						if (args[2].equalsIgnoreCase("perm")) {
							plugin.getNTEHandler().groupData.get(group).set(0,
									args[4]);
							plugin.getNTEHandler().permissions.remove(plugin
									.getNTEHandler().groupData.get(group)
									.get(2));
							plugin.getNTEHandler().permissions.put(args[4],
									group);
							sender.sendMessage(prefix + "§fSet §6" + group
									+ "§f's permission to: §6" + args[4]);

							if (plugin.databaseEnabled) {
								plugin.getMySQL().updateGroupSQL("groups",
										"permission", group, args[4]);
							}
						} else if (args[2].equalsIgnoreCase("prefix")) {

							String oper = argsToString(args, 4, args.length);

							plugin.getNTEHandler().groupData.get(group).set(1,
									NametagAPI.trim(oper));

							sender.sendMessage(prefix + "§fSet §6" + args[3]
									+ "§f's prefix to: §6"
									+ NametagAPI.trim(oper));

							if (plugin.databaseEnabled) {
								plugin.getMySQL().updateGroupSQL("groups",
										"prefix", group, NametagAPI.trim(oper));
							}
						} else if (args[2].equalsIgnoreCase("suffix")) {

							String oper = argsToString(args, 4, args.length);

							plugin.getNTEHandler().groupData.get(group).set(2,
									NametagAPI.trim(oper));

							sender.sendMessage(prefix + "§fSet §6" + args[3]
									+ "§f's suffix to: §6"
									+ NametagAPI.trim(oper));

							if (plugin.databaseEnabled) {
								plugin.getMySQL().updateGroupSQL("groups",
										"suffix", group, NametagAPI.trim(oper));
							}
						}
					}
				} else {
					sender.sendMessage(prefix
							+ "§fUsage §6/ne groups set [option] <group> <value>");
				}
			} else {
				sender.sendMessage(prefix + "§fUnrecognized sub-command!");
			}
		} else {
			sender.sendMessage(prefix + "§fUnrecognized sub-command!");
		}
		return true;
	}

	private String argsToString(String[] text, int to, int from) {
		return Joiner.on(" ").join(Arrays.copyOfRange(text, to, from))
				.replaceAll("'", "").replaceAll("&", "§");
	}

	private void createOrganizer(Player p) {
		plugin.organizer = Bukkit.createInventory(null, 18,
				"§0NametagEdit Group Organizer");
		plugin.organizer
				.setItem(
						0,
						make(Material.WOOL,
								1,
								5,
								"§bNametagEdit Organizer",
								Arrays.asList(
										"§fStarting from the left and going to the right",
										"§forganize your groups so the most important",
										"§fgroup is at the end.")));
		plugin.organizer.setItem(
				1,
				make(Material.WOOL, 1, 5, "§aDone",
						Arrays.asList("§fONLY click this when you are done!")));

		for (String s : plugin.getNTEHandler().allGroups) {
			plugin.organizer.addItem(make(Material.WOOL, 1, 0, s,
					Arrays.asList("§7Move Me :D")));
		}

		p.openInventory(plugin.organizer);
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
		case "prefix":
			reason = NametagChangeReason.SET_PREFIX;
			tempId = 1;
			break;
		case "suffix":
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
	public static void setNametagSoft(String player, String prefix,
			String suffix, NametagChangeReason reason) {
		NametagChangeEvent e = new NametagChangeEvent(player,
				NametagAPI.getPrefix(player), NametagAPI.getSuffix(player),
				prefix, suffix, NametagChangeType.SOFT, reason);
		Bukkit.getServer().getPluginManager().callEvent(e);
		if (!e.isCancelled()) {
			NametagManager.update(player, prefix, suffix);
		}
	}

	/**
	 * Returns an ItemStack for the group organizer
	 */
	public ItemStack make(Material material, int amount, int shrt,
			String displayName, List<String> lore) {
		ItemStack item = new ItemStack(material, amount, (short) shrt);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(displayName);
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}
}