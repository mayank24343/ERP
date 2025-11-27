package edu.univ.erp.api.slabs;

import edu.univ.erp.api.common.ApiResult;
import edu.univ.erp.domain.GradeSlab;
import edu.univ.erp.service.GradeSlabService;

import java.util.List;

public class GradeSlabApi {
    private final GradeSlabService service;

    //constructor
    public GradeSlabApi(GradeSlabService service) {
        this.service = service;
    }

    //add slab
    public ApiResult<String> add(int sectionId, String letter, double min, double max, String instructorId) {
        try {
            service.addSlab(sectionId, letter, min, max, instructorId);
            return ApiResult.okMessage("Slab added");
        } catch (Exception e) {
            return ApiResult.error("Error adding slab: " + e.getMessage());
        }
    }

    //update slab
    public ApiResult<String> update(GradeSlab slab, int sectionId, String instructorId) {
        try {
            service.updateSlab(slab, sectionId, instructorId);
            return ApiResult.okMessage("Slab updated");
        } catch (Exception e) {
            return ApiResult.error("Error updating slab: " + e.getMessage());
        }
    }

    //delete slab
    public ApiResult<String> delete(int slabId, int sectionId, String instructorId) {
        try {
            service.deleteSlab(slabId, sectionId, instructorId);
            return ApiResult.okMessage("Slab deleted");
        } catch (Exception e) {
            return ApiResult.error("Error deleting slab: " + e.getMessage());
        }
    }

    //list of slabs
    public ApiResult<List<GradeSlab>> list(int sectionId, String instructorId) {
        try {
            return ApiResult.ok(service.getSlabs(sectionId, instructorId));
        } catch (Exception e) {
            return ApiResult.error("Failed To Load Slabs: " + e.getMessage());
        }
    }
}

