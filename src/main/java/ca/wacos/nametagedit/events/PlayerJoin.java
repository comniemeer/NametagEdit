package ca.wacos.nametagedit.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import ca.wacos.nametagedit.NametagEdit;
import ca.wacos.nametagedit.NametagManager;

public class PlayerJoin implements Listener {

	private NametagEdit plugin;

	public PlayerJoin(NametagEdit plugin) {
		this.plugin = plugin;
	}

	// Clears a player's tag, sends teams, and applies tags to the player
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(final PlayerJoinEvent e) {
		final Player p = e.getPlayer();

		NametagManager.sendTeamsToPlayer(p);

		NametagManager.clear(p.getName());

		new BukkitRunnable() {
			@Override
			public void run() {
				plugin.getNTEHandler().applyTagToPlayer(p);
			}
		}.runTaskLater(plugin, 1);
	}
}