package ca.wacos.nametagedit.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import ca.wacos.nametagedit.core.NametagManager;

public class PlayerJoin implements Listener {

    // Clears a player's tag, sends teams, and applies tags to the player
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();

        NametagManager.sendTeamsToPlayer(p);
        NametagManager.clear(p.getName());
    }
}