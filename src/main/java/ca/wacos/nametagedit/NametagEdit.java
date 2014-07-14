package ca.wacos.nametagedit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import ca.wacos.nametagedit.events.AsyncPlayerChat;
import ca.wacos.nametagedit.events.PlayerJoin;
import ca.wacos.nametagedit.utils.FileUtils;

/**
 * This is the main class for the NametagEdit server plugin.
 * 
 * @author Levi Webb Heavily edited by @sgtcaze
 * 
 */
public class NametagEdit extends JavaPlugin {

	static NametagEdit plugin;

	private FileUtils fileUtils;
	private NTEHandler nteHandler;
	private NametagManager nametagManager;

	public FileConfiguration groups, players;
	public File groupsFile, playersFile;

	public boolean tabListDisabled = false;

	@Override
	public void onEnable() {

		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new PlayerJoin(this), this);
		
		if(getConfig().getBoolean("Chat.Enabled")){
			pm.registerEvents(new AsyncPlayerChat(this), this);
		}

		getCommand("ne").setExecutor(new NametagCommand(this));

		plugin = this;

		fileUtils = new FileUtils(this);
		nametagManager = new NametagManager();
		nteHandler = new NTEHandler(this);

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

		nteHandler.loadGroups();
		nteHandler.loadPlayers();
		nteHandler.applyTags();

		startup();

		tabListDisabled = getConfig().getBoolean("TabListDisabled");

		if (getConfig().getBoolean("MetricsEnabled")) {
			try {
				Metrics metrics = new Metrics(this);
				metrics.start();
			} catch (IOException e) {
				// Failed to submit the stats :-(
			}
		}
	}

	@Override
	public void onDisable() {
		NametagManager.reset();
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

	private void startup() {
		File f = new File(plugin.getDataFolder() + File.separator + "INFO.txt");

		if (!f.exists()) {
			try {
				InputStream in = plugin.getResource("INFO.txt");
				OutputStream out = new FileOutputStream(f);
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
				out.close();
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}