package edu.univ.erp.service;

import edu.univ.erp.data.GradeSlabDao;
import edu.univ.erp.domain.GradeSlab;
import edu.univ.erp.ui.UiContext;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

public class GradeSlabService {
    private final GradeSlabDao dao;
    private final MaintenanceService maintenanceService;

    public GradeSlabService(DataSource ds) {
        this.dao = new GradeSlabDao(ds);
        this.maintenanceService = new MaintenanceService(ds);
    }

    public List<GradeSlab> getSlabs(int sectionId, String instructorId) throws SQLException, ServiceException {
        UiContext.get().access().requireInstructorForSection(instructorId,sectionId);
        return dao.getSlabs(sectionId);
    }

    public void addSlab(int sectionId, String letter, double min, double max, String instructorId) throws SQLException, ServiceException {
        maintenanceService.requireWriteAllowed();
        UiContext.get().access().requireInstructorForSection(instructorId, sectionId);
        List<GradeSlab> slabs = getSlabs(sectionId, instructorId);
        for (GradeSlab slab : slabs) {
            if (slab.getLetter().equals(letter)) {
                throw new ServiceException("Letter Grade already exists");
            }
            if (!((slab.getMin() >= max && slab.getMax() >= max) || (slab.getMin() <= min && slab.getMax() <= min))) {
                throw new ServiceException("Overlapping Slabs");
            }
        }
        dao.insertSlab(sectionId, letter, min, max);
    }

    public void updateSlab(GradeSlab slab, int sectionId, String instructorId) throws SQLException, ServiceException {
        maintenanceService.requireWriteAllowed();
        UiContext.get().access().requireInstructorForSection(instructorId,sectionId);
        List<GradeSlab> slabs = getSlabs(sectionId, instructorId);
        for (GradeSlab s : slabs) {
            if (s.getLetter().equals(slab.getLetter())) {
                throw new ServiceException("Letter Grade already exists");
            }
            if (!((s.getMin() >= slab.getMax() && s.getMax() >= slab.getMax()) || (s.getMin() <= slab.getMin() && s.getMax() <= slab.getMin()))) {
                throw new ServiceException("Overlapping Slabs");
            }
        }
        dao.updateSlab(slab);
    }

    public void deleteSlab(int slabId, int sectionId, String instructorId) throws SQLException, ServiceException {
        maintenanceService.requireWriteAllowed();
        UiContext.get().access().requireInstructorForSection(instructorId,sectionId);
        List<GradeSlab> slabs = getSlabs(sectionId, instructorId);
        dao.deleteSlab(slabId);
    }
}
