package ca.wacos.nametagedit;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import ca.wacos.nametagedit.events.AsyncPlayerChat;
import ca.wacos.nametagedit.events.InventoryClick;
import ca.wacos.nametagedit.events.PlayerJoin;
import ca.wacos.nametagedit.utils.FileUtils;
import ca.wacos.nametagedit.utils.MySQL;

/**
 * This is the main class for the NametagEdit plugin.
 * 
 * @author sgtcaze
 * 
 */
public class NametagEdit extends JavaPlugin {

	static NametagEdit plugin;

	private FileUtils fileUtils;
	private NTEHandler nteHandler;
	private MySQL mySQL;
	private NametagManager nametagManager;

	public Inventory organizer;

	public FileConfiguration groups, players, config;
	public File groupsFile, playersFile;

	public boolean tabListDisabled, databaseEnabled;

	@Override
	public void onEnable() {

		config = getConfig();

		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new PlayerJoin(this), this);
		pm.registerEvents(new InventoryClick(this), this);

		if (config.getBoolean("Chat.Enabled")) {
			pm.registerEvents(new AsyncPlayerChat(this), this);
		}

		getCommand("ne").setExecutor(new NametagCommand(this));

		plugin = this;

		fileUtils = new FileUtils(this);
		nametagManager = new NametagManager();
		nteHandler = new NTEHandler(this);
		mySQL = new MySQL(this);

		saveDefaultConfig();

		NametagManager.load();

		groupsFile = new File(getDataFolder(), "groups.yml");
		playersFile = new File(getDataFolder(), "players.yml");

		try {
			fileUtils.run();
		} catch (Exception e) {
			e.printStackTrace();
		}

		groups = new YamlConfiguration();
		players = new YamlConfiguration();

		fileUtils.loadYamls();

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
			mySQL.open();
			mySQL.createTables();
			nteHandler.loadFromSQL();
		} else {
			nteHandler.loadFromFile();
		}

		nteHandler.applyTags();
	}

	@Override
	public void onDisable() {
		NametagManager.reset();

		fileUtils.loadYamls();
		getNTEHandler().saveFileData();

		if (databaseEnabled) {
			mySQL.close();
		}
	}

	public NametagManager getNametagManager() {
		return nametagManager;
	}

	public NTEHandler getNTEHandler() {
		return nteHandler;
	}

	public FileUtils getFileUtils() {
		return fileUtils;
	}

	public MySQL getMySQL() {
		return mySQL;
	}
}