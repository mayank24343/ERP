package edu.univ.erp.data;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;

public class AddDropDao {

    private final DataSource ds;

    public AddDropDao(DataSource ds) {
        this.ds = ds;
    }

    // this function save the deadline date. 
    // it updates a date if it already exists. If a date does not exist, then it creates a new one 
    public void setDeadline(LocalDate date) throws SQLException {
        // SQL query: Try to insert ID 1. If ID 1 exists, update the deadline instead.
        var sql = " INSERT INTO add_drop_deadline (id, deadline) VALUES (1, ?) ON DUPLICATE KEY UPDATE deadline = VALUES(deadline)";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date)); // Convert Java date to Database date
            ps.executeUpdate();
        }
    }
    
    // This function gets the current deadline date from the database
    public LocalDate getDeadline() throws SQLException {
        var sql = "SELECT deadline FROM add_drop_deadline WHERE id = 1";

        try (var conn = ds.getConnection();
             var ps = conn.prepareStatement(sql);
             var rs = ps.executeQuery()) {

            // If we find a row, convert the database date back to a Java LocalDate
            if (rs.next()) {
                Date d = rs.getDate("deadline");
                return (d != null) ? d.toLocalDate() : null;
            }
            return null; // return null if no date is found
        }
    }
}
