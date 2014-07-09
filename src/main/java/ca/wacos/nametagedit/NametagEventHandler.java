package ca.wacos.nametagedit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * This class is responsible for handling various events in the server.
 * 
 * @author Levi Webb Heavily edited by @sgtcaze
 * 
 */
class NametagEventHandler implements Listener {

	private NametagEdit plugin;

	public NametagEventHandler(NametagEdit plugin) {
		this.plugin = plugin;
	}

	/**
	 * Called when a player joins the server. This event is set to
	 * <i>HIGHEST</i> priority to address a conflict created with plugins that
	 * read player information in this event.<br>
	 * <br>
	 * 
	 * This event updates nametag information, and the tab list (if enabled).
	 * 
	 * @param e
	 *            the {@link org.bukkit.event.player.PlayerJoinEvent} associated
	 *            with this listener.
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	void onPlayerJoin(final PlayerJoinEvent e) {

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