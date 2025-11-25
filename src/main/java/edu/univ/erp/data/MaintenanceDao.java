package edu.univ.erp.data;

import edu.univ.erp.util.DataSourceProvider;

import javax.sql.DataSource;
import java.sql.*;

public class MaintenanceDao {

    private final DataSource ds;


    public MaintenanceDao(DataSource ds) {
        this.ds = ds;
    }

   //check is maintenance is on
    public boolean isMaintenanceOn() throws SQLException {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = 'maintenance_mode'";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                return false; // default OFF
            }

            return "on".equalsIgnoreCase(rs.getString(1));
        }
    }

    //change maintenance flag
    public void setMaintenance(boolean on) throws SQLException {
        String sql =
                "INSERT INTO settings (setting_key, setting_value) " +
                        "VALUES ('maintenance_mode', ?) " +
                        "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, on ? "on" : "off");
            ps.executeUpdate();
        }
    }
}
