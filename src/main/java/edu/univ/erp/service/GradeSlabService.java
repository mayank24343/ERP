package edu.univ.erp.service;

import edu.univ.erp.data.GradeSlabDao;
import edu.univ.erp.domain.GradeSlab;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class GradeSlabService {
    private final GradeSlabDao dao;

    public GradeSlabService(DataSource ds) {
        this.dao = new GradeSlabDao(ds);
    }

    public List<GradeSlab> getSlabs(int sectionId) throws SQLException {
        return dao.getSlabs(sectionId);
    }

    public void addSlab(int sectionId, String letter, double min, double max) throws SQLException {
        dao.insertSlab(sectionId, letter, min, max);
    }

    public void updateSlab(GradeSlab slab) throws SQLException {
        dao.updateSlab(slab);
    }

    public void deleteSlab(int slabId) throws SQLException {
        dao.deleteSlab(slabId);
    }
}
