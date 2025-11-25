package edu.univ.erp.data;

import javax.sql.DataSource;
import java.sql.*;

public class SettingsDao {

    private final DataSource ds;

    public SettingsDao(DataSource ds) {
        this.ds = ds;
    }

    public boolean readMaintenanceFlag() throws SQLException {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = 'maintenance_mode'";

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false;

            return rs.getString("setting_value").equalsIgnoreCase("true");
        }
    }

    public void writeMaintenanceFlag(boolean enabled) throws SQLException {
        String sql = """
            UPDATE settings SET setting_value = ?
            WHERE setting_key = 'maintenance_mode'
        """;

        try (Connection c = ds.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, enabled ? "true" : "false");
            ps.executeUpdate();
        }
    }
}
