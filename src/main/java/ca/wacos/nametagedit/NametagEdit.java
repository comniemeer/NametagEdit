package ca.wacos.nametagedit;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
import org.mcstats.Metrics;

import ca.wacos.nametagedit.core.NTEHandler;
import ca.wacos.nametagedit.core.NametagManager;
import ca.wacos.nametagedit.events.AsyncPlayerChat;
import ca.wacos.nametagedit.events.InventoryClick;
import ca.wacos.nametagedit.events.PlayerJoin;
import ca.wacos.nametagedit.utils.FileManager;
import ca.wacos.nametagedit.utils.MySQL;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

/**
 * This is the main class for the NametagEdit plugin.
 * 
 * @author sgtcaze
 * 
 */
public class NametagEdit extends JavaPlugin {

    private static NametagEdit instance;

    private MySQL mySQL;
    private FileManager fileUtils;
    private NTEHandler nteHandler;
    private NametagManager nametagManager;

    public Inventory organizer;

    private BoneCP connectionPool;

    public boolean tabListDisabled, databaseEnabled;

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

        getCommand("ne").setExecutor(new NametagCommand());

        fileUtils = new FileManager();
        nametagManager = new NametagManager();
        nteHandler = new NTEHandler();
        mySQL = new MySQL();

        saveDefaultConfig();

        NametagManager.load();

        fileUtils.loadFiles();

        tabListDisabled = config.getBoolean("TabListDisabled");
        databaseEnabled = config.getBoolean("MySQL.Enabled");

        if (config.getBoolean("MetricsEnabled")) {
            try {
                Metrics metrics = new Metrics(this);
                metrics.start();
            } catch (IOException e) {
                // Failed to submit the stats :-(
            }
        }

        if (databaseEnabled) {
            setupBoneCP();
            new SQLData().runTaskAsynchronously(this);
        } else {
            nteHandler.loadFromFile(fileUtils.getPlayersFile(),
                    fileUtils.getGroupsFile());
        }

        nteHandler.applyTags();
    }

    @Override
    public void onDisable() {
        NametagManager.reset();

        getNTEHandler().saveFileData(fileUtils.getPlayersFile(),
                fileUtils.getGroupsFile());

        if (databaseEnabled) {
            if (connectionPool != null) {
                connectionPool.shutdown();
            }
        }
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

    public MySQL getMySQL() {
        return mySQL;
    }

    public BoneCP getConnectionPool() {
        return connectionPool;
    }

    private void setupBoneCP() {
        FileConfiguration bconfig = getConfig();
        String address = "jdbc:mysql://" + bconfig.getString("MySQL.Hostname")
                + ":" + bconfig.getString("MySQL.Port") + "/"
                + bconfig.getString("MySQL.Database");
        String username = bconfig.getString("MySQL.Username");
        String password = bconfig.getString("MySQL.Password");

        try {
            BoneCPConfig config = new BoneCPConfig();
            config.setJdbcUrl(address);
            config.setUsername(username);
            config.setPassword(password);
            config.setMinConnectionsPerPartition(5);
            config.setMaxConnectionsPerPartition(10);
            config.setPartitionCount(1);
            connectionPool = new BoneCP(config);
        } catch (SQLException e) {
            getLogger().severe(
                    "The BoneCP connection pool could not be established!");
            e.printStackTrace();
            return;
        }

        Connection connection = null;

        try {
            connection = connectionPool.getConnection();
        } catch (SQLException e) {
            getLogger()
                    .severe("The MySQL connection could not be established!");
            e.printStackTrace();

        }

        String playerTable = "CREATE TABLE IF NOT EXISTS `players` (`uuid` varchar(64) NOT NULL, `name` varchar(16) NOT NULL, `prefix` varchar(16) NOT NULL, `suffix` varchar(16) NOT NULL, PRIMARY KEY (`uuid`));";
        String groupTable = "CREATE TABLE IF NOT EXISTS `groups` (`name` varchar(64) NOT NULL, `permission` varchar(16) NOT NULL, `prefix` varchar(16) NOT NULL, `suffix` varchar(16) NOT NULL, PRIMARY KEY (`name`));";

        try {
            PreparedStatement p = connection.prepareStatement(playerTable);
            p.execute();
            p.close();

            PreparedStatement g = connection.prepareStatement(groupTable);
            g.execute();
            g.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
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