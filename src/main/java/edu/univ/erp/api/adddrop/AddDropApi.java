package edu.univ.erp.api.adddrop;

import edu.univ.erp.api.common.ApiResult;
import edu.univ.erp.service.AddDropService;
import java.time.LocalDate;

public class AddDropApi {
    private final AddDropService service;

    //constructor
    public AddDropApi(AddDropService service) {
        this.service = service;
    }

    //set deadline
    public ApiResult<Void> setDeadline(LocalDate date) {
        try {
            service.updateDeadline(date);
            return ApiResult.okMessage("Add/Drop deadline updated.");
        } catch (Exception ex) {
            return ApiResult.error("Failed: " + ex.getMessage());
        }
    }

    //get deadline
    public ApiResult<LocalDate> getDeadline() {
        try {
            return ApiResult.ok(service.getDeadline());
        } catch (Exception ex) {
            return ApiResult.error("Failed: " + ex.getMessage());
        }
    }
}
