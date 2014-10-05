package ca.wacos.nametagedit;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import ca.wacos.nametagedit.core.NTEHandler;
import ca.wacos.nametagedit.core.NametagManager;
import ca.wacos.nametagedit.events.AsyncPlayerChat;
import ca.wacos.nametagedit.events.InventoryClick;
import ca.wacos.nametagedit.events.PlayerJoin;
import ca.wacos.nametagedit.utils.FileManager;

/**
 * This is the main class for the NametagEdit plugin.
 * 
 * @author sgtcaze
 * 
 */
public class NametagEdit extends JavaPlugin {
	
    private static NametagEdit instance;
    
    private FileManager fileUtils;
    private NTEHandler nteHandler;
    private NametagManager nametagManager;
    
    public Inventory organizer;
    
    public boolean tabListDisabled;
    
    @Override
    public void onEnable() {
        instance = this;

        FileConfiguration config = getConfig();

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerJoin(), this);
        pm.registerEvents(new InventoryClick(), this);

        if (config.getBoolean("Chat.Enabled")) {
            pm.registerEvents(new AsyncPlayerChat(), this);
        }

        fileUtils = new FileManager();
        nametagManager = new NametagManager();
        nteHandler = new NTEHandler();

        saveDefaultConfig();

        NametagManager.load();

        fileUtils.loadFiles();

        tabListDisabled = config.getBoolean("TabListDisabled");

        nteHandler.loadFromFile(fileUtils.getPlayersFile(),
                fileUtils.getGroupsFile());

        nteHandler.applyTags();
    }

    @Override
    public void onDisable() {
        NametagManager.reset();

        getNTEHandler().saveFileData(fileUtils.getPlayersFile(),
                fileUtils.getGroupsFile());
    }

    public static NametagEdit getInstance() {
        return instance;
    }

    public NametagManager getNametagManager() {
        return nametagManager;
    }

    public NTEHandler getNTEHandler() {
        return nteHandler;
    }

    public FileManager getFileUtils() {
        return fileUtils;
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

    /**
     * Opens the group organizer
     * 
     * @param p
     *            the player to open the inventory
     */
    public void createOrganizer(Player p) {
        organizer = Bukkit.createInventory(null, 18,
                "§0NametagEdit Group Organizer");
        organizer
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
        organizer.setItem(
                1,
                make(Material.WOOL, 1, 5, "§aDone",
                        Arrays.asList("§fONLY click this when you are done!")));

        for (String s : getNTEHandler().allGroups) {
            organizer.addItem(make(Material.WOOL, 1, 0, s,
                    Arrays.asList("§7Move Me :D")));
        }

        p.openInventory(organizer);
    }
}