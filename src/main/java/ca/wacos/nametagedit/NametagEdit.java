package ca.wacos.nametagedit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import ca.wacos.nametagedit.core.NametagManager;
import ca.wacos.nametagedit.events.PlayerJoin;

/**
 * This is the main class for the NametagEdit plugin.
 * 
 * @author sgtcaze
 * 
 */
public class NametagEdit extends JavaPlugin {

    private static NametagEdit instance;

    private NametagManager nametagManager;

    @Override
    public void onEnable() {
        instance = this;
        
        nametagManager = new NametagManager();

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerJoin(), this);

        NametagManager.load();
    }

    @Override
    public void onDisable() {
        NametagManager.reset();
    }

    public static NametagEdit getInstance() {
        return instance;
    }

    public NametagManager getNametagManager() {
        return nametagManager;
    }
}