package edu.univ.erp.data;

import edu.univ.erp.domain.GradeSlab;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class GradeSlabDao {
    private final DataSource ds;

    public GradeSlabDao(DataSource ds) {
        this.ds = ds;
    }

    // gets all grade ranges or slabs for a specific section
    // the order is from highest to lowest
    public List<GradeSlab> getSlabs(int sectionId) throws SQLException {
        var sql = "SELECT * FROM grade_slabs WHERE section_id=? ORDER BY min_percent DESC";
        var list = new ArrayList<GradeSlab>();

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new GradeSlab(
                        rs.getInt("slab_id"),
                        rs.getInt("section_id"),
                        rs.getString("letter"),
                        rs.getDouble("min_percent"),
                        rs.getDouble("max_percent")
                    ));
                }
            }
        }
        return list;
    }

    // adds a new grade range definition (like 'A' is 90-100)
    public void insertSlab(int sectionId, String letter, double min, double max) throws SQLException {
        var sql = "INSERT INTO grade_slabs (section_id, letter, min_percent, max_percent) VALUES (?, ?, ?, ?)";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, sectionId);
            ps.setString(2, letter);
            ps.setDouble(3, min);
            ps.setDouble(4, max);
            ps.executeUpdate();
        }
    }

    // updates an existing grade range definition
    public void updateSlab(GradeSlab slab) throws SQLException {
        var sql = "UPDATE grade_slabs SET letter=?, min_percent=?, max_percent=? WHERE slab_id=?";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setString(1, slab.getLetter());
            ps.setDouble(2, slab.getMin());
            ps.setDouble(3, slab.getMax());
            ps.setInt(4, slab.getId());
            ps.executeUpdate();
        }
    }

    // deletes a grade range
    public void deleteSlab(int slabId) throws SQLException {
        var sql = "DELETE FROM grade_slabs WHERE slab_id=?";

        try (var conn = ds.getConnection(); var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slabId);
            ps.executeUpdate();
        }
    }
}
