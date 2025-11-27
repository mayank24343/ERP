package edu.univ.erp.data;

import javax.sql.DataSource;
import java.sql.*;

public class MaintenanceDao {
    private final DataSource ds;

    public MaintenanceDao(DataSource ds) {
        this.ds = ds;
    }

    // checks if the system is currently in maintenance mode 
    // returns true if it is on
    public boolean isMaintenanceOn() throws SQLException {
        var sql = "SELECT setting_value FROM settings WHERE setting_key = 'maintenance_mode'";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql); var rs = ps.executeQuery()) {
            // Returns true only if a setting exists and it is set to "on"
            return rs.next() && "on".equalsIgnoreCase(rs.getString(1));
        }
    }

    // turns the maintenance mode on or off by saving the setting to the database
    public void setMaintenance(boolean on) throws SQLException {
        var sql = "INSERT INTO settings (setting_key, setting_value) VALUES ('maintenance_mode', ?) ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, on ? "on" : "off");
            ps.executeUpdate();
        }
    }
}
