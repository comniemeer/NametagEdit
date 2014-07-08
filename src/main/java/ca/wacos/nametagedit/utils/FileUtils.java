package ca.wacos.nametagedit.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ca.wacos.nametagedit.NametagEdit;

public class FileUtils {

	private NametagEdit plugin;

	public FileUtils(NametagEdit plugin) {
		this.plugin = plugin;
	}

	public void run() throws Exception {

		if (!plugin.groupsFile.exists()) {
			plugin.groupsFile.getParentFile().mkdirs();
			copy(plugin.getResource("groups.yml"), plugin.groupsFile);
		}

		if (!plugin.playersFile.exists()) {
			plugin.playersFile.getParentFile().mkdirs();
			copy(plugin.getResource("players.yml"), plugin.playersFile);
		}
	}

	public void saveYamls() {
		try {
			plugin.groups.save(plugin.groupsFile);
			plugin.players.save(plugin.playersFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadYamls() {
		try {
			plugin.groups.load(plugin.groupsFile);
			plugin.players.load(plugin.playersFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
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