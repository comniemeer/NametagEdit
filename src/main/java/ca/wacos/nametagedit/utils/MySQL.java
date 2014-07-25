package ca.wacos.nametagedit.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.scheduler.BukkitRunnable;

import ca.wacos.nametagedit.NametagEdit;

/**
 * This class is responsible for handling all MySQL connection
 * 
 * @author sgtcaze
 */
public class MySQL {

	private static String HOSTNAME, PORT, DATABASE, USERNAME, PASSWORD;

	private NametagEdit plugin;

	public MySQL(NametagEdit plugin) {
		this.plugin = plugin;
	}

	private Connection connection;

	public Connection getConnection() {
		return connection;
	}

	// Opens MySQL Connection
	public synchronized void open() {
		HOSTNAME = plugin.config.getString("MySQL.Hostname");
		PORT = plugin.config.getString("MySQL.Port");
		DATABASE = plugin.config.getString("MySQL.Database");
		USERNAME = plugin.config.getString("MySQL.Username");
		PASSWORD = plugin.config.getString("MySQL.Password");

		try {
			connection = DriverManager.getConnection("jdbc:mysql://" + HOSTNAME
					+ ":" + PORT + "/" + DATABASE, USERNAME, PASSWORD);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Closes MySQL Connection
	public synchronized void close() {
		try {
			if (connection != null || connection.isValid(0)) {
				connection.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Checks if a connection is valid, opens if necessary
	public void checkValid() {
		try {
			if (!connection.isValid(0)) {
				open();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Function creates the tables if they don't exist
	public void createTables() {
		checkValid();

		String playerTable = "CREATE TABLE IF NOT EXISTS `players` (`uuid` varchar(64) NOT NULL, `name` varchar(16) NOT NULL, `prefix` varchar(16) NOT NULL, `suffix` varchar(16) NOT NULL);";
		String groupTable = "CREATE TABLE IF NOT EXISTS `groups` (`name` varchar(64) NOT NULL, `permission` varchar(16) NOT NULL, `prefix` varchar(16) NOT NULL, `suffix` varchar(16) NOT NULL);";

		try {
			PreparedStatement p = connection.prepareStatement(playerTable);
			p.execute();
			p.close();

			PreparedStatement g = connection.prepareStatement(groupTable);
			g.execute();
			g.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Function removes a row from a table
	public void purgeType(String table, String type, String id) {
		checkValid();

		String query = "DELETE FROM `" + table + "` WHERE `" + type + "`='"
				+ id + "';";

		try {
			PreparedStatement p = connection.prepareStatement(query);
			p.execute();
			p.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Function checks if a player is in the database, if not, they're added
	public synchronized void runCheck(String table, String uuid, String name) {
		checkValid();

		try {
			String query = "SELECT * FROM `" + table + "` WHERE `uuid`='"
					+ uuid + "';";

			ResultSet res = connection.prepareStatement(query).executeQuery();

			if (!res.next()) {
				String updateQuery = "INSERT INTO `" + table + "` VALUES('"
						+ uuid + "', '" + name + "', '', '');";

				PreparedStatement p = connection.prepareStatement(updateQuery);
				p.execute();
				p.close();
			}

			res.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Function updates a player value (prefix, suffix)
	public void updatePlayerSQL(String table, String uuid, String name,
			String field, String oper) {

		checkValid();

		runCheck(table, uuid, name);

		String query = "UPDATE `" + table + "` SET `" + field + "`='" + oper
				+ "' WHERE uuid='" + uuid + "';";
		try {
			PreparedStatement p = connection.prepareStatement(query);
			p.execute();
			p.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Function checks if a group is in the database, if not, it is added
	public void groupCheck(String table, String group) {
		checkValid();

		try {
			String query = "SELECT * FROM `" + table + "` WHERE `name`='"
					+ group + "';";

			ResultSet res = connection.prepareStatement(query).executeQuery();

			if (!res.next()) {
				String updateQuery = "INSERT INTO `" + table + "` VALUES('"
						+ group + "', '', '', '');";

				PreparedStatement p = connection.prepareStatement(updateQuery);
				p.execute();
				p.close();
			}

			res.close();
		} catch (SQLException e) {

		}
	}

	// Function updates a group value (permission, prefix, suffix)
	public void updateGroupSQL(String table, String field, String group,
			String oper) {
		checkValid();

		groupCheck(table, group);

		try {
			String overwriteQuery = "UPDATE `" + table + "` SET `" + field
					+ "`='" + oper + "' WHERE `name`='" + group + "';";

			PreparedStatement p = connection.prepareStatement(overwriteQuery);
			p.execute();
			p.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Function returns group info for NTEHandler (similar to file loader)
	public HashMap<String, List<String>> returnGroups() {
		checkValid();

		HashMap<String, List<String>> temp = new HashMap<>();
		final HashMap<String, String> tPerms = new HashMap<>();

		String query = "SELECT * FROM `groups`";

		try {
			ResultSet res = connection.prepareStatement(query).executeQuery();

			while (res.next()) {
				temp.put(
						res.getString("name"),
						Arrays.asList(res.getString("permission"),
								res.getString("prefix"),
								res.getString("suffix")));
				tPerms.put(res.getString("permission"), res.getString("name"));
			}

			res.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				plugin.getNTEHandler().permissions = tPerms;
			}
		}.runTask(plugin);

		plugin.getLogger().info("[MySQL] Found " + temp.size() + " groups.");

		return temp;
	}

	// Function returns player info for NTEHandler (similar to file loader)
	public HashMap<String, List<String>> returnPlayers() {
		checkValid();

		HashMap<String, List<String>> temp = new HashMap<>();

		String query = "SELECT * FROM `players`";

		try {
			ResultSet res = connection.prepareStatement(query).executeQuery();

			while (res.next()) {
				temp.put(res.getString("uuid"), Arrays.asList(res
						.getString("name"),
						res.getString("prefix").replaceAll("&", "§"), res
								.getString("suffix").replaceAll("&", "§")));
			}

			res.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		plugin.getLogger().info("[MySQL] Found " + temp.size() + " players.");

		return temp;
	}
}