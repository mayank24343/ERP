package edu.univ.erp.data;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;

public class AddDropDao {

    private final DataSource ds;

    public AddDropDao(DataSource ds) {
        this.ds = ds;
    }

    public void setDeadline(LocalDate date) throws SQLException {
        String sql = """
            INSERT INTO add_drop_deadline (id, deadline)
            VALUES (1, ?)
            ON DUPLICATE KEY UPDATE deadline = VALUES(deadline)
        """;

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(date));  // LocalDate → java.sql.Date
            ps.executeUpdate();
        }
    }

    public LocalDate getDeadline() throws SQLException {
        String sql = "SELECT deadline FROM add_drop_deadline WHERE id = 1";

        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                Date d = rs.getDate("deadline");   // java.sql.Date
                return d != null ? d.toLocalDate() : null;
            }
            return null;
        }
    }
}
