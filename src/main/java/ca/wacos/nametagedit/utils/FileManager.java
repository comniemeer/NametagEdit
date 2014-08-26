package ca.wacos.nametagedit.utils;

import java.io.File;
import java.io.IOException;

import net.minecraft.util.org.apache.commons.io.FileUtils;

import org.bukkit.configuration.file.YamlConfiguration;

import ca.wacos.nametagedit.NametagEdit;

/**
 * This class is responsible for loading/writing/saving files
 * 
 * @author sgtcaze
 * 
 */
public class FileManager {

    private NametagEdit plugin = NametagEdit.getInstance();

    private File groupsFile, playersFile;
    private YamlConfiguration groups, players;

    public void loadFiles() {
        File groupsTemp = new File(plugin.getDataFolder(), "groups.yml");
        if (!groupsTemp.exists()) {
            generateFile("groups.yml");
        }

        groupsFile = groupsTemp;
        groups = YamlConfiguration.loadConfiguration(groupsFile);

        File playersTemp = new File(plugin.getDataFolder(), "players.yml");
        if (!playersTemp.exists()) {
            generateFile("players.yml");
        }

        playersFile = playersTemp;
        players = YamlConfiguration.loadConfiguration(playersFile);
    }

    private void generateFile(String name) {
        try {
            FileUtils.copyInputStreamToFile(plugin.getResource(name), new File(
                    plugin.getDataFolder() + "/" + name));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public YamlConfiguration getGroupsFile() {
        return this.groups;
    }

    public YamlConfiguration getPlayersFile() {
        return this.players;
    }

    public void saveAllFiles() {
        try {
            players.save(playersFile);
            groups.save(groupsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}