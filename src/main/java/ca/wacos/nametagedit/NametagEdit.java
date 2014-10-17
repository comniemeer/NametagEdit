package ca.wacos.nametagedit;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import ca.wacos.nametagedit.core.NametagHandler;
import ca.wacos.nametagedit.core.NametagManager;
import ca.wacos.nametagedit.events.AsyncPlayerChat;
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
    private NametagHandler nteHandler;
    private NametagManager nametagManager;

    public Inventory organizer;

    private BoneCP connectionPool;

    public boolean tabListDisabled, databaseEnabled;

    @Override
    public void onEnable() {
        instance = this;

        mySQL = new MySQL();
        fileUtils = new FileManager();
        nteHandler = new NametagHandler();
        nametagManager = new NametagManager();

        FileConfiguration config = getConfig();

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerJoin(), this);

        if (config.getBoolean("Chat.Enabled")) {
            pm.registerEvents(new AsyncPlayerChat(), this);
        }

        getCommand("ne").setExecutor(new NametagCommand());

        saveDefaultConfig();
        fileUtils.loadFiles();

        tabListDisabled = config.getBoolean("TabListDisabled");
        databaseEnabled = config.getBoolean("MySQL.Enabled");

        NametagManager.load();

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
            new SQLData().runTask(this);
        } else {
            nteHandler.loadFromFile(fileUtils.getPlayersFile(),
                    fileUtils.getGroupsFile());
        }

        nteHandler.applyTags();
    }

    @Override
    public void onDisable() {
        NametagManager.reset();

        nteHandler.saveFileData(fileUtils.getPlayersFile(),
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

    public NametagHandler getNTEHandler() {
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

        String playerTable = "CREATE TABLE IF NOT EXISTS `players` (`uuid` varchar(64) NOT NULL, `name` varchar(16) NOT NULL, `prefix` varchar(16) NOT NULL, `suffix` varchar(16) NOT NULL, PRIMARY KEY (`uuid`));";
        String groupTable = "CREATE TABLE IF NOT EXISTS `groups` (`name` varchar(64) NOT NULL, `permission` varchar(16) NOT NULL, `prefix` varchar(16) NOT NULL, `suffix` varchar(16) NOT NULL, PRIMARY KEY (`name`));";

        try {
            connection = connectionPool.getConnection();

            PreparedStatement p = connection.prepareStatement(playerTable);
            p.execute();
            p.close();

            PreparedStatement g = connection.prepareStatement(groupTable);
            g.execute();
            g.close();
        } catch (SQLException e) {
            getLogger()
                    .severe("The MySQL connection could not be established!");
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
}