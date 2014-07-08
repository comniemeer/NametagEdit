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

import ca.wacos.nametagedit.utils.FileUtils;
import ca.wacos.nametagedit.utils.Metrics;

/**
 * This is the main class for the NametagEdit server plugin.
 * 
 * @author Levi Webb
 * 
 */
public class NametagEdit extends JavaPlugin {

	public FileConfiguration groups, players;
	public File groupsFile, playersFile;

	static NametagEdit plugin;

	public boolean tabListDisabled = false;

	private NametagManager nametagManager;
	private NTEHandler nteHandler;
	private FileUtils fileUtils;

	@Override
	public void onEnable() {

		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new NametagEventHandler(this), this);

		getCommand("ne").setExecutor(new NametagCommand(this));

		plugin = this;

		fileUtils = new FileUtils(this);

		nametagManager = new NametagManager();
		nteHandler = new NTEHandler(this);

		NametagManager.load();

		saveDefaultConfig();

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