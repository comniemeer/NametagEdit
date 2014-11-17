package ca.wacos.nametagedit.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import ca.wacos.nametagedit.NametagAPI;
import ca.wacos.nametagedit.NametagEdit;

public class AsyncPlayerChat implements Listener {

    private NametagEdit plugin = NametagEdit.getInstance();
    
    private String format;
    
    public AsyncPlayerChat() {
        this.format = plugin.getConfig().getString("Chat.Format");
    }
    

    // Formats chat if 'true' in the config
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();

        String prefix = NametagAPI.getPrefix(p.getName());
        String suffix = NametagAPI.getSuffix(p.getName());

        String temp =  format.replace("%prefix%", prefix).replace("%suffix%", suffix)
                .replace("%name%", "%s").replace("%message%", "%s");

        e.setFormat(temp);
    }
}