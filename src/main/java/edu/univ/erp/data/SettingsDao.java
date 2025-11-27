package edu.univ.erp.data;

import javax.sql.DataSource;
import java.sql.*;

public class SettingsDao {

    private final DataSource ds;

    public SettingsDao(DataSource ds) {
        this.ds = ds;
    }

    // checks the database to see if maintenance mode is currently enabled
    //looks for true
    public boolean readMaintenanceFlag() throws SQLException {
        var sql = "SELECT setting_value FROM settings WHERE setting_key = 'maintenance_mode'";

        try (var c = ds.getConnection(); var ps = c.prepareStatement(sql); var rs = ps.executeQuery()) {
            // Return true only if the row exists and the value is "true"
            return rs.next() && "true".equalsIgnoreCase(rs.getString("setting_value"));
        }
    }

    // updates the maintenance mode setting in the database (true / false)
    public void writeMaintenanceFlag(boolean enabled) throws SQLException {
        var sql = "UPDATE settings SET setting_value = ? WHERE setting_key = 'maintenance_mode'";

        try (var c = ds.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, enabled ? "true" : "false");
            ps.executeUpdate();
        }
    }
}
