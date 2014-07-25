package ca.wacos.nametagedit.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import ca.wacos.nametagedit.NametagAPI;
import ca.wacos.nametagedit.NametagEdit;

public class AsyncPlayerChat implements Listener {

	private NametagEdit plugin;

	public AsyncPlayerChat(NametagEdit plugin) {
		this.plugin = plugin;
	}

	// Formats chat if 'true' in the config
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();

		String prefix = NametagAPI.getPrefix(p.getName());
		String suffix = NametagAPI.getSuffix(p.getName());

		String format = plugin.config.getString("Chat.Format")
				.replaceAll("%prefix%", prefix).replaceAll("%suffix%", suffix)
				.replaceAll("%name%", "%s").replaceAll("%message%", "%s");

		e.setFormat(format);
	}
}