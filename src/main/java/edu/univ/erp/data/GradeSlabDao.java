package edu.univ.erp.data;

import edu.univ.erp.domain.GradeSlab;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

public class GradeSlabDao {
    private final DataSource ds;

    //constructor
    public GradeSlabDao(DataSource ds) {
        this.ds = ds;
    }

    //get slabs
    public List<GradeSlab> getSlabs(int sectionId) throws SQLException {
        String sql = "SELECT * FROM grade_slabs WHERE section_id=? ORDER BY min_percent DESC";
        List<GradeSlab> list = new ArrayList<>();

        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ResultSet rs = ps.executeQuery();

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

        return list;
    }

    //add slabs
    public void insertSlab(int sectionId, String letter, double min, double max) throws SQLException {
        String sql = "INSERT INTO grade_slabs (section_id, letter, min_percent, max_percent) VALUES (?, ?, ?, ?)";

        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, sectionId);
            ps.setString(2, letter);
            ps.setDouble(3, min);
            ps.setDouble(4, max);

            ps.executeUpdate();
        }
    }

    //updet slabs
    public void updateSlab(GradeSlab slab) throws SQLException {
        String sql = "UPDATE grade_slabs SET letter=?, min_percent=?, max_percent=? WHERE slab_id=?";

        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, slab.getLetter());
            ps.setDouble(2, slab.getMin());
            ps.setDouble(3, slab.getMax());
            ps.setInt(4, slab.getId());

            ps.executeUpdate();
        }
    }

    //delete slabs
    public void deleteSlab(int slabId) throws SQLException {
        try (Connection conn = ds.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM grade_slabs WHERE slab_id=?")) {
            ps.setInt(1, slabId);
            ps.executeUpdate();
        }
    }
}
