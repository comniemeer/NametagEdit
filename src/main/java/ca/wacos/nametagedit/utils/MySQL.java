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

            String query = "DELETE FROM `" + table + "` WHERE `" + type
                    + "`=?;";

            PreparedStatement p = connection.prepareStatement(query);
            p.setString(1, id);
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

            String query = "INSERT INTO `players` VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE `prefix`=?, `suffix`=?;";

            PreparedStatement p = connection.prepareStatement(query);
            p.setString(1, uuid);
            p.setString(2, name);
            p.setString(3, prefix);
            p.setString(4, suffix);
            p.setString(5, prefix);
            p.setString(6, suffix);
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

            String query = "UPDATE `" + table + "` SET `" + field
                    + "`=? WHERE `name`=?;";

            PreparedStatement p = connection.prepareStatement(query);
            p.setString(1, oper);
            p.setString(2, group);
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