package ca.wacos.nametagedit.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import ca.wacos.nametagedit.NametagEdit;

/**
 * This class is responsible for handling all MySQL queries
 * 
 * @author sgtcaze
 */
public class MySQL {

    private NametagEdit plugin = NametagEdit.getInstance();

    // Function removes a row from a table
    public void purgeType(String table, String type, String id) {
        Connection connection = null;

        try {
            connection = plugin.getConnectionPool().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        String query = "DELETE FROM `" + table + "` WHERE `" + type + "`='"
                + id + "';";

        try {
            PreparedStatement p = connection.prepareStatement(query);
            p.execute();
            p.close();
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

    // Function updates a player value (prefix, suffix)
    public void updatePlayerSQL(String uuid, String name, String prefix,
            String suffix) {
        Connection connection = null;

        try {
            connection = plugin.getConnectionPool().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        String query = "INSERT INTO `players` VALUES ('" + uuid + "', '" + name
                + "', '" + prefix + "', '" + suffix
                + "') ON DUPLICATE KEY UPDATE `prefix`='" + prefix
                + "', `suffix`='" + suffix + "';";
        try {
            PreparedStatement p = connection.prepareStatement(query);
            p.execute();
            p.close();
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

    // Function updates a group value (permission, prefix, suffix)
    public void updateGroupSQL(String table, String field, String group,
            String oper) {
        Connection connection = null;

        try {
            connection = plugin.getConnectionPool().getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        try {
            String overwriteQuery = "UPDATE `" + table + "` SET `" + field
                    + "`='" + oper + "' WHERE `name`='" + group + "';";

            PreparedStatement p = connection.prepareStatement(overwriteQuery);
            p.execute();
            p.close();
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
}